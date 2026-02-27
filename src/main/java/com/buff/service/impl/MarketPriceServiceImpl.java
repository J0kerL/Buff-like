package com.buff.service.impl;

import com.buff.config.BuffCrawlerConfig;
import com.buff.mapper.ItemTemplateMapper;
import com.buff.mapper.MarketPriceMapper;
import com.buff.model.entity.ItemTemplate;
import com.buff.model.entity.MarketPrice;
import com.buff.service.MarketPriceService;
import com.buff.util.BuffCrawlerUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 市场价格服务实现类
 *
 * @author Administrator
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MarketPriceServiceImpl implements MarketPriceService {

    private final MarketPriceMapper marketPriceMapper;
    private final ItemTemplateMapper itemTemplateMapper;
    private final BuffCrawlerConfig crawlerConfig;

    @Override
    public BigDecimal getPriceByTemplateId(Long templateId) {
        if (templateId == null) {
            return BigDecimal.ZERO;
        }

        MarketPrice marketPrice = marketPriceMapper.selectByTemplateId(templateId);
        return marketPrice != null ? marketPrice.getPrice() : BigDecimal.ZERO;
    }

    @Override
    public Map<Long, BigDecimal> getPricesByTemplateIds(List<Long> templateIds) {
        if (templateIds == null || templateIds.isEmpty()) {
            return new HashMap<>();
        }

        List<MarketPrice> prices = marketPriceMapper.selectByTemplateIds(templateIds);
        return prices.stream()
                .collect(Collectors.toMap(
                        MarketPrice::getTemplateId,
                        MarketPrice::getPrice,
                        (existing, replacement) -> existing
                ));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePrice(Long templateId, BigDecimal price) {
        if (templateId == null || price == null) {
            log.warn("更新价格失败: templateId或price为空");
            return;
        }

        MarketPrice marketPrice = new MarketPrice();
        marketPrice.setTemplateId(templateId);
        marketPrice.setPrice(price);
        marketPrice.setSource("BUFF");

        marketPriceMapper.insertOrUpdate(marketPrice);
        log.info("更新价格成功: templateId={}, price={}", templateId, price);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdatePrices(List<MarketPrice> prices) {
        if (prices == null || prices.isEmpty()) {
            log.warn("批量更新价格失败: 价格列表为空");
            return;
        }

        marketPriceMapper.batchInsertOrUpdate(prices);
        log.info("批量更新价格成功: 共{}条", prices.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void fetchAndUpdateAllPrices() {
        log.info("开始从BUFF平台爬取价格数据...");

        try {
            // 1. 查询所有饰品模板
            List<ItemTemplate> templates = itemTemplateMapper.selectAll();
            if (templates.isEmpty()) {
                log.warn("没有找到饰品模板数据");
                return;
            }

            log.info("找到{}个饰品模板，开始爬取价格", templates.size());

            // 2. 爬取每个饰品的价格
            List<MarketPrice> priceList = new ArrayList<>();
            for (ItemTemplate template : templates) {
                try {
                    // 调用爬虫获取价格（根据配置决定使用真实爬虫或模拟数据）
                    BigDecimal price = fetchPriceFromBuff(template);

                    MarketPrice marketPrice = new MarketPrice();
                    marketPrice.setTemplateId(template.getId());
                    marketPrice.setPrice(price);
                    marketPrice.setSource("BUFF");

                    priceList.add(marketPrice);

                    // 避免请求过快被封IP，添加延迟（2-5秒随机延迟）
                    long delay = 2000 + (long) (Math.random() * 3000);
                    Thread.sleep(delay);

                } catch (Exception e) {
                    log.error("爬取饰品价格失败: templateId={}, name={}",
                            template.getId(), template.getName(), e);
                }
            }

            // 3. 批量更新价格到数据库
            if (!priceList.isEmpty()) {
                batchUpdatePrices(priceList);
                log.info("价格更新完成: 成功更新{}个饰品价格", priceList.size());
            }

        } catch (Exception e) {
            log.error("从BUFF平台爬取价格失败", e);
            throw new RuntimeException("价格更新失败", e);
        }
    }

    /**
     * 从BUFF平台爬取单个饰品价格
     */
    private BigDecimal fetchPriceFromBuff(ItemTemplate template) {
        log.debug("开始爬取饰品价格: {}", template.getName());

        // 检查是否启用真实爬虫
        if (!crawlerConfig.getEnabled()) {
            log.debug("爬虫未启用，使用模拟价格: {}", template.getName());
            return generateMockPrice(template);
        }

        try {
            // 使用BuffCrawlerUtil爬取真实价格
            BigDecimal price = BuffCrawlerUtil.fetchPriceByName(template.getName());

            // 如果爬取失败，使用模拟价格作为兜底
            if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
                log.warn("爬取价格失败或价格无效，使用模拟价格: {}", template.getName());
                price = generateMockPrice(template);
            }

            return price;

        } catch (Exception e) {
            log.error("爬取BUFF价格异常: {}", template.getName(), e);
            // 异常时返回模拟价格
            return generateMockPrice(template);
        }
    }

    /**
     * 生成模拟价格（用于测试）
     */
    private BigDecimal generateMockPrice(ItemTemplate template) {
        // 根据稀有度生成不同价格范围
        String rarity = template.getRarity();
        if (rarity == null) {
            return BigDecimal.valueOf(10.0);
        }

        return switch (rarity) {
            case "隐秘" -> BigDecimal.valueOf(1000 + Math.random() * 9000);
            case "保密" -> BigDecimal.valueOf(100 + Math.random() * 900);
            case "受限" -> BigDecimal.valueOf(10 + Math.random() * 90);
            default -> BigDecimal.valueOf(1 + Math.random() * 9);
        };
    }
}
