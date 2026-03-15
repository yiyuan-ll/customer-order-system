package com.order.dao;

import com.order.db.DatabaseManager;
import com.order.model.Invoice;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class InvoiceDAO {
    private Connection conn() { return DatabaseManager.getInstance().getConnection(); }

    public List<Invoice> findAll() throws SQLException {
        return query("SELECT i.*, c.name AS customer_name FROM invoices i " +
            "JOIN orders o ON i.order_id=o.order_id " +
            "JOIN customers c ON o.customer_id=c.customer_id ORDER BY i.invoice_id DESC", null);
    }

    public List<Invoice> findByStatus(String status) throws SQLException {
        List<Invoice> list = new ArrayList<>();
        String sql = "SELECT i.*, c.name AS customer_name FROM invoices i " +
            "JOIN orders o ON i.order_id=o.order_id " +
            "JOIN customers c ON o.customer_id=c.customer_id WHERE i.status=? ORDER BY i.invoice_id DESC";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(map(rs)); }
        }
        return list;
    }

    public List<Invoice> search(String keyword) throws SQLException {
        List<Invoice> list = new ArrayList<>();
        String sql = "SELECT i.*, c.name AS customer_name FROM invoices i " +
            "JOIN orders o ON i.order_id=o.order_id " +
            "JOIN customers c ON o.customer_id=c.customer_id " +
            "WHERE i.invoice_no LIKE ? OR c.name LIKE ? OR CAST(i.order_id AS TEXT) LIKE ? ORDER BY i.invoice_id DESC";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            String k = "%" + keyword + "%";
            ps.setString(1, k); ps.setString(2, k); ps.setString(3, k);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(map(rs)); }
        }
        return list;
    }

    public boolean existsForOrder(int orderId) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement("SELECT 1 FROM invoices WHERE order_id=?")) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    public int insert(Invoice inv) throws SQLException {
        String sql = "INSERT INTO invoices(order_id,invoice_no,amount,tax_rate,tax_amount,total_amount,status) VALUES(?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, inv.getOrderId()); ps.setString(2, inv.getInvoiceNo());
            ps.setDouble(3, inv.getAmount()); ps.setDouble(4, inv.getTaxRate());
            ps.setDouble(5, inv.getTaxAmount()); ps.setDouble(6, inv.getTotalAmount());
            ps.setString(7, inv.getStatus());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) { if (rs.next()) return rs.getInt(1); }
        }
        return -1;
    }

    public void updateStatus(int invoiceId, String status) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement("UPDATE invoices SET status=? WHERE invoice_id=?")) {
            ps.setString(1, status); ps.setInt(2, invoiceId); ps.executeUpdate();
        }
    }

    public void delete(int invoiceId) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement("DELETE FROM invoices WHERE invoice_id=?")) {
            ps.setInt(1, invoiceId); ps.executeUpdate();
        }
    }

    /** 统计 */
    public double sumByStatus(String status) throws SQLException {
        String sql = "SELECT COALESCE(SUM(total_amount),0) FROM invoices WHERE status=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getDouble(1) : 0; }
        }
    }

    public int countByStatus(String status) throws SQLException {
        String sql = "SELECT COUNT(*) FROM invoices WHERE status=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getInt(1) : 0; }
        }
    }

    public String generateInvoiceNo() {
        return "INV" + new SimpleDateFormat("yyyyMMdd").format(new Date()) + String.format("%04d", System.currentTimeMillis() % 10000);
    }

    private List<Invoice> query(String sql, String param) throws SQLException {
        List<Invoice> list = new ArrayList<>();
        if (param == null) {
            try (Statement st = conn().createStatement(); ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    private Invoice map(ResultSet rs) throws SQLException {
        Invoice inv = new Invoice();
        inv.setInvoiceId(rs.getInt("invoice_id")); inv.setOrderId(rs.getInt("order_id"));
        try { inv.setCustomerName(rs.getString("customer_name")); } catch (Exception ignored) {}
        inv.setInvoiceNo(rs.getString("invoice_no")); inv.setIssueDate(rs.getString("issue_date"));
        inv.setAmount(rs.getDouble("amount")); inv.setTaxRate(rs.getDouble("tax_rate"));
        inv.setTaxAmount(rs.getDouble("tax_amount")); inv.setTotalAmount(rs.getDouble("total_amount"));
        inv.setStatus(rs.getString("status"));
        return inv;
    }
}
