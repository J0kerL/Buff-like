package com.buff.mapper;

import com.buff.model.entity.TradeOrder;
import com.buff.model.vo.OrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 交易订单Mapper接口
 *
 * @author Administrator
 */
@Mapper
public interface TradeOrderMapper {

    /**
     * 根据ID查询订单
     */
    TradeOrder selectById(@Param("id") Long id);

    /**
     * 根据订单号查询订单
     */
    TradeOrder selectByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 查询订单详情（包含饰品信息）
     */
    OrderVO selectOrderDetailById(@Param("id") Long id);

    /**
     * 查询我的订单列表（买家）
     */
    List<OrderVO> selectMyBuyOrders(@Param("buyerId") Long buyerId,
                                    @Param("status") Integer status,
                                    @Param("offset") Integer offset,
                                    @Param("pageSize") Integer pageSize);

    /**
     * 统计我的购买订单总数
     */
    Long countMyBuyOrders(@Param("buyerId") Long buyerId,
                          @Param("status") Integer status);

    /**
     * 查询我的订单列表（卖家）
     */
    List<OrderVO> selectMySellOrders(@Param("sellerId") Long sellerId,
                                     @Param("status") Integer status,
                                     @Param("offset") Integer offset,
                                     @Param("pageSize") Integer pageSize);

    /**
     * 统计我的出售订单总数
     */
    Long countMySellOrders(@Param("sellerId") Long sellerId,
                           @Param("status") Integer status);

    /**
     * 插入订单
     */
    int insert(TradeOrder order);

    /**
     * 更新订单状态
     */
    int updateStatus(@Param("id") Long id,
                     @Param("status") Integer status);

    /**
     * 更新支付时间
     */
    int updatePayTime(@Param("id") Long id);

    /**
     * 更新发货时间
     */
    int updateDeliverTime(@Param("id") Long id);

    /**
     * 更新完成时间
     */
    int updateFinishTime(@Param("id") Long id);
}
