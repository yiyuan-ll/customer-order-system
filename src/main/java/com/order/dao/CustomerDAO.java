package com.order.dao;

import com.order.db.DatabaseManager;
import com.order.model.Customer;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {
    private Connection conn() { return DatabaseManager.getInstance().getConnection(); }

    public List<Customer> findAll() throws SQLException {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT c.*, " +
            "(SELECT COUNT(*) FROM orders o WHERE o.customer_id=c.customer_id) AS order_cnt, " +
            "(SELECT COALESCE(SUM(o.total_amount),0) FROM orders o WHERE o.customer_id=c.customer_id) AS total_amt " +
            "FROM customers c ORDER BY c.customer_id ASC";
        try (Statement st = conn().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapFull(rs));
        }
        return list;
    }

    public List<Customer> search(String keyword) throws SQLException {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT c.*, " +
            "(SELECT COUNT(*) FROM orders o WHERE o.customer_id=c.customer_id) AS order_cnt, " +
            "(SELECT COALESCE(SUM(o.total_amount),0) FROM orders o WHERE o.customer_id=c.customer_id) AS total_amt " +
            "FROM customers c WHERE c.name LIKE ? OR c.contact LIKE ? OR c.phone LIKE ? ORDER BY c.customer_id ASC";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            String k = "%" + keyword + "%";
            ps.setString(1, k); ps.setString(2, k); ps.setString(3, k);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(mapFull(rs)); }
        }
        return list;
    }

    public Customer findById(int id) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement("SELECT * FROM customers WHERE customer_id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return map(rs); }
        }
        return null;
    }

    public Customer findByName(String name) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement("SELECT * FROM customers WHERE name LIKE ? LIMIT 1")) {
            ps.setString(1, "%" + name + "%");
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return map(rs); }
        }
        return null;
    }

    public int insert(Customer c) throws SQLException {
        String sql = "INSERT INTO customers(name,contact,phone,email,address) VALUES(?,?,?,?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getName()); ps.setString(2, c.getContact());
            ps.setString(3, c.getPhone()); ps.setString(4, c.getEmail()); ps.setString(5, c.getAddress());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) { if (rs.next()) return rs.getInt(1); }
        }
        return -1;
    }

    public void update(Customer c) throws SQLException {
        String sql = "UPDATE customers SET name=?,contact=?,phone=?,email=?,address=? WHERE customer_id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, c.getName()); ps.setString(2, c.getContact());
            ps.setString(3, c.getPhone()); ps.setString(4, c.getEmail());
            ps.setString(5, c.getAddress()); ps.setInt(6, c.getCustomerId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement("DELETE FROM customers WHERE customer_id=?")) {
            ps.setInt(1, id); ps.executeUpdate();
        }
    }

    public int getTotalCount() throws SQLException {
        try (Statement st = conn().createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM customers")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private Customer map(ResultSet rs) throws SQLException {
        return new Customer(rs.getInt("customer_id"), rs.getString("name"),
            safeStr(rs, "contact"), rs.getString("phone"),
            rs.getString("email"), rs.getString("address"), rs.getString("created_at"));
    }

    private Customer mapFull(ResultSet rs) throws SQLException {
        Customer c = map(rs);
        try { c.setOrderCount(rs.getInt("order_cnt")); } catch (Exception ignored) {}
        try { c.setTotalAmount(rs.getDouble("total_amt")); } catch (Exception ignored) {}
        return c;
    }

    private String safeStr(ResultSet rs, String col) {
        try { return rs.getString(col); } catch (Exception e) { return ""; }
    }
}
