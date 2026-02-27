package com.buff.mapper;

import com.buff.model.entity.UserInventory;
import com.buff.model.vo.InventoryVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 库存Mapper接口
 *
 * @author Administrator
 */
@Mapper
public interface InventoryMapper {

    /**
     * 根据ID查询库存
     */
    UserInventory selectById(@Param("id") Long id);

    /**
     * 查询库存详情（包含饰品模板信息）
     */
    InventoryVO selectInventoryDetailById(@Param("id") Long id);

    /**
     * 分页查询用户库存列表
     */
    List<InventoryVO> selectInventoryList(@Param("userId") Long userId,
                                          @Param("status") Integer status,
                                          @Param("type") String type,
                                          @Param("rarity") String rarity,
                                          @Param("keyword") String keyword,
                                          @Param("offset") Integer offset,
                                          @Param("pageSize") Integer pageSize);

    /**
     * 统计用户库存总数
     */
    Long countInventory(@Param("userId") Long userId,
                        @Param("status") Integer status,
                        @Param("type") String type,
                        @Param("rarity") String rarity,
                        @Param("keyword") String keyword);

    /**
     * 统计用户各状态库存数量
     */
    List<Map<String, Object>> countByStatus(@Param("userId") Long userId);

    /**
     * 统计用户按稀有度分组的库存数量
     */
    List<Map<String, Object>> countByRarity(@Param("userId") Long userId);

    /**
     * 统计用户按类型分组的库存数量
     */
    List<Map<String, Object>> countByType(@Param("userId") Long userId);

    /**
     * 批量查询库存
     */
    List<InventoryVO> selectByIds(@Param("ids") List<Long> ids);

    /**
     * 插入库存
     */
    int insert(UserInventory inventory);

    /**
     * 更新库存状态
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 更新库存信息
     */
    int updateById(UserInventory inventory);

    /**
     * 删除库存（软删除）
     */
    int deleteById(@Param("id") Long id);

    /**
     * 查询用户库存总价值（JOIN market_price 在 DB 层聚合，避免全量加载到内存）
     */
    java.math.BigDecimal selectTotalValueByUserId(@Param("userId") Long userId);
}
