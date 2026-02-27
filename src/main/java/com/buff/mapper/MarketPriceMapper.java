package com.buff.mapper;

import com.buff.model.entity.MarketPrice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * 市场价格Mapper接口
 *
 * @author Administrator
 */
@Mapper
public interface MarketPriceMapper {

    /**
     * 根据模板ID查询价格
     */
    MarketPrice selectByTemplateId(@Param("templateId") Long templateId);

    /**
     * 批量查询价格
     */
    List<MarketPrice> selectByTemplateIds(@Param("templateIds") List<Long> templateIds);

    /**
     * 查询所有价格
     */
    List<MarketPrice> selectAll();

    /**
     * 插入或更新价格
     */
    int insertOrUpdate(MarketPrice marketPrice);

    /**
     * 批量插入或更新价格
     */
    int batchInsertOrUpdate(@Param("list") List<MarketPrice> list);

    /**
     * 根据模板ID更新价格
     */
    int updatePriceByTemplateId(@Param("templateId") Long templateId,
                                @Param("price") BigDecimal price);

    /**
     * 删除价格记录
     */
    int deleteByTemplateId(@Param("templateId") Long templateId);
}
