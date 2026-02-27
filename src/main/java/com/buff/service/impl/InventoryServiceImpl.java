package com.buff.service.impl;

import com.buff.common.PageResult;
import com.buff.common.ResultCode;
import com.buff.constant.InventoryStatus;
import com.buff.exception.BusinessException;
import com.buff.mapper.InventoryMapper;
import com.buff.model.dto.InventoryQueryDTO;
import com.buff.model.vo.InventoryStatisticsVO;
import com.buff.model.vo.InventoryVO;
import com.buff.service.InventoryService;
import com.buff.service.MarketPriceService;
import com.buff.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 库存服务实现类
 *
 * @author Administrator
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryMapper inventoryMapper;
    private final MarketPriceService marketPriceService;

    @Override
    public PageResult<InventoryVO> getMyInventory(InventoryQueryDTO queryDTO) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        // 参数校验
        if (queryDTO.getPageNum() == null || queryDTO.getPageNum() < 1) {
            queryDTO.setPageNum(1);
        }
        if (queryDTO.getPageSize() == null || queryDTO.getPageSize() < 1) {
            queryDTO.setPageSize(10);
        }

        // 计算偏移量
        int offset = (queryDTO.getPageNum() - 1) * queryDTO.getPageSize();

        // 查询总数
        Long total = inventoryMapper.countInventory(
                userId,
                queryDTO.getStatus(),
                queryDTO.getType(),
                queryDTO.getRarity(),
                queryDTO.getKeyword()
        );

        if (total == 0) {
            return PageResult.empty(queryDTO.getPageNum(), queryDTO.getPageSize());
        }

        // 查询列表
        List<InventoryVO> list = inventoryMapper.selectInventoryList(
                userId,
                queryDTO.getStatus(),
                queryDTO.getType(),
                queryDTO.getRarity(),
                queryDTO.getKeyword(),
                offset,
                queryDTO.getPageSize()
        );

        return new PageResult<>(total, list, queryDTO.getPageNum(), queryDTO.getPageSize());
    }

    @Override
    public InventoryVO getInventoryDetail(Long id) {
        if (id == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        InventoryVO inventoryVO = inventoryMapper.selectInventoryDetailById(id);
        if (inventoryVO == null) {
            throw new BusinessException(ResultCode.ERROR.getCode(), "库存不存在");
        }

        // 验证是否属于当前用户
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        return inventoryVO;
    }

    @Override
    public InventoryStatisticsVO getStatistics() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        InventoryStatisticsVO statistics = new InventoryStatisticsVO();

        // 统计各状态数量
        List<Map<String, Object>> statusList = inventoryMapper.countByStatus(userId);
        long inStockCount = 0L;
        long onSaleCount = 0L;
        long lockedCount = 0L;

        for (Map<String, Object> map : statusList) {
            try {
                Object statusObj = map.get("status");
                Object countObj = map.get("count");

                int status = 0;
                if (statusObj instanceof Number) {
                    status = ((Number) statusObj).intValue();
                } else if (statusObj instanceof String) {
                    status = Integer.parseInt((String) statusObj);
                }

                long count = 0L;
                if (countObj instanceof Number) {
                    count = ((Number) countObj).longValue();
                } else if (countObj instanceof String) {
                    count = Long.parseLong((String) countObj);
                }

                if (status == InventoryStatus.IN_STOCK) {
                    inStockCount = count;
                } else if (status == InventoryStatus.ON_SALE) {
                    onSaleCount = count;
                } else if (status == InventoryStatus.LOCKED) {
                    lockedCount = count;
                }
            } catch (Exception e) {
                log.warn("解析状态统计失败: map={}", map, e);
            }
        }

        statistics.setInStockCount(inStockCount);
        statistics.setOnSaleCount(onSaleCount);
        statistics.setLockedCount(lockedCount);
        statistics.setTotalCount(inStockCount + onSaleCount + lockedCount);

        // 统计按稀有度分组
        List<Map<String, Object>> rarityList = inventoryMapper.countByRarity(userId);
        Map<String, Long> rarityStats = new HashMap<>();
        for (Map<String, Object> map : rarityList) {
            String rarity = (String) map.get("rarity");
            Object countObj = map.get("count");
            Long count = 0L;
            if (countObj instanceof Number) {
                count = ((Number) countObj).longValue();
            } else if (countObj instanceof String) {
                try {
                    count = Long.parseLong((String) countObj);
                } catch (NumberFormatException e) {
                    log.warn("解析稀有度统计失败: rarity={}, count={}", rarity, countObj);
                }
            }
            if (rarity != null) {
                rarityStats.put(rarity, count);
            }
        }
        statistics.setRarityStats(rarityStats);

        // 统计按类型分组
        List<Map<String, Object>> typeList = inventoryMapper.countByType(userId);
        Map<String, Long> typeStats = new HashMap<>();
        for (Map<String, Object> map : typeList) {
            String type = (String) map.get("type");
            Object countObj = map.get("count");
            Long count = 0L;
            if (countObj instanceof Number) {
                count = ((Number) countObj).longValue();
            } else if (countObj instanceof String) {
                try {
                    count = Long.parseLong((String) countObj);
                } catch (NumberFormatException e) {
                    log.warn("解析类型统计失败: type={}, count={}", type, countObj);
                }
            }
            if (type != null) {
                typeStats.put(type, count);
            }
        }
        statistics.setTypeStats(typeStats);

        // 计算库存总价值
        BigDecimal totalValue = calculateTotalValue(userId);
        statistics.setTotalValue(totalValue);

        log.info("用户库存统计: userId={}, totalCount={}, totalValue={}",
                userId, statistics.getTotalCount(), totalValue);

        return statistics;
    }

    /**
     * 计算用户库存总价值
     */
    private BigDecimal calculateTotalValue(Long userId) {
        try {
            // 查询用户所有库存
            List<InventoryVO> inventoryList = inventoryMapper.selectInventoryList(
                    userId, null, null, null, null, 0, Integer.MAX_VALUE
            );

            if (inventoryList.isEmpty()) {
                return BigDecimal.ZERO;
            }

            // 提取所有模板ID
            List<Long> templateIds = inventoryList.stream()
                    .map(InventoryVO::getTemplateId)
                    .distinct()
                    .toList();

            // 批量查询价格
            Map<Long, BigDecimal> priceMap = marketPriceService.getPricesByTemplateIds(templateIds);

            // 累加计算总价值
            BigDecimal totalValue = BigDecimal.ZERO;
            for (InventoryVO inventory : inventoryList) {
                BigDecimal price = priceMap.getOrDefault(inventory.getTemplateId(), BigDecimal.ZERO);
                totalValue = totalValue.add(price);
            }

            return totalValue;

        } catch (Exception e) {
            log.error("计算库存总价值失败: userId={}", userId, e);
            return BigDecimal.ZERO;
        }
    }

    @Override
    public List<InventoryVO> getInventoryByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        return inventoryMapper.selectByIds(ids);
    }
}
