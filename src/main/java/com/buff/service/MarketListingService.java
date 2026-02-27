package com.buff.service;

import com.buff.common.PageResult;
import com.buff.model.dto.ListingCreateDTO;
import com.buff.model.dto.MarketQueryDTO;
import com.buff.model.vo.MarketListingVO;

/**
 * 市场挂单服务接口
 *
 * @author Administrator
 */
public interface MarketListingService {

    /**
     * 上架商品
     */
    Long createListing(ListingCreateDTO dto);

    /**
     * 下架商品
     */
    void cancelListing(Long id);

    /**
     * 查询市场商品列表
     */
    PageResult<MarketListingVO> getMarketListings(MarketQueryDTO queryDTO);

    /**
     * 查看挂单详情
     */
    MarketListingVO getListingDetail(Long id);

    /**
     * 查看我的挂单
     */
    PageResult<MarketListingVO> getMyListings(Integer status, Integer pageNum, Integer pageSize);
}
