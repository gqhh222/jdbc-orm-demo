package com.example.model;

import java.time.LocalDate;

/**
 * 学生实体类 —— 对应数据库 student 表
 *
 * 字段映射：
 *   id              -> id
 *   name            -> name
 *   major           -> major
 *   age             -> age
 *   enrollmentDate  -> enrollment_date（驼峰转下划线）
 *   graduated       -> graduated
 *   tuition         -> tuition
 */
public class Student {
    private Integer id;              // 学号
    private String  name;            // 姓名
    private String  major;           // 专业
    private Integer age;             // 年龄
    private LocalDate enrollmentDate; // 入学时间
    private Boolean   graduated;     // 是否毕业
    private Double    tuition;       // 学费

    public Student() {}

    public Student(String name, String major, Integer age,
                   LocalDate enrollmentDate, Boolean graduated, Double tuition) {
        this.name = name;
        this.major = major;
        this.age = age;
        this.enrollmentDate = enrollmentDate;
        this.graduated = graduated;
        this.tuition = tuition;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getMajor() { return major; }
    public void setMajor(String major) { this.major = major; }
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    public LocalDate getEnrollmentDate() { return enrollmentDate; }
    public void setEnrollmentDate(LocalDate enrollmentDate) { this.enrollmentDate = enrollmentDate; }
    public Boolean getGraduated() { return graduated; }
    public void setGraduated(Boolean graduated) { this.graduated = graduated; }
    public Double getTuition() { return tuition; }
    public void setTuition(Double tuition) { this.tuition = tuition; }

    @Override
    public String toString() {
        return "Student{id=" + id + ", name='" + name + "', major='" + major + "', age=" + age +
                ", enrollmentDate=" + enrollmentDate + ", graduated=" + graduated + ", tuition=" + tuition + "}";
    }
}
