package com.buff.task;

import com.buff.service.MarketPriceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 市场价格定时任务
 *
 * @author Administrator
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MarketPriceTask {

    private final MarketPriceService marketPriceService;

    /**
     * 每天凌晨2点执行价格更新任务
     * cron表达式: 秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void updateMarketPrices() {
        log.info("========== 开始执行市场价格更新定时任务 ==========");

        try {
            marketPriceService.fetchAndUpdateAllPrices();
            log.info("========== 市场价格更新定时任务执行成功 ==========");

        } catch (Exception e) {
            log.error("========== 市场价格更新定时任务执行失败 ==========", e);
        }
    }

    /**
     * 测试方法：每5分钟执行一次（用于开发测试，生产环境请注释掉）
     */
    // @Scheduled(cron = "0 */5 * * * ?")
    public void updateMarketPricesForTest() {
        log.info("========== 开始执行市场价格更新定时任务（测试） ==========");

        try {
            marketPriceService.fetchAndUpdateAllPrices();
            log.info("========== 市场价格更新定时任务执行成功（测试） ==========");

        } catch (Exception e) {
            log.error("========== 市场价格更新定时任务执行失败（测试） ==========", e);
        }
    }
}
