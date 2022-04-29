package com.doc.demo.entity;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

/**
 * @author : zengYeMin
 * @date : 2022/4/22 14:57
 **/
@MappedSuperclass
public class BaseInfo {

    @Column(columnDefinition = "datetime default now() comment '创建时间'")
    private Date createTime;

    @Column(nullable = false, columnDefinition = "varchar(20) comment '老师名字'")
    private String teacherName;

    @Column(columnDefinition = "bit default false comment '是否删除'")
    private Boolean deleteStatus;

    @Column(nullable = false, columnDefinition = "varchar(30) comment '居住地址'")
    private String address;

    @Column(nullable = false, columnDefinition = "int(6) comment '年龄'")
    private Integer age;

    @Column(nullable = false, columnDefinition = "varchar(3) comment '性别'")
    private String sex;

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public Boolean getDeleteStatus() {
        return deleteStatus;
    }

    public void setDeleteStatus(Boolean deleteStatus) {
        this.deleteStatus = deleteStatus;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
