package com.order.dao;

import com.order.db.DatabaseManager;
import com.order.model.Goods;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GoodsDAO {
    private Connection conn() { return DatabaseManager.getInstance().getConnection(); }

    public List<Goods> findAll() throws SQLException {
        List<Goods> list = new ArrayList<>();
        String sql = "SELECT g.*, COALESCE((SELECT SUM(oi.quantity) FROM order_items oi WHERE oi.goods_id=g.goods_id),0) AS sold_qty " +
                     "FROM goods g ORDER BY g.goods_id ASC";
        try (Statement st = conn().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapFull(rs));
        }
        return list;
    }

    public List<Goods> findByCategory(String category) throws SQLException {
        List<Goods> list = new ArrayList<>();
        String sql = "SELECT g.*, COALESCE((SELECT SUM(oi.quantity) FROM order_items oi WHERE oi.goods_id=g.goods_id),0) AS sold_qty " +
                     "FROM goods g WHERE g.category=? ORDER BY g.goods_id ASC";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, category);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(mapFull(rs)); }
        }
        return list;
    }

    public List<Goods> search(String keyword) throws SQLException {
        List<Goods> list = new ArrayList<>();
        String sql = "SELECT g.*, COALESCE((SELECT SUM(oi.quantity) FROM order_items oi WHERE oi.goods_id=g.goods_id),0) AS sold_qty " +
                     "FROM goods g WHERE g.goods_name LIKE ? OR g.category LIKE ? OR g.description LIKE ? ORDER BY g.goods_id ASC";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            String k = "%" + keyword + "%";
            ps.setString(1, k); ps.setString(2, k); ps.setString(3, k);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(mapFull(rs)); }
        }
        return list;
    }

    public Goods findById(int id) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement("SELECT * FROM goods WHERE goods_id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return map(rs); }
        }
        return null;
    }

    public Goods findByName(String name) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement("SELECT * FROM goods WHERE goods_name LIKE ? LIMIT 1")) {
            ps.setString(1, "%" + name + "%");
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return map(rs); }
        }
        return null;
    }

    public List<String> getCategories() throws SQLException {
        List<String> cats = new ArrayList<>();
        try (Statement st = conn().createStatement();
             ResultSet rs = st.executeQuery("SELECT DISTINCT category FROM goods WHERE category IS NOT NULL ORDER BY category")) {
            while (rs.next()) cats.add(rs.getString(1));
        }
        return cats;
    }

    public int insert(Goods g) throws SQLException {
        String sql = "INSERT INTO goods(goods_name,category,unit,unit_price,stock,description) VALUES(?,?,?,?,?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, g.getGoodsName()); ps.setString(2, g.getCategory());
            ps.setString(3, g.getUnit()); ps.setDouble(4, g.getUnitPrice());
            ps.setInt(5, g.getStock()); ps.setString(6, g.getDescription());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) { if (rs.next()) return rs.getInt(1); }
        }
        return -1;
    }

    public void update(Goods g) throws SQLException {
        String sql = "UPDATE goods SET goods_name=?,category=?,unit=?,unit_price=?,stock=?,description=? WHERE goods_id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, g.getGoodsName()); ps.setString(2, g.getCategory());
            ps.setString(3, g.getUnit()); ps.setDouble(4, g.getUnitPrice());
            ps.setInt(5, g.getStock()); ps.setString(6, g.getDescription()); ps.setInt(7, g.getGoodsId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement("DELETE FROM goods WHERE goods_id=?")) {
            ps.setInt(1, id); ps.executeUpdate();
        }
    }

    public int getTotalCount() throws SQLException {
        try (Statement st = conn().createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM goods")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private Goods map(ResultSet rs) throws SQLException {
        String cat = "";
        try { cat = rs.getString("category"); } catch (Exception ignored) {}
        return new Goods(rs.getInt("goods_id"), rs.getString("goods_name"), cat,
            rs.getString("unit"), rs.getDouble("unit_price"),
            rs.getInt("stock"), rs.getString("description"), rs.getString("created_at"));
    }

    private Goods mapFull(ResultSet rs) throws SQLException {
        Goods g = map(rs);
        try { g.setSoldQty(rs.getInt("sold_qty")); } catch (Exception ignored) {}
        return g;
    }
}
