package com.doc.demo.service;

import com.doc.demo.entity.ExamResults;
import java.util.Date;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * @author : zengYeMin
 * @date : 2022/4/22 16:19
 **/
public interface IExamResultsService extends IServiceBase<ExamResults> {

    /**
     * 以学生的名字进行模糊查询
     *
     * @param name 学生名字
     * @return 考试信息集合 {@link List < ExamResults >}
     */
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
     * 根据学生的名字获取分页信息
     *
     * @param pageable 分页对象
     * @param studentNameList 学生名字
     * @return 查询到的分页数据 {@link Page<ExamResults>}
     */
    Page<ExamResults> findByStudentNamePage(List<String> studentNameList, Pageable pageable);
}
