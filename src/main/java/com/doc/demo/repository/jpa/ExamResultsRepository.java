package com.doc.demo.repository.jpa;

import com.doc.demo.entity.ExamResults;
import java.util.Date;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * @author : zengYeMin
 * @date : 2022/4/22 15:42
 **/
public interface ExamResultsRepository extends JpaRepository<ExamResults, Long> {

    /**
     * 以学生的名字进行模糊查询
     *
     * @param name 学生名字
     * @return 考试信息集合 {@link List<ExamResults>}
     */
    @Query("select er from ExamResults er where er.studentName like :name")
    List<ExamResults> findByStudentNameLike(String name);

    /**
     * 根据学生名字和考试时间查询
     *
     * @param name 学生名字
     * @param time 考试时间
     * @return 考试信息 {@link ExamResults}
     */
    ExamResults findByStudentNameAndExamTime(String name, Date time);

    /**
     * 根据学生的名字查询学生的考试信息
     *
     * @param studentName 学生名字
     * @return 学生考试信息实体 {@link List<ExamResults>}
     */
    List<ExamResults> findByStudentName(String studentName);

    /**
     * 根据传入的学生集合，和分页设置进行分页查询
     *
     * @param studentNameList 学生的名字集合
     * @param pageable 分页的设置
     * @return 查询到的分页对象 {@link Page<ExamResults>}
     */
    Page<ExamResults> findByStudentNameIn(List<String> studentNameList, Pageable pageable);

    /**
     * 查询根据学生的名字查询考试成绩，并且根据分数进行排序
     *
     * @param name 学生的名字
     * @return 排序后的考试信息 {@link List<ExamResults>}
     */
    List<ExamResults> findByStudentNameOrderByFractionDesc(String name);
}
