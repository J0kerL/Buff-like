package com.buff.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket会话管理器
 * 管理用户ID与WebSocket Session的映射关系
 *
 * @author Administrator
 */
@Slf4j
@Component
public class WebSocketSessionManager {

    /**
     * 用户ID -> WebSocket Session 映射
     */
    private final Map<Long, WebSocketSession> sessionMap = new ConcurrentHashMap<>();

    /**
     * 添加会话
     */
    public void addSession(Long userId, WebSocketSession session) {
        sessionMap.put(userId, session);
        log.info("WebSocket会话添加: userId={}, sessionId={}", userId, session.getId());
    }

    /**
     * 移除会话
     */
    public void removeSession(Long userId) {
        WebSocketSession session = sessionMap.remove(userId);
        if (session != null) {
            log.info("WebSocket会话移除: userId={}, sessionId={}", userId, session.getId());
        }
    }

    /**
     * 获取会话
     */
    public WebSocketSession getSession(Long userId) {
        return sessionMap.get(userId);
    }

    /**
     * 判断用户是否在线
     */
    public boolean isOnline(Long userId) {
        return sessionMap.containsKey(userId);
    }

    /**
     * 发送消息给指定用户
     */
    public boolean sendMessage(Long userId, String message) {
        WebSocketSession session = sessionMap.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
                log.debug("WebSocket消息发送成功: userId={}", userId);
                return true;
            } catch (IOException e) {
                log.error("WebSocket消息发送失败: userId={}, error={}", userId, e.getMessage());
                removeSession(userId);
            }
        }
        return false;
    }

    /**
     * 获取在线用户数
     */
    public int getOnlineCount() {
        return sessionMap.size();
    }
}
