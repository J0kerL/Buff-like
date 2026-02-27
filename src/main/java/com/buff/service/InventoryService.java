package com.buff.service;

import com.buff.common.PageResult;
import com.buff.model.dto.InventoryQueryDTO;
import com.buff.model.vo.InventoryStatisticsVO;
import com.buff.model.vo.InventoryVO;

import java.util.List;

/**
 * 库存服务接口
 *
 * @author Administrator
 */
public interface InventoryService {

    /**
     * 分页查询我的库存
     */
    PageResult<InventoryVO> getMyInventory(InventoryQueryDTO queryDTO);

    /**
     * 查看库存详情
     */
    InventoryVO getInventoryDetail(Long id);

    /**
     * 获取库存统计信息
     */
    InventoryStatisticsVO getStatistics();

    /**
     * 批量查询库存
     */
    List<InventoryVO> getInventoryByIds(List<Long> ids);
}
