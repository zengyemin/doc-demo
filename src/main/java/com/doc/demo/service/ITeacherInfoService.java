package com.doc.demo.service;

import com.doc.demo.entity.TeacherInfo;
import java.util.List;

/**
 * @author : zengYeMin
 * @date : 2022/4/22 15:40
 **/
public interface ITeacherInfoService extends IServiceBase<TeacherInfo> {

    /**
     * 根据传入的地址查询老师信息
     *
     * @param address 地址,格式例如：湖南长沙
     * @return 老师信息集合 {@link List<TeacherInfo>}
     */
    List<TeacherInfo> findByAddress(String address);

    /**
     * 根据传入的地址和年龄查询老师信息
     *
     * @param address 地址,格式例如：湖南长沙
     * @param age 年龄,格式例如：20
     * @return 老师信息集合 {@link List<TeacherInfo>}
     */
    List<TeacherInfo> findByAddressAndAge(String address, Integer age);

    /**
     * 根据传入的性别查询老师信息
     *
     * @param sex 性别,格式例如：女
     * @return 老师信息集合 {@link List<TeacherInfo>}
     */
    List<TeacherInfo> findBySex(String sex);
}
