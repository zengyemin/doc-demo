package com.doc.demo.repository.jpa;

import com.doc.demo.entity.TeacherInfo;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author : zengYeMin
 * @date : 2022/4/22 15:43
 **/
public interface TeacherInfoRepository extends JpaRepository<TeacherInfo, Long> {

    List<TeacherInfo> findByAddress(String address);

    List<TeacherInfo> findByAddressAndAge(String address, Integer age);

    List<TeacherInfo> findBySex(String sex);
}
