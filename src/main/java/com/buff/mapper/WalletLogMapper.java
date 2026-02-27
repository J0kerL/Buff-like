package com.buff.mapper;

import com.buff.model.entity.WalletLog;
import com.buff.model.vo.WalletLogVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 资金流水Mapper接口
 *
 * @author Administrator
 */
@Mapper
public interface WalletLogMapper {

    /**
     * 根据ID查询流水
     */
    WalletLog selectById(@Param("id") Long id);

    /**
     * 查询用户资金流水列表
     */
    List<WalletLogVO> selectUserLogs(@Param("userId") Long userId,
                                     @Param("type") Integer type,
                                     @Param("offset") Integer offset,
                                     @Param("pageSize") Integer pageSize);

    /**
     * 统计用户资金流水总数
     */
    Long countUserLogs(@Param("userId") Long userId,
                       @Param("type") Integer type);

    /**
     * 插入流水记录
     */
    int insert(WalletLog walletLog);
}
