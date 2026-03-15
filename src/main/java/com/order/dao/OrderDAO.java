package com.order.dao;

import com.order.db.DatabaseManager;
import com.order.model.Order;
import com.order.model.OrderItem;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {
    private Connection conn() { return DatabaseManager.getInstance().getConnection(); }

    public List<Order> findAll() throws SQLException {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT o.*, c.name AS customer_name FROM orders o " +
                     "LEFT JOIN customers c ON o.customer_id=c.customer_id ORDER BY o.order_id DESC";
        try (Statement st = conn().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapOrder(rs));
        }
        return list;
    }

    public List<Order> search(String keyword) throws SQLException {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT o.*, c.name AS customer_name FROM orders o " +
                     "LEFT JOIN customers c ON o.customer_id=c.customer_id " +
                     "WHERE c.name LIKE ? OR o.status LIKE ? OR CAST(o.order_id AS TEXT) LIKE ? ORDER BY o.order_id DESC";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            String k = "%" + keyword + "%";
            ps.setString(1, k); ps.setString(2, k); ps.setString(3, k);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(mapOrder(rs)); }
        }
        return list;
    }

    public Order findById(int orderId) throws SQLException {
        String sql = "SELECT o.*, c.name AS customer_name FROM orders o " +
                     "LEFT JOIN customers c ON o.customer_id=c.customer_id WHERE o.order_id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Order order = mapOrder(rs);
                    order.setItems(findItems(orderId));
                    return order;
                }
            }
        }
        return null;
    }

    public List<OrderItem> findItems(int orderId) throws SQLException {
        List<OrderItem> items = new ArrayList<>();
        String sql = "SELECT oi.*, g.goods_name, g.unit FROM order_items oi " +
                     "LEFT JOIN goods g ON oi.goods_id=g.goods_id WHERE oi.order_id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderItem item = new OrderItem();
                    item.setItemId(rs.getInt("item_id")); item.setOrderId(rs.getInt("order_id"));
                    item.setGoodsId(rs.getInt("goods_id")); item.setGoodsName(rs.getString("goods_name"));
                    item.setUnit(rs.getString("unit")); item.setQuantity(rs.getInt("quantity"));
                    item.setUnitPrice(rs.getDouble("unit_price")); item.setSubtotal(rs.getDouble("subtotal"));
                    items.add(item);
                }
            }
        }
        return items;
    }

    /** 插入订单（含明细），使用事务 */
    public int insert(Order order) throws SQLException {
        conn().setAutoCommit(false);
        try {
            String sql = "INSERT INTO orders(customer_id,total_amount,status,remark) VALUES(?,?,?,?)";
            int orderId;
            try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, order.getCustomerId()); ps.setDouble(2, order.getTotalAmount());
                ps.setString(3, order.getStatus()); ps.setString(4, order.getRemark());
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) { rs.next(); orderId = rs.getInt(1); }
            }
            insertItems(orderId, order.getItems());
            conn().commit();
            return orderId;
        } catch (SQLException e) {
            conn().rollback(); throw e;
        } finally {
            conn().setAutoCommit(true);
        }
    }

    private void insertItems(int orderId, List<OrderItem> items) throws SQLException {
        String sql = "INSERT INTO order_items(order_id,goods_id,quantity,unit_price,subtotal) VALUES(?,?,?,?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            for (OrderItem item : items) {
                ps.setInt(1, orderId); ps.setInt(2, item.getGoodsId());
                ps.setInt(3, item.getQuantity()); ps.setDouble(4, item.getUnitPrice());
                ps.setDouble(5, item.getSubtotal()); ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public void updateStatus(int orderId, String status) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement("UPDATE orders SET status=? WHERE order_id=?")) {
            ps.setString(1, status); ps.setInt(2, orderId); ps.executeUpdate();
        }
    }

    public void updateTotalAmount(int orderId, double amount) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement("UPDATE orders SET total_amount=? WHERE order_id=?")) {
            ps.setDouble(1, amount); ps.setInt(2, orderId); ps.executeUpdate();
        }
    }

    public void delete(int orderId) throws SQLException {
        conn().setAutoCommit(false);
        try {
            try (PreparedStatement ps = conn().prepareStatement("DELETE FROM order_items WHERE order_id=?")) {
                ps.setInt(1, orderId); ps.executeUpdate();
            }
            try (PreparedStatement ps = conn().prepareStatement("DELETE FROM orders WHERE order_id=?")) {
                ps.setInt(1, orderId); ps.executeUpdate();
            }
            conn().commit();
        } catch (SQLException e) { conn().rollback(); throw e; }
        finally { conn().setAutoCommit(true); }
    }

    // 统计：按客户汇总订单数和金额
    public List<Object[]> statByCustomer() throws SQLException {
        List<Object[]> rows = new ArrayList<>();
        String sql = "SELECT c.name, COUNT(o.order_id) AS cnt, SUM(o.total_amount) AS total " +
                     "FROM orders o JOIN customers c ON o.customer_id=c.customer_id " +
                     "GROUP BY o.customer_id ORDER BY total DESC";
        try (Statement st = conn().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) rows.add(new Object[]{rs.getString(1), rs.getInt(2), rs.getDouble(3)});
        }
        return rows;
    }

    // 统计：按货物汇总订购数量和金额
    public List<Object[]> statByGoods() throws SQLException {
        List<Object[]> rows = new ArrayList<>();
        String sql = "SELECT g.goods_name, SUM(oi.quantity) AS qty, SUM(oi.subtotal) AS total " +
                     "FROM order_items oi JOIN goods g ON oi.goods_id=g.goods_id " +
                     "GROUP BY oi.goods_id ORDER BY total DESC";
        try (Statement st = conn().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) rows.add(new Object[]{rs.getString(1), rs.getInt(2), rs.getDouble(3)});
        }
        return rows;
    }

    private Order mapOrder(ResultSet rs) throws SQLException {
        Order o = new Order();
        o.setOrderId(rs.getInt("order_id")); o.setCustomerId(rs.getInt("customer_id"));
        o.setCustomerName(rs.getString("customer_name")); o.setOrderDate(rs.getString("order_date"));
        o.setTotalAmount(rs.getDouble("total_amount")); o.setStatus(rs.getString("status"));
        o.setRemark(rs.getString("remark"));
        return o;
    }
}
