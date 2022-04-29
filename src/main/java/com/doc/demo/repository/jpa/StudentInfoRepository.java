package com.doc.demo.repository.jpa;

import com.doc.demo.entity.StudentInfo;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * @author : zengYeMin
 * @date : 2022/4/22 14:19
 **/
public interface StudentInfoRepository extends JpaRepository<StudentInfo, Long> {

    /**
     * 根据老师名字查询学生信息
     *
     * @param teacherName 老师名字
     * @return {@link List<StudentInfo>}
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
    @Query("update StudentInfo set graduate=:graduate where teacherName=:teacherName")
    int updateGraduateByTeacherName(@Param("graduate") int graduate, @Param("teacherName") String teacherName);

    /**
     * 获取当前表中全部的学生名字
     *
     * @return 学生名字集合 {@link List<String>}
     */
    @Query("select info.studentName from StudentInfo info")
    List<String> findAllStudentName();

    /**
     * 根据学生姓名查询学生信息
     *
     * @param studentName 学生名字,例如：张三
     * @return 学生信息对象 {@link StudentInfo}
     */
    StudentInfo findByStudentName(String studentName);
}
