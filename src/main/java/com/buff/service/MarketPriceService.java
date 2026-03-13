package com.buff.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 市场价格服务接口
 *
 * @author Administrator
 */
public interface MarketPriceService {

    /**
     * 根据模板ID查询参考价格
     */
    BigDecimal getPriceByTemplateId(Long templateId);

    /**
     * 批量查询价格（返回 Map: templateId -> price）
     */
    Map<Long, BigDecimal> getPricesByTemplateIds(List<Long> templateIds);

    /**
     * 更新单个饰品参考价格
     */
    void updatePrice(Long templateId, BigDecimal price);

    /**
     * 从 Steam Community Market 同步所有饰品参考价格并写入数据库
     */
    void fetchAndUpdateAllPrices();
}
