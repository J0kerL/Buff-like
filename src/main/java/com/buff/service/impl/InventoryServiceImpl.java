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
import com.buff.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
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

        // 将 wears 字符串转为磨损范围列表（支持 OR 条件查询）
        List<Map<String, BigDecimal>> wearRanges = parseWearRanges(queryDTO.getWears());

        // 解析 typeSelects 为类型条件列表
        List<Map<String, String>> typeConditions = parseTypeConditions(queryDTO.getTypeSelects());

        // 查询总数
        Long total = inventoryMapper.countInventory(
                userId,
                queryDTO.getStatus(),
                typeConditions,
                wearRanges,
                queryDTO.getKeyword()
        );

        if (total == 0) {
            return PageResult.empty(queryDTO.getPageNum(), queryDTO.getPageSize());
        }

        // 查询列表
        List<InventoryVO> list = inventoryMapper.selectInventoryList(
                userId,
                queryDTO.getStatus(),
                typeConditions,
                wearRanges,
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
     * 将递号分隔的 typeSelects 解析为类型条件列表。
     * 每个元素是 Map，包含 "type" 和 "keyword" 两个键：
     *   TYPE:步枪           → {type=步枪, keyword=}
     *   SUB:AK-47:步枪    → {type=步枪, keyword=AK-47}
     */
    private List<Map<String, String>> parseTypeConditions(String typeSelects) {
        List<Map<String, String>> conditions = new ArrayList<>();
        if (typeSelects == null || typeSelects.isBlank()) return conditions;
        for (String sel : typeSelects.split(",")) {
            sel = sel.trim();
            if (sel.startsWith("TYPE:")) {
                Map<String, String> cond = new java.util.HashMap<>();
                cond.put("type", sel.substring(5));
                cond.put("keyword", "");
                conditions.add(cond);
            } else if (sel.startsWith("SUB:")) {
                int firstColon = sel.indexOf(':');
                int lastColon = sel.lastIndexOf(':');
                if (lastColon > firstColon) {
                    Map<String, String> cond = new java.util.HashMap<>();
                    cond.put("keyword", sel.substring(firstColon + 1, lastColon));
                    cond.put("type", sel.substring(lastColon + 1));
                    conditions.add(cond);
                }
            }
        }
        return conditions;
    }

    /**
     * 将逗号分隔的外观标签转换为 [{"min": x, "max": y}] 列表（Map 键名限，以展 MyBatis OGNL 访问）
     */
    private List<Map<String, BigDecimal>> parseWearRanges(String wears) {
        List<Map<String, BigDecimal>> ranges = new ArrayList<>();
        if (wears == null || wears.isBlank()) return ranges;
        for (String wear : wears.split(",")) {
            BigDecimal[] bounds = switch (wear.trim()) {
                case "崭新出厂" -> new BigDecimal[]{BigDecimal.ZERO, new BigDecimal("0.07")};
                case "略有磨损" -> new BigDecimal[]{new BigDecimal("0.07"), new BigDecimal("0.15")};
                case "久经沙场" -> new BigDecimal[]{new BigDecimal("0.15"), new BigDecimal("0.37")};
                case "破损不堪" -> new BigDecimal[]{new BigDecimal("0.37"), new BigDecimal("0.44")};
                case "战痕累累" -> new BigDecimal[]{new BigDecimal("0.44"), BigDecimal.ONE};
                default -> null;
            };
            if (bounds != null) {
                Map<String, BigDecimal> range = new java.util.HashMap<>();
                range.put("min", bounds[0]);
                range.put("max", bounds[1]);
                ranges.add(range);
            }
        }
        return ranges;
    }
    
    /**
     * 计算用户库存总价値。
     * 通过 SQL 层 JOIN + SUM 聚合，避免将全量库存加载到 Java 内存。
     */
    private BigDecimal calculateTotalValue(Long userId) {
        try {
            BigDecimal totalValue = inventoryMapper.selectTotalValueByUserId(userId);
            return totalValue != null ? totalValue : BigDecimal.ZERO;
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
