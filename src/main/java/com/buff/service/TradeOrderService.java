package com.buff.service;

import com.buff.common.PageResult;
import com.buff.model.dto.OrderCreateDTO;
import com.buff.model.vo.OrderVO;

/**
 * 交易订单服务接口
 *
 * @author Administrator
 */
public interface TradeOrderService {

    /**
     * 创建订单（购买商品）
     */
    String createOrder(OrderCreateDTO dto);

    /**
     * 支付订单
     */
    void payOrder(Long id);

    /**
     * 卖家发货
     */
    void deliverOrder(Long id);

    /**
     * 买家确认收货
     */
    void confirmOrder(Long id);

    /**
     * 取消订单
     */
    void cancelOrder(Long id);

    /**
     * 查看订单详情
     */
    OrderVO getOrderDetail(Long id);

    /**
     * 查看我的购买订单
     */
    PageResult<OrderVO> getMyBuyOrders(Integer status, Integer pageNum, Integer pageSize);

    /**
     * 查看我的出售订单
     */
    PageResult<OrderVO> getMySellOrders(Integer status, Integer pageNum, Integer pageSize);
}
