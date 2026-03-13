package com.buff.service.impl;

import com.buff.mapper.ItemTemplateMapper;
import com.buff.model.entity.ItemTemplate;
import com.buff.service.MarketPriceService;
import com.buff.util.BuffCrawlerUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 市场价格服务实现类
 * <p>
 * 合并 item_market_price 表后，直接操作 item_template 的 ref_price 字段。
 *
 * @author Administrator
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MarketPriceServiceImpl implements MarketPriceService {

    private final ItemTemplateMapper itemTemplateMapper;

    @Override
    public BigDecimal getPriceByTemplateId(Long templateId) {
        if (templateId == null) {
            return BigDecimal.ZERO;
        }
        ItemTemplate template = itemTemplateMapper.selectById(templateId);
        return (template != null && template.getRefPrice() != null)
                ? template.getRefPrice() : BigDecimal.ZERO;
    }

    @Override
    public Map<Long, BigDecimal> getPricesByTemplateIds(List<Long> templateIds) {
        if (templateIds == null || templateIds.isEmpty()) {
            return new HashMap<>();
        }
        List<ItemTemplate> templates = itemTemplateMapper.selectByIds(templateIds);
        Map<Long, BigDecimal> result = new HashMap<>();
        for (ItemTemplate t : templates) {
            if (t.getRefPrice() != null) {
                result.put(t.getId(), t.getRefPrice());
            }
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePrice(Long templateId, BigDecimal price) {
        if (templateId == null || price == null) {
            log.warn("updatePrice 参数缺失: templateId={}, price={}", templateId, price);
            return;
        }
        itemTemplateMapper.updateRefPrice(templateId, price);
        log.info("更新参考价格成功: templateId={}, price={}", templateId, price);
    }

    @Override
    public void fetchAndUpdateAllPrices() {
        log.info("开始从 Steam Market 同步价格数据...");

        List<ItemTemplate> templates = itemTemplateMapper.selectAll();
        if (templates.isEmpty()) {
            log.warn("饰品模板表为空，跳过价格同步");
            return;
        }
        log.info("共 {} 个饰品模板，开始逐一查询 Steam 价格", templates.size());

        int successCount = 0;
        for (ItemTemplate template : templates) {
            try {
                BigDecimal price = BuffCrawlerUtil.fetchPriceByName(template.getMarketHashName());
                if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
                    // Steam API 不可用时跳过，保留旧价格不覆盖
                    log.warn("Steam API 无效价格，跳过本次更新: id={}, name={}",
                            template.getId(), template.getName());
                } else {
                    itemTemplateMapper.updateRefPrice(template.getId(), price);
                    successCount++;
                    log.info("价格同步成功: {} = ¥{}", template.getName(), price);
                }

                // Steam Market API 有频率限制（约 20 次/分钟），每次请求间隔 2~5 秒
                long delay = 2000 + (long) (Math.random() * 3000);
                Thread.sleep(delay);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("价格同步任务被中断");
                break;
            } catch (Exception e) {
                log.error("查询价格失败，跳过: templateId={}, name={}", template.getId(), template.getName(), e);
            }
        }
        log.info("Steam 价格同步完成：成功更新 {} / {} 个饰品", successCount, templates.size());
    }

}
