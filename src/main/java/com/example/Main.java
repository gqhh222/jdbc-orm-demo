package com.example;

import com.example.model.College;
import com.example.model.Student;
import com.example.util.JDBCTool;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;

/**
 * 主程序：演示 JDBCTool 对两张表做完整的增删改查
 */
public class Main {
    public static void main(String[] args) throws Exception {
        // 强制控制台以 UTF-8 输出，解决 Windows 中文乱码
        System.setOut(new java.io.PrintStream(System.out, true, java.nio.charset.StandardCharsets.UTF_8));
        Connection conn = JDBCTool.getConnection();
        System.out.println("===== 数据库连接成功 =====");

        // ---------- 1. 查所有学院 ----------
        System.out.println("\n[1] 查询所有学院:");
        List<College> colleges = JDBCTool.resultSetToList(
                conn.createStatement().executeQuery("SELECT * FROM college"), College.class);
        colleges.forEach(System.out::println);

        // ---------- 2. 新增一个学生 ----------
        System.out.println("\n[2] 新增学生 赵六:");
        Student s = new Student("赵六", "数据科学", 19, LocalDate.of(2025, 9, 1), false, 6200.0);
        int rows = JDBCTool.save(s, conn);
        System.out.println("影响行数=" + rows + "，自增id=" + s.getId());

        // ---------- 3. 按 id 查刚插入的学生 ----------
        System.out.println("\n[3] 按id=" + s.getId() + " 查询学生:");
        Student byId = JDBCTool.getOneById(String.valueOf(s.getId()), Student.class, conn);
        System.out.println(byId);

        // ---------- 4. 修改这个学生的学费 ----------
        System.out.println("\n[4] 修改学费为 6800:");
        byId.setTuition(6800.0);
        System.out.println("影响行数=" + JDBCTool.update(byId, conn));
        System.out.println(JDBCTool.getOneById(String.valueOf(byId.getId()), Student.class, conn));

        // ---------- 5. 查询所有学生 ----------
        System.out.println("\n[5] 查询所有学生:");
        List<Student> students = JDBCTool.resultSetToList(
                conn.createStatement().executeQuery("SELECT * FROM student"), Student.class);
        students.forEach(System.out::println);

        // ---------- 6. 删除刚插入的学生 ----------
        System.out.println("\n[6] 删除学生 id=" + byId.getId() + ":");
        System.out.println("影响行数=" + JDBCTool.delete(byId, conn));

        // ---------- 7. 新增并删除一个学院（演示第二张表也能用） ----------
        System.out.println("\n[7] 新增并删除一个学院:");
        College c = new College("外国语学院", "CFL");
        JDBCTool.save(c, conn);
        System.out.println("新增后: " + c);
        JDBCTool.delete(c, conn);
        System.out.println("已删除");

        JDBCTool.closeQuietly(conn);
        System.out.println("\n===== 全部操作完成，连接已关闭 =====");
    }
}
