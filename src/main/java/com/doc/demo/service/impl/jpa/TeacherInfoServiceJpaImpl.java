package com.doc.demo.service.impl.jpa;

import com.doc.demo.entity.TeacherInfo;
import com.doc.demo.repository.jpa.TeacherInfoRepository;
import com.doc.demo.service.ITeacherInfoService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author : zengYeMin
 * @date : 2022/4/22 16:17
 **/
@Service
public class TeacherInfoServiceJpaImpl implements ITeacherInfoService {

    @Autowired
    private TeacherInfoRepository repository;

    @Override
    public TeacherInfo findById(Long indexId) {
        return repository.findById(indexId).orElse(null);
    }

    @Override
    public List<TeacherInfo> findAll() {
        return repository.findAll();
    }

    @Override
    public TeacherInfo save(TeacherInfo entity) {
        return repository.save(entity);
    }

    @Override
    public List<TeacherInfo> save(List<TeacherInfo> entityList) {
        return repository.saveAll(entityList);
    }

    @Override
    public List<TeacherInfo> findByAddress(String address) {
        return repository.findByAddress(address);
    }

    @Override
    public List<TeacherInfo> findByAddressAndAge(String address, Integer age) {
        return repository.findByAddressAndAge(address, age);
    }

    @Override
    public List<TeacherInfo> findBySex(String sex) {
        return repository.findBySex(sex);
    }

}
