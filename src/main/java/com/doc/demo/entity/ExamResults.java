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
 * @date : 2022/4/22 15:13
 **/
@Entity
@Table(name = "jap_exam_results", indexes = {
    @Index(columnList = "studentName", name = "jap_exam_results_student_name")})
public class ExamResults {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long erId;

    @Column(columnDefinition = "int default 0 comment '考试分数'")
    private Integer fraction;

    @Column(columnDefinition = "datetime default now() comment '考试时间'")
    private Date examTime;

    @Column(columnDefinition = "varchar(40) default '' comment '考试内容'")
    private String content;

    @Column(nullable = false, columnDefinition = "varchar(40) default '' comment '学生名字'")
    private String studentName;

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public Long getErId() {
        return erId;
    }

    public void setErId(Long erId) {
        this.erId = erId;
    }

    public Integer getFraction() {
        return fraction;
    }

    public void setFraction(Integer fraction) {
        this.fraction = fraction;
    }

    public Date getExamTime() {
        return examTime;
    }

    public void setExamTime(Date examTime) {
        this.examTime = examTime;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    // public String getStudentName() {
    //     return studentName;
    // }
    //
    // public void setStudentName(String studentName) {
    //     this.studentName = studentName;
    // }
}
