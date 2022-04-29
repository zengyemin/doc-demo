package com.doc.demo.entity;

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

/**
 * @author : zengYeMin
 * @date : 2022/4/6 17:39
 **/
@Entity
@Table(name = "jap_student_info", indexes = {
    @Index(columnList = "teacherName", name = "jap_student_info_teacher_name")})
public class StudentInfo extends BaseInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long studentId;

    @Column(columnDefinition = "varchar(20)  comment '学生的父亲/母亲'")
    private String parent;

    @Column(nullable = false, columnDefinition = "varchar(30) comment '学生的名字'")
    private String studentName;

    @Column(columnDefinition = "text COMMENT '学生的简介'")
    private String introduction;

    @Column(columnDefinition = "tinyint(4) comment '是否毕业,0表示在读,1表示毕业'")
    private int graduate;

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public int getGraduate() {
        return graduate;
    }

    public void setGraduate(int graduate) {
        this.graduate = graduate;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        StudentInfo info = (StudentInfo) o;
        return Objects.equals(studentName, info.studentName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentName);
    }
}
