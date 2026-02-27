package com.buff.mapper;

import com.buff.model.entity.MarketListing;
import com.buff.model.vo.MarketListingVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * 市场挂单Mapper接口
 *
 * @author Administrator
 */
@Mapper
public interface MarketListingMapper {

    /**
     * 根据ID查询挂单
     */
    MarketListing selectById(@Param("id") Long id);

    /**
     * 查询挂单详情（包含饰品和卖家信息）
     */
    MarketListingVO selectListingDetailById(@Param("id") Long id);

    /**
     * 分页查询市场商品列表
     */
    List<MarketListingVO> selectMarketListings(@Param("keyword") String keyword,
                                               @Param("templateId") Long templateId,
                                               @Param("minPrice") BigDecimal minPrice,
                                               @Param("maxPrice") BigDecimal maxPrice,
                                               @Param("minWear") BigDecimal minWear,
                                               @Param("maxWear") BigDecimal maxWear,
                                               @Param("sortField") String sortField,
                                               @Param("sortOrder") String sortOrder,
                                               @Param("offset") Integer offset,
                                               @Param("pageSize") Integer pageSize);

    /**
     * 统计市场商品总数
     */
    Long countMarketListings(@Param("keyword") String keyword,
                             @Param("templateId") Long templateId,
                             @Param("minPrice") BigDecimal minPrice,
                             @Param("maxPrice") BigDecimal maxPrice,
                             @Param("minWear") BigDecimal minWear,
                             @Param("maxWear") BigDecimal maxWear);

    /**
     * 查询用户的挂单列表
     */
    List<MarketListingVO> selectMyListings(@Param("sellerId") Long sellerId,
                                           @Param("status") Integer status,
                                           @Param("offset") Integer offset,
                                           @Param("pageSize") Integer pageSize);

    /**
     * 统计用户挂单总数
     */
    Long countMyListings(@Param("sellerId") Long sellerId,
                         @Param("status") Integer status);

    /**
     * 插入挂单
     */
    int insert(MarketListing listing);

    /**
     * 更新挂单状态
     */
    int updateStatus(@Param("id") Long id,
                     @Param("status") Integer status,
                     @Param("version") Integer version);

    /**
     * 删除挂单
     */
    int deleteById(@Param("id") Long id);

    /**
     * 根据库存ID查询挂单
     */
    MarketListing selectByInventoryId(@Param("inventoryId") Long inventoryId);
}
