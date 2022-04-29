package com.doc.demo.service;

import java.util.List;

/**
 * @author : zengYeMin
 * @date : 2022/4/22 16:38
 **/
public interface IServiceBase<E> {

    /**
     * 根据主键ID查询实体信息
     *
     * @param indexId 主键ID
     * @return 试信息 {@link E}
     */
    E findById(Long indexId);

    /**
     * 查询全部实体数据
     *
     * @return 成功查询到的实体集合 {@link E}
     */
    List<E> findAll();

    /**
     * 保存单个实体信息，保存状态以  {@link E} 中定义的主键为准
     *
     * 如果主键ID在数据库中没有则为新增
     * 如果主键ID在数据库中存在则为修改
     *
     * @param entity 要保存的实体
     * @return 保存成功的信息 {@link E}
     */
    E save(E entity);

    /**
     * 保存多个实体信息，保存状态以 {@link  List<E>} 中定义的主键为准
     *
     * 如果主键ID在数据库中没有则为新增
     * 如果主键ID在数据库中存在则为修改
     *
     * @param entityList 要保存的实体集合
     * @return 保存成功的信息 {@link List<E>}
     */
    List<E> save(List<E> entityList);
}
