package com.buff.websocket;

import com.buff.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * 通知WebSocket处理器
 * 处理WebSocket连接、断开、消息接收
 *
 * @author Administrator
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationWebSocketHandler extends TextWebSocketHandler {

    private final WebSocketSessionManager sessionManager;
    private final JwtUtils jwtUtils;

    /**
     * 连接建立后调用
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = extractUserIdFromSession(session);
        if (userId != null) {
            sessionManager.addSession(userId, session);
            // 发送连接成功消息
            Map<String, Object> result = new HashMap<>();
            result.put("type", "connected");
            result.put("message", "WebSocket连接成功");
            session.sendMessage(new TextMessage(toJson(result)));
        } else {
            log.warn("WebSocket连接失败: 无法获取用户ID");
            session.close(CloseStatus.NOT_ACCEPTABLE);
        }
    }

    /**
     * 接收消息时调用
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.debug("收到WebSocket消息: {}", payload);
        
        // 心跳检测
        if ("ping".equals(payload)) {
            session.sendMessage(new TextMessage("{\"type\":\"pong\"}"));
        }
    }

    /**
     * 连接关闭后调用
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long userId = extractUserIdFromSession(session);
        if (userId != null) {
            sessionManager.removeSession(userId);
        }
        log.info("WebSocket连接关闭: sessionId={}, status={}", session.getId(), status);
    }

    /**
     * 发生错误时调用
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        Long userId = extractUserIdFromSession(session);
        if (userId != null) {
            sessionManager.removeSession(userId);
        }
        log.error("WebSocket传输错误: sessionId={}, error={}", session.getId(), exception.getMessage());
    }

    /**
     * 从Session中提取用户ID
     */
    private Long extractUserIdFromSession(WebSocketSession session) {
        // 从URL参数中获取token
        URI uri = session.getUri();
        if (uri == null) {
            return null;
        }
        
        String query = uri.getQuery();
        if (query == null || !query.contains("token=")) {
            return null;
        }
        
        // 解析token参数
        String token = null;
        String[] params = query.split("&");
        for (String param : params) {
            if (param.startsWith("token=")) {
                token = param.substring(6);
                break;
            }
        }
        
        if (token == null) {
            return null;
        }
        
        // 验证token并获取用户ID
        if (jwtUtils.validateToken(token)) {
            return jwtUtils.getUserIdFromToken(token);
        }
        return null;
    }

    /**
     * 简单的JSON转换
     */
    private String toJson(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) {
                sb.append(",");
            }
            sb.append("\"").append(entry.getKey()).append("\":");
            Object value = entry.getValue();
            if (value instanceof String) {
                sb.append("\"").append(value).append("\"");
            } else {
                sb.append(value);
            }
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }
}
