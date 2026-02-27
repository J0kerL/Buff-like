package com.buff.service.impl;

import com.buff.common.PageResult;
import com.buff.common.ResultCode;
import com.buff.constant.InventoryStatus;
import com.buff.constant.ListingStatus;
import com.buff.exception.BusinessException;
import com.buff.mapper.InventoryMapper;
import com.buff.mapper.MarketListingMapper;
import com.buff.model.dto.ListingCreateDTO;
import com.buff.model.dto.MarketQueryDTO;
import com.buff.model.entity.MarketListing;
import com.buff.model.entity.UserInventory;
import com.buff.model.vo.MarketListingVO;
import com.buff.service.MarketListingService;
import com.buff.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 市场挂单服务实现类
 *
 * @author Administrator
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MarketListingServiceImpl implements MarketListingService {

    private final MarketListingMapper marketListingMapper;
    private final InventoryMapper inventoryMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createListing(ListingCreateDTO dto) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        // 1. 查询库存
        UserInventory inventory = inventoryMapper.selectById(dto.getInventoryId());
        if (inventory == null) {
            throw new BusinessException(ResultCode.ERROR.getCode(), "库存不存在");
        }

        // 2. 验证库存所有权
        if (!inventory.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.ERROR.getCode(), "无权操作该库存");
        }

        // 3. 验证库存状态（必须是在库状态）
        if (inventory.getStatus() != InventoryStatus.IN_STOCK) {
            throw new BusinessException(ResultCode.ERROR.getCode(), "该饰品不在库中，无法上架");
        }

        // 4. 检查是否已经上架
        MarketListing existingListing = marketListingMapper.selectByInventoryId(dto.getInventoryId());
        if (existingListing != null) {
            throw new BusinessException(ResultCode.ERROR.getCode(), "该饰品已上架，请勿重复操作");
        }

        // 5. 更新库存状态为出售中
        int updateCount = inventoryMapper.updateStatus(dto.getInventoryId(), InventoryStatus.ON_SALE);
        if (updateCount == 0) {
            throw new BusinessException(ResultCode.ERROR.getCode(), "更新库存状态失败");
        }

        // 6. 创建挂单
        MarketListing listing = new MarketListing();
        listing.setSellerId(userId);
        listing.setInventoryId(dto.getInventoryId());
        listing.setTemplateId(inventory.getTemplateId());
        listing.setPrice(dto.getPrice());
        listing.setStatus(ListingStatus.ON_SALE);
        listing.setVersion(0);
        listing.setCreateTime(LocalDateTime.now());

        marketListingMapper.insert(listing);

        log.info("用户上架商品成功: userId={}, inventoryId={}, price={}", userId, dto.getInventoryId(), dto.getPrice());

        return listing.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelListing(Long id) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        // 1. 查询挂单
        MarketListing listing = marketListingMapper.selectById(id);
        if (listing == null) {
            throw new BusinessException(ResultCode.ERROR.getCode(), "挂单不存在");
        }

        // 2. 验证所有权
        if (!listing.getSellerId().equals(userId)) {
            throw new BusinessException(ResultCode.ERROR.getCode(), "无权操作该挂单");
        }

        // 3. 验证挂单状态（只能下架上架中的商品）
        if (listing.getStatus() != ListingStatus.ON_SALE) {
            throw new BusinessException(ResultCode.ERROR.getCode(), "该商品不在上架中，无法下架");
        }

        // 4. 更新挂单状态为已下架
        int updateCount = marketListingMapper.updateStatus(id, ListingStatus.OFF_SALE, listing.getVersion());
        if (updateCount == 0) {
            throw new BusinessException(ResultCode.ERROR.getCode(), "下架失败，请重试");
        }

        // 5. 恢复库存状态为在库
        inventoryMapper.updateStatus(listing.getInventoryId(), InventoryStatus.IN_STOCK);

        log.info("用户下架商品成功: userId={}, listingId={}", userId, id);
    }

    @Override
    public PageResult<MarketListingVO> getMarketListings(MarketQueryDTO queryDTO) {
        // 参数校验
        if (queryDTO.getPageNum() == null || queryDTO.getPageNum() < 1) {
            queryDTO.setPageNum(1);
        }
        if (queryDTO.getPageSize() == null || queryDTO.getPageSize() < 1) {
            queryDTO.setPageSize(20);
        }

        // 计算偏移量
        int offset = (queryDTO.getPageNum() - 1) * queryDTO.getPageSize();

        // 查询总数
        Long total = marketListingMapper.countMarketListings(
                queryDTO.getKeyword(),
                queryDTO.getTemplateId(),
                queryDTO.getMinPrice(),
                queryDTO.getMaxPrice(),
                queryDTO.getMinWear(),
                queryDTO.getMaxWear()
        );

        if (total == 0) {
            return PageResult.empty(queryDTO.getPageNum(), queryDTO.getPageSize());
        }

        // 查询列表
        List<MarketListingVO> list = marketListingMapper.selectMarketListings(
                queryDTO.getKeyword(),
                queryDTO.getTemplateId(),
                queryDTO.getMinPrice(),
                queryDTO.getMaxPrice(),
                queryDTO.getMinWear(),
                queryDTO.getMaxWear(),
                queryDTO.getSortField(),
                queryDTO.getSortOrder(),
                offset,
                queryDTO.getPageSize()
        );

        return new PageResult<>(total, list, queryDTO.getPageNum(), queryDTO.getPageSize());
    }

    @Override
    public MarketListingVO getListingDetail(Long id) {
        if (id == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        MarketListingVO listingVO = marketListingMapper.selectListingDetailById(id);
        if (listingVO == null) {
            throw new BusinessException(ResultCode.ERROR.getCode(), "挂单不存在");
        }

        return listingVO;
    }

    @Override
    public PageResult<MarketListingVO> getMyListings(Integer status, Integer pageNum, Integer pageSize) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        // 参数校验
        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 20;
        }

        // 计算偏移量
        int offset = (pageNum - 1) * pageSize;

        // 查询总数
        Long total = marketListingMapper.countMyListings(userId, status);

        if (total == 0) {
            return PageResult.empty(pageNum, pageSize);
        }

        // 查询列表
        List<MarketListingVO> list = marketListingMapper.selectMyListings(userId, status, offset, pageSize);

        return new PageResult<>(total, list, pageNum, pageSize);
    }
}
