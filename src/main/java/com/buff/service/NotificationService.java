package com.buff.service;

import com.buff.common.PageResult;
import com.buff.model.vo.NotificationVO;

/**
 * 通知服务接口
 *
 * @author Administrator
 */
public interface NotificationService {

    /**
     * 创建通知并推送
     *
     * @param userId   接收用户ID
     * @param type     通知类型
     * @param title    通知标题
     * @param content  通知内容
     * @param orderId  关联订单ID
     */
    void createNotification(Long userId, Integer type, String title, String content, Long orderId);

    /**
     * 获取未读通知数量
     */
    Long getUnreadCount();

    /**
     * 获取通知列表
     */
    PageResult<NotificationVO> getList(Integer pageNum, Integer pageSize);

    /**
     * 标记通知为已读
     */
    void markAsRead(Long id);

    /**
     * 根据订单ID标记通知为已读
     */
    void markAsReadByOrderId(Long orderId);

    /**
     * 删除通知
     */
    void delete(Long id);
}
