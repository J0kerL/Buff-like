package com.buff.mapper;

import com.buff.model.entity.ItemTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 饰品模板Mapper接口
 *
 * @author Administrator
 */
@Mapper
public interface ItemTemplateMapper {

    /**
     * 根据ID查询
     */
    ItemTemplate selectById(@Param("id") Long id);

    /**
     * 查询所有饰品模板
     */
    List<ItemTemplate> selectAll();

    /**
     * 根据名称查询
     */
    ItemTemplate selectByName(@Param("name") String name);

    /**
     * 插入饰品模板
     */
    int insert(ItemTemplate itemTemplate);

    /**
     * 更新饰品模板
     */
    int update(ItemTemplate itemTemplate);

    /**
     * 删除饰品模板
     */
    int deleteById(@Param("id") Long id);
}
