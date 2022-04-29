package com.doc.demo.service;

import com.doc.demo.entity.StudentInfo;
import java.util.List;

/**
 * @author : zengYeMin
 * @date : 2022/4/22 15:39
 **/
public interface IStudentInfoService extends IServiceBase<StudentInfo> {

    /**
     * 根据老师名字查询学生信息
     *
     * @param teacherName 老师名字
     * @return {@link List <StudentInfo>}
     */
    List<StudentInfo> findByTeacherName(String teacherName);

    /**
     * 根据学生集合ID查询学生信息
     *
     * @param ids 学生ID集合
     * @return {@link List<StudentInfo>}
     */
    List<StudentInfo> findByStudentIdIn(Long... ids);

    /**
     * 根据传入老师的名字修改，学生的毕业状态
     *
     * @param graduate 毕业状态
     * @param teacherName 老师名字
     * @return 为0则表示没有进行修改，大于0则表示修改的行数
     */
    int updateGraduateByTeacherName(int graduate, String teacherName);

    /**
     * 查询表中学生的名字
     *
     * @return 查询到的名字集合 {@link List<String>}
     */
    List<String> findAllStudentName();

    StudentInfo findByStudentName(String studentName);

}
