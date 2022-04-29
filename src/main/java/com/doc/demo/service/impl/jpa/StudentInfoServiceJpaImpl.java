package com.doc.demo.service.impl.jpa;

import com.doc.demo.repository.jpa.StudentInfoRepository;
import com.doc.demo.service.IStudentInfoService;
import com.doc.demo.entity.StudentInfo;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author : zengYeMin
 * @date : 2022/4/22 16:19
 **/
@Service
public class StudentInfoServiceJpaImpl implements IStudentInfoService {

    @Autowired
    private StudentInfoRepository repository;

    @Override
    public StudentInfo findById(Long indexId) {
        return repository.findById(indexId).orElse(null);
    }

    @Override
    public List<StudentInfo> findAll() {
        return repository.findAll();
    }

    @Override
    public StudentInfo save(StudentInfo entity) {
        return repository.save(entity);
    }

    @Override
    public List<StudentInfo> save(List<StudentInfo> entityList) {
        return repository.saveAll(entityList);
    }

    @Override
    public List<StudentInfo> findByTeacherName(String teacherName) {
        return repository.findByTeacherName(teacherName);
    }

    @Override
    public List<StudentInfo> findByStudentIdIn(Long... ids) {
        return repository.findByStudentIdIn(ids);
    }

    @Override
    public int updateGraduateByTeacherName(int graduate, String teacherName) {
        return repository.updateGraduateByTeacherName(graduate, teacherName);
    }

    @Override
    public List<String> findAllStudentName() {
        return repository.findAllStudentName();
    }

    @Override
    public StudentInfo findByStudentName(String studentName) {
        return repository.findByStudentName(studentName);
    }
}
