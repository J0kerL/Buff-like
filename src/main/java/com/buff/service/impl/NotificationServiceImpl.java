package com.buff.service.impl;

import com.buff.common.PageResult;
import com.buff.common.ResultCode;
import com.buff.exception.BusinessException;
import com.buff.mapper.NotificationMapper;
import com.buff.model.entity.Notification;
import com.buff.model.vo.NotificationVO;
import com.buff.service.NotificationService;
import com.buff.util.UserContext;
import com.buff.websocket.WebSocketSessionManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通知服务实现类
 *
 * @author Administrator
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;
    private final WebSocketSessionManager sessionManager;
    private final ObjectMapper objectMapper;

    @Override
    public void createNotification(Long userId, Integer type, String title, String content, Long orderId) {
        // 1. 创建通知记录
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setOrderId(orderId);
        notification.setIsRead(false);
        
        notificationMapper.insert(notification);
        log.info("创建通知: userId={}, type={}, title={}", userId, type, title);

        // 2. 实时推送通知
        pushNotification(userId, notification);
    }

    @Override
    public Long getUnreadCount() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return notificationMapper.countUnreadByUserId(userId);
    }

    @Override
    public PageResult<NotificationVO> getList(Integer pageNum, Integer pageSize) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 20;
        }

        int offset = (pageNum - 1) * pageSize;
        Long total = notificationMapper.countByUserId(userId);

        if (total == 0) {
            return PageResult.empty(pageNum, pageSize);
        }

        List<NotificationVO> list = notificationMapper.selectByUserId(userId, offset, pageSize);
        return new PageResult<>(total, list, pageNum, pageSize);
    }

    @Override
    public void markAsRead(Long id) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        Notification notification = notificationMapper.selectById(id);
        if (notification == null) {
            throw new BusinessException(ResultCode.ERROR.getCode(), "通知不存在");
        }

        if (!notification.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.ERROR.getCode(), "无权操作该通知");
        }

        notificationMapper.updateRead(id);
    }

    @Override
    public void markAsReadByOrderId(Long orderId) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        notificationMapper.updateReadByOrderId(userId, orderId);
    }

    @Override
    public void delete(Long id) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        Notification notification = notificationMapper.selectById(id);
        if (notification == null) {
            throw new BusinessException(ResultCode.ERROR.getCode(), "通知不存在");
        }

        if (!notification.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.ERROR.getCode(), "无权操作该通知");
        }

        notificationMapper.deleteById(id);
    }

    /**
     * 推送通知给在线用户
     */
    private void pushNotification(Long userId, Notification notification) {
        if (!sessionManager.isOnline(userId)) {
            log.debug("用户不在线，跳过推送: userId={}", userId);
            return;
        }

        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "notification");
            message.put("data", buildNotificationVO(notification));
            
            String json = objectMapper.writeValueAsString(message);
            sessionManager.sendMessage(userId, json);
            log.info("通知推送成功: userId={}, notificationId={}", userId, notification.getId());
        } catch (Exception e) {
            log.error("通知推送失败: userId={}, error={}", userId, e.getMessage());
        }
    }

    /**
     * 构建通知VO对象
     */
    private NotificationVO buildNotificationVO(Notification notification) {
        NotificationVO vo = new NotificationVO();
        vo.setId(notification.getId());
        vo.setType(notification.getType());
        vo.setTitle(notification.getTitle());
        vo.setContent(notification.getContent());
        vo.setOrderId(notification.getOrderId());
        vo.setIsRead(notification.getIsRead());
        vo.setCreateTime(notification.getCreateTime());
        return vo;
    }
}
