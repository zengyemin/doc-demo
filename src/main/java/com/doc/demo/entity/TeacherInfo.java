package com.doc.demo.entity;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

/**
 * @author : zengYeMin
 * @date : 2022/4/22 14:45
 **/
@Entity
@Table(name = "jap_teacher_info", indexes = {
    @Index(columnList = "address,age", name = "jap_teacher_info_index_address_age")})
public class TeacherInfo extends BaseInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long teacherId;

    @Column(columnDefinition = "varchar(20) comment '老师的学历'")
    private String education;

    @Column(precision = 2, columnDefinition = "double comment '老师的工资'")
    private Double wage;

    public TeacherInfo() {
        super.setCreateTime(new Date());
        super.setDeleteStatus(false);
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }

    public String getEducation() {
        return education;
    }

    public void setEducation(String education) {
        this.education = education;
    }

    public Double getWage() {
        return wage;
    }

    public void setWage(Double wage) {
        this.wage = wage;
    }
}
