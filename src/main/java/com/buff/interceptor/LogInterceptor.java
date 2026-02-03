package com.buff.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

/**
 * 日志拦截器
 * 用于记录请求日志
 *
 * @author Administrator
 */
@Slf4j
@Component
public class LogInterceptor implements HandlerInterceptor {

    private static final String START_TIME = "startTime";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 记录请求开始时间
        request.setAttribute(START_TIME, System.currentTimeMillis());

        String traceId = UUID.randomUUID().toString().replace("-", "");
        MDC.put("traceId", traceId);
        
        // 记录请求信息
        String method = request.getMethod();
        String uri = request.getRequestURI();
        
        log.info("请求开始: method={}, uri={}", method, uri);
        
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, 
                          Object handler, ModelAndView modelAndView) {
        // 可以在这里记录Controller处理完成的信息
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                               Object handler, Exception ex) {
        // 计算请求耗时
        Long startTime = (Long) request.getAttribute(START_TIME);
        if (startTime != null) {
            long duration = System.currentTimeMillis() - startTime;
            String method = request.getMethod();
            String uri = request.getRequestURI();
            int status = response.getStatus();
            
            // 根据耗时和状态码选择日志级别
            if (status >= 500) {
                log.error("请求完成: method={}, uri={}, status={}, duration={}ms", 
                         method, uri, status, duration);
            } else if (status >= 400) {
                log.warn("请求完成: method={}, uri={}, status={}, duration={}ms", 
                        method, uri, status, duration);
            } else if (duration > 3000) {
                log.warn("请求耗时过长: method={}, uri={}, status={}, duration={}ms", 
                        method, uri, status, duration);
            } else {
                log.info("请求完成: method={}, uri={}, status={}, duration={}ms", 
                        method, uri, status, duration);
            }
        }
        
        // 记录异常
        if (ex != null) {
            log.error("请求处理异常: uri={}", request.getRequestURI(), ex);
        }

        MDC.clear();
    }


}
