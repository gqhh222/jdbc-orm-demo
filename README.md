# JDBC ORM 作业说明

## 项目结构
```
jdbc-orm-demo/
├── pom.xml                        # Maven 配置，引入 mysql-connector-j
├── sql/init.sql                   # 建库建表脚本
├── src/main/resources/db.properties  # 数据库连接配置
└── src/main/java/com/example/
    ├── Main.java                  # 主程序：演示增删改查
    ├── model/
    │   ├── Student.java           # 学生实体（对应 student 表）
    │   └── College.java           # 学院实体（对应 college 表）
    └── util/
        └── JDBCTool.java          # 核心：手写 ORM 工具类
```

## 运行方式
1. 先用 MySQL 执行 `sql/init.sql` 建库建表
2. 修改 `src/main/resources/db.properties` 里的密码为你本机 MySQL 密码
3. IDEA 打开本项目，等 Maven 下载完依赖
4. 运行 `Main.java`

## JDBCTool 5 个核心方法
| 方法 | 作用 |
|---|---|
| `resultSetToList(rs, clazz)` | 用反射把 ResultSet 每行转成对象 |
| `save(obj, conn)` | INSERT，并回填自增主键 |
| `update(obj, conn)` | UPDATE，按 id 更新 |
| `delete(obj, conn)` | DELETE，按 id 删除 |
| `getOneById(id, clazz, conn)` | 按主键查一条 |

## 原理一句话
类名 `Student` → 表名 `student`；属性 `enrollmentDate` → 列名 `enrollment_date`。
通过反射读对象字段名拼 SQL，再通过反射把查询结果塞回对象属性——这就是 MyBatis、Hibernate 在干的事。
