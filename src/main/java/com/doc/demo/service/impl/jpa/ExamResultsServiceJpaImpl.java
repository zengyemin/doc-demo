package com.doc.demo.service.impl.jpa;

import com.doc.demo.repository.jpa.ExamResultsRepository;
import com.doc.demo.service.IExamResultsService;
import com.doc.demo.entity.ExamResults;
import java.util.Date;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * @author : zengYeMin
 * @date : 2022/4/22 16:19
 **/
@Service
public class ExamResultsServiceJpaImpl implements IExamResultsService {

    @Resource
    private ExamResultsRepository repository;

    @Override
    public ExamResults findById(Long indexId) {
        return repository.findById(indexId).orElse(null);
    }

    @Override
    public List<ExamResults> findAll() {
        return repository.findAll();
    }

    @Override
    public List<ExamResults> findByStudentNameLike(String name) {
        return repository.findByStudentNameLike("%" + name + "%");
    }

    @Override
    public ExamResults findByStudentNameAndExamTime(String name, Date time) {
        return repository.findByStudentNameAndExamTime(name, time);
    }

    @Override
    public List<ExamResults> findByStudentName(String studentName) {
        return repository.findByStudentName(studentName);
    }

    @Override
    public Page<ExamResults> findByStudentNamePage(List<String> studentNameList, Pageable pageable) {
        return repository.findByStudentNameIn(studentNameList, pageable);
    }

    @Override
    public ExamResults save(ExamResults examResults) {
        return repository.save(examResults);
    }

    @Override
    public List<ExamResults> save(List<ExamResults> examResultsList) {
        return repository.saveAll(examResultsList);
    }

}
