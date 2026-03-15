package com.order.db;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;

public class DatabaseManager {

    private static final String DB_FILE = "order_system.db";
    private static DatabaseManager instance;
    private Connection connection;

    private DatabaseManager() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + DB_FILE);
            connection.setAutoCommit(true);
            // Enable WAL for better performance
            try (Statement st = connection.createStatement()) {
                st.execute("PRAGMA journal_mode=WAL");
                st.execute("PRAGMA foreign_keys=ON");
            }
            initDatabase();
        } catch (Exception e) {
            throw new RuntimeException("数据库初始化失败: " + e.getMessage(), e);
        }
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) instance = new DatabaseManager();
        return instance;
    }

    public Connection getConnection() { return connection; }

    private void initDatabase() throws Exception {
        String sql = loadInitSQL();
        try (Statement stmt = connection.createStatement()) {
            String[] statements = sql.split(";");
            for (String s : statements) {
                s = s.trim().replaceAll("--[^\n]*", "").trim();
                if (!s.isEmpty() && !s.toUpperCase().startsWith("INSERT")) {
                    stmt.execute(s);
                }
            }
        }
        // Try to add category column if upgrading from v1
        try (Statement st = connection.createStatement()) {
            st.execute("ALTER TABLE goods ADD COLUMN category TEXT DEFAULT '其他'");
        } catch (SQLException ignored) {} // Column already exists
        try (Statement st = connection.createStatement()) {
            st.execute("ALTER TABLE customers ADD COLUMN contact TEXT");
        } catch (SQLException ignored) {}

        // Insert sample data only if empty
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM customers")) {
            if (rs.next() && rs.getInt(1) == 0) insertSampleData();
        }
    }

    private void insertSampleData() throws SQLException {
        try (Statement st = connection.createStatement()) {
            st.execute("INSERT INTO customers(name,contact,phone,email,address) VALUES('张三贸易有限公司','张三','13800138001','zhangsan@trade.com','北京市朝阳区建国路1号')");
            st.execute("INSERT INTO customers(name,contact,phone,email,address) VALUES('李五游戏公司','李五','13800138002','liwu@games.com','上海市浦东新区张江路2号')");
            st.execute("INSERT INTO customers(name,contact,phone,email,address) VALUES('蛋蛋电子商务','王蛋蛋','13800138003','dandan@ecom.com','广州市天河区天河路3号')");
            st.execute("INSERT INTO customers(name,contact,phone,email,address) VALUES('宏达科技集团','陈宏达','13800138004','hongda@tech.com','深圳市南山区科技园4号')");
            st.execute("INSERT INTO customers(name,contact,phone,email,address) VALUES('明辉进出口贸易','刘明辉','13800138005','minghui@import.com','杭州市西湖区文三路5号')");

            st.execute("INSERT INTO goods(goods_name,category,unit,unit_price,stock,description) VALUES('笔记本电脑','电子设备','台',5999.00,50,'Intel i5, 16GB RAM, 512GB SSD')");
            st.execute("INSERT INTO goods(goods_name,category,unit,unit_price,stock,description) VALUES('无线鼠标','电脑配件','个',199.00,200,'2.4G无线，电池供电')");
            st.execute("INSERT INTO goods(goods_name,category,unit,unit_price,stock,description) VALUES('机械键盘','电脑配件','个',599.00,100,'青轴，104键')");
            st.execute("INSERT INTO goods(goods_name,category,unit,unit_price,stock,description) VALUES('显示器27寸','电子设备','台',2199.00,30,'4K IPS屏，HDMI接口')");
            st.execute("INSERT INTO goods(goods_name,category,unit,unit_price,stock,description) VALUES('USB集线器','电脑配件','个',89.00,300,'USB 3.0，4口')");
            st.execute("INSERT INTO goods(goods_name,category,unit,unit_price,stock,description) VALUES('移动硬盘1TB','存储设备','个',399.00,80,'USB 3.0，便携式')");
        }
    }

    private String loadInitSQL() {
        InputStream is = getClass().getClassLoader().getResourceAsStream("init.sql");
        if (is == null) return "";
        try (BufferedReader r = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) sb.append(line).append("\n");
            return sb.toString();
        } catch (IOException e) { return ""; }
    }

    public void close() {
        try { if (connection != null && !connection.isClosed()) connection.close(); }
        catch (SQLException ignored) {}
    }
}
