package com.example.util;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

/**
 * JDBCTool —— 手写的极简 ORM 工具类
 *
 * 【这个类在干嘛？】
 * 平时我们写 JDBC 要手动写 "SELECT id,name,age FROM student"，
 * 然后再手动 rs.getInt("id")、rs.getString("name") 一个个塞进对象。
 *
 * 这个工具类要做的事：
 *   你只需要把一个 Java 对象（比如 Student）丢进来，
 *   它自己通过【反射】看这个对象有哪些字段，自动拼 SQL、自动把查询结果塞回对象。
 *   以后新增一张表，只要写个实体类，不用再写重复的 JDBC 代码。
 *
 * 【核心套路（MyBatis、Hibernate 也是这么干的）】
 *   类名 Student  ->  表名 student
 *   属性 enrollmentDate  ->  列名 enrollment_date
 *   属性 id  =  主键
 */
public class JDBCTool {

    private static String driver;
    private static String url;
    private static String username;
    private static String password;

    // 类加载时读一次 db.properties，只执行一次
    static {
        try (InputStream in = JDBCTool.class.getClassLoader().getResourceAsStream("db.properties")) {
            Properties p = new Properties();
            p.load(in);
            driver   = p.getProperty("jdbc.driver");
            url      = p.getProperty("jdbc.url");
            username = p.getProperty("jdbc.username");
            password = p.getProperty("jdbc.password");
            Class.forName(driver);
        } catch (Exception e) {
            throw new ExceptionInInitializerError("数据库配置加载失败: " + e.getMessage());
        }
    }

    /** 拿到一个数据库连接 */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    /** 安静地关闭资源（不抛异常） */
    public static void closeQuietly(AutoCloseable... resources) {
        for (AutoCloseable r : resources) {
            if (r != null) {
                try { r.close(); } catch (Exception ignored) {}
            }
        }
    }

    // ============================================================
    //  方法1：把 ResultSet 一行行转成对象列表（反射核心）
    // ============================================================
    public static <T> List<T> resultSetToList(ResultSet rs, Class<T> clazz) throws Exception {
        List<T> list = new ArrayList<>();
        ResultSetMetaData meta = rs.getMetaData();           // 拿到结果集的"表头信息"：有几列、列名是什么
        int columnCount = meta.getColumnCount();

        while (rs.next()) {                                   // 一行行往下走
            T obj = clazz.getDeclaredConstructor().newInstance(); // 反射调用无参构造器，new 一个空对象

            for (int i = 1; i <= columnCount; i++) {
                String columnName = meta.getColumnLabel(i);   // 例如 enrollment_date
                String fieldName  = columnToField(columnName); // 转成驼峰：enrollmentDate

                // 在类里找这个字段（包括私有字段）
                Field field = findField(clazz, fieldName);
                if (field == null) continue;
                field.setAccessible(true);                   // 破封装：允许给私有字段赋值

                Object value = rs.getObject(i, field.getType()); // 让 JDBC 自动把数据库类型转成 Java 类型
                field.set(obj, value);                       // 反射赋值：obj.field = value
            }
            list.add(obj);
        }
        return list;
    }

    // ============================================================
    //  方法2：保存（INSERT）
    // ============================================================
    public static <T> int save(T obj, Connection connection) throws Exception {
        Class<?> clazz = obj.getClass();
        String table = getTableName(clazz);                   // student

        List<Field> fields = getAllFields(clazz);
        List<String> columns = new ArrayList<>();
        List<Object> values  = new ArrayList<>();

        for (Field f : fields) {
            f.setAccessible(true);
            if ("id".equals(f.getName())) continue;           // 自增主键不插入
            columns.add(fieldToColumn(f.getName()));          // enrollmentDate -> enrollment_date
            values.add(f.get(obj));                           // 反射读对象属性值
        }

        String sql = "INSERT INTO " + table + " (" + String.join(",", columns) + ") VALUES ("
                   + String.join(",", Collections.nCopies(values.size(), "?")) + ")";

        // RETURN_GENERATED_KEYS：要求 MySQL 把自增生成的 id 返回来
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (int i = 0; i < values.size(); i++) {
                ps.setObject(i + 1, values.get(i));
            }
            int rows = ps.executeUpdate();

            // 把自增 id 回填到对象里
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                Field idField = findField(clazz, "id");
                if (idField != null) {
                    idField.setAccessible(true);
                    idField.set(obj, keys.getInt(1));
                }
            }
            return rows;
        }
    }

    // ============================================================
    //  方法3：更新（UPDATE）  SET 每个字段 = ?  WHERE id = ?
    // ============================================================
    public static <T> int update(T obj, Connection connection) throws Exception {
        Class<?> clazz = obj.getClass();
        String table = getTableName(clazz);

        List<Field> fields = getAllFields(clazz);
        List<String> setClauses = new ArrayList<>();
        List<Object> values = new ArrayList<>();
        Object idValue = null;

        for (Field f : fields) {
            f.setAccessible(true);
            if ("id".equals(f.getName())) {
                idValue = f.get(obj);                        // 主键值，留作 WHERE 条件
                continue;
            }
            setClauses.add(fieldToColumn(f.getName()) + " = ?");
            values.add(f.get(obj));
        }
        values.add(idValue);                               // WHERE id = ?

        String sql = "UPDATE " + table + " SET " + String.join(",", setClauses) + " WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (int i = 0; i < values.size(); i++) {
                ps.setObject(i + 1, values.get(i));
            }
            return ps.executeUpdate();
        }
    }

    // ============================================================
    //  方法4：删除（DELETE）  WHERE id = ?
    // ============================================================
    public static <T> int delete(T obj, Connection connection) throws Exception {
        Class<?> clazz = obj.getClass();
        String table = getTableName(clazz);
        Field idField = findField(clazz, "id");
        idField.setAccessible(true);
        Object idValue = idField.get(obj);

        String sql = "DELETE FROM " + table + " WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setObject(1, idValue);
            return ps.executeUpdate();
        }
    }

    // ============================================================
    //  方法5：按主键查一条
    // ============================================================
    public static <T> T getOneById(String id, Class<T> clazz, Connection connection) throws Exception {
        String table = getTableName(clazz);
        String sql = "SELECT * FROM " + table + " WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setObject(1, Integer.valueOf(id));
            ResultSet rs = ps.executeQuery();
            List<T> list = resultSetToList(rs, clazz);
            return list.isEmpty() ? null : list.get(0);
        }
    }

    // ============================================================
    //  辅助方法：下面这些都是给上面 5 个方法打杂用的
    // ============================================================

    /** 类名首字母变小写：Student -> student */
    private static String getTableName(Class<?> clazz) {
        String simple = clazz.getSimpleName();
        return Character.toLowerCase(simple.charAt(0)) + simple.substring(1);
    }

    /** 驼峰转下划线：enrollmentDate -> enrollment_date */
    private static String fieldToColumn(String fieldName) {
        StringBuilder sb = new StringBuilder();
        for (char c : fieldName.toCharArray()) {
            if (Character.isUpperCase(c)) {
                sb.append('_').append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /** 下划线转驼峰：enrollment_date -> enrollmentDate */
    private static String columnToField(String columnName) {
        StringBuilder sb = new StringBuilder();
        boolean nextUpper = false;
        for (char c : columnName.toCharArray()) {
            if (c == '_') {
                nextUpper = true;
            } else if (nextUpper) {
                sb.append(Character.toUpperCase(c));
                nextUpper = false;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /** 找一个类里的字段（含父类，本作业实体类没继承父类，留作通用） */
    private static Field findField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        Class<?> c = clazz;
        while (c != Object.class) {
            try {
                return c.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                c = c.getSuperclass();
            }
        }
        throw new NoSuchFieldException(fieldName);
    }

    /** 拿到类里所有声明的字段 */
    private static List<Field> getAllFields(Class<?> clazz) {
        List<Field> list = new ArrayList<>();
        Class<?> c = clazz;
        while (c != Object.class) {
            list.addAll(Arrays.asList(c.getDeclaredFields()));
            c = c.getSuperclass();
        }
        return list;
    }
}
