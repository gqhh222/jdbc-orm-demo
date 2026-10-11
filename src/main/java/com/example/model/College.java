package com.example.model;

/**
 * 学院实体类 —— 对应数据库 college 表
 *
 * ORM 的约定：
 *   Java 类名  Student  ->  数据库表名  student（首字母变小写）
 *   Java 属性  userName ->  数据库列名  user_name（驼峰转下划线）
 */
public class College {
    private Integer id;       // 对应列 id
    private String  name;     // 对应列 name
    private String  code;     // 对应列 code

    // 空构造方法必须有：JDBCTool 用反射 newInstance() 创建对象时要调用它
    public College() {}

    public College(String name, String code) {
        this.name = name;
        this.code = code;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    @Override
    public String toString() {
        return "College{id=" + id + ", name='" + name + "', code='" + code + "'}";
    }
}
