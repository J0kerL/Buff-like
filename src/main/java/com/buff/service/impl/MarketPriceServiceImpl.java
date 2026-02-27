package com.buff.service.impl;

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
            log.warn("updatePrice 参数缺失: templateId={}, price={}", templateId, price);
            return;
        }
        MarketPrice marketPrice = new MarketPrice();
        marketPrice.setTemplateId(templateId);
        marketPrice.setPrice(price);
        marketPrice.setSource("STEAM");
        marketPriceMapper.insertOrUpdate(marketPrice);
        log.info("更新价格成功: templateId={}, price={}", templateId, price);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdatePrices(List<MarketPrice> prices) {
        if (prices == null || prices.isEmpty()) {
            return;
        }
        marketPriceMapper.batchInsertOrUpdate(prices);
        log.info("批量更新价格成功: 共 {} 条", prices.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void fetchAndUpdateAllPrices() {
        log.info("开始从 Steam Market 同步价格数据...");

        List<ItemTemplate> templates = itemTemplateMapper.selectAll();
        if (templates.isEmpty()) {
            log.warn("饰品模板表为空，跳过价格同步");
            return;
        }
        log.info("共 {} 个饰品模板，开始逐一查询 Steam 价格", templates.size());

        List<MarketPrice> priceList = new ArrayList<>();
        for (ItemTemplate template : templates) {
            try {
                BigDecimal price = fetchSteamPrice(template);

                MarketPrice marketPrice = new MarketPrice();
                marketPrice.setTemplateId(template.getId());
                marketPrice.setPrice(price);
                marketPrice.setSource("STEAM");
                priceList.add(marketPrice);

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

        if (!priceList.isEmpty()) {
            batchUpdatePrices(priceList);
            log.info("Steam 价格同步完成：成功更新 {} 个饰品", priceList.size());
        }
    }

    /**
     * 从 Steam Community Market API 查询单个饰品价格。
     * <p>
     * 模板必须配置 marketHashName（英文名）才能调用 API；
     * 未配置或 API 返回失败时，降级使用按稀有度生成的模拟价格。
     */
    private BigDecimal fetchSteamPrice(ItemTemplate template) {
        if (template.getMarketHashName() == null || template.getMarketHashName().isBlank()) {
            log.warn("模板未配置 market_hash_name，使用模拟价格: id={}, name={}",
                    template.getId(), template.getName());
            return mockPrice(template);
        }

        BigDecimal price = BuffCrawlerUtil.fetchPriceByName(template.getMarketHashName());
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Steam API 未返回有效价格，使用模拟价格: {}", template.getName());
            return mockPrice(template);
        }
        return price;
    }

    /**
     * 按稀有度生成模拟价格，仅在 Steam API 不可用时作为兜底。
     */
    private BigDecimal mockPrice(ItemTemplate template) {
        String rarity = template.getRarity();
        if (rarity == null) {
            return BigDecimal.valueOf(10.0);
        }
        return switch (rarity) {
            case "隐秘" -> BigDecimal.valueOf(1000 + Math.random() * 9000);
            case "保密" -> BigDecimal.valueOf(100 + Math.random() * 900);
            case "受限" -> BigDecimal.valueOf(10 + Math.random() * 90);
            default    -> BigDecimal.valueOf(1 + Math.random() * 9);
        };
    }
}
