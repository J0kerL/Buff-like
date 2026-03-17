package com.buff.mapper;

import com.buff.model.entity.Notification;
import com.buff.model.vo.NotificationVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 通知Mapper接口
 *
 * @author Administrator
 */
@Mapper
public interface NotificationMapper {

    /**
     * 插入通知
     */
    int insert(Notification notification);

    /**
     * 根据ID查询通知
     */
    Notification selectById(@Param("id") Long id);

    /**
     * 分页查询用户通知列表
     */
    List<NotificationVO> selectByUserId(@Param("userId") Long userId,
                                        @Param("offset") Integer offset,
                                        @Param("pageSize") Integer pageSize);

    /**
     * 统计用户通知总数
     */
    Long countByUserId(@Param("userId") Long userId);

    /**
     * 统计用户未读通知数
     */
    Long countUnreadByUserId(@Param("userId") Long userId);

    /**
     * 标记通知为已读
     */
    int updateRead(@Param("id") Long id);

    /**
     * 根据订单ID将用户所有相关通知标记为已读
     */
    int updateReadByOrderId(@Param("userId") Long userId,
                            @Param("orderId") Long orderId);

    /**
     * 删除通知
     */
    int deleteById(@Param("id") Long id);
}
