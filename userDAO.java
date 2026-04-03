package database_setup;

import java.sql.*;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.*;

public class UserDAO {

    private String lastUsername = "";

    public int[] authenticate(String username, String password) {
        Connection conn = null; PreparedStatement ps = null; ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(
                "SELECT user_id, username FROM users WHERE username = ? AND password = ?");
            ps.setString(1, username);
            ps.setString(2, password);
            rs = ps.executeQuery();
            if (rs.next()) {
                lastUsername = rs.getString("username");
                return new int[]{ rs.getInt("user_id") };
            }
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBConnection.close(conn, ps, rs); }
        return null;
    }

    public String getLastUsername() { return lastUsername; }

    public boolean registerUser(String username, String password,
                                String email, String fullName) {
        Connection conn = null; PreparedStatement ps = null;
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(
                "INSERT INTO users (username, password, email, full_name) VALUES (?,?,?,?)");
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, email);
            ps.setString(4, fullName);
            return ps.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException e) {
            return false;
        } catch (SQLException e) {
            e.printStackTrace(); return false;
        } finally { DBConnection.close(conn, ps, null); }
    }

    // Used by RegisterDialog
    boolean register(String name, String phone, String email, String pwd) {
        Connection conn = null; PreparedStatement ps = null;
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(
                "INSERT INTO users (username, password, email, full_name) VALUES (?,?,?,?)");
            ps.setString(1, name);
            ps.setString(2, pwd);
            ps.setString(3, email);
            ps.setString(4, phone);
            return ps.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException e) {
            return false;
        } catch (SQLException e) {
            e.printStackTrace(); return false;
        } finally { DBConnection.close(conn, ps, null); }
    }

    public String getUsername(int userId) {
        Connection conn = null; PreparedStatement ps = null; ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(
                "SELECT username FROM users WHERE user_id = ?");
            ps.setInt(1, userId);
            rs = ps.executeQuery();
            if (rs.next()) return rs.getString(1);
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBConnection.close(conn, ps, rs); }
        return "User";
    }

    // Used by ForgotPasswordDialog
    public boolean emailExists(String email) {
        Connection conn = null; PreparedStatement ps = null; ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(
                "SELECT 1 FROM users WHERE email = ?");
            ps.setString(1, email);
            rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) { e.printStackTrace(); return false; }
        finally { DBConnection.close(conn, ps, rs); }
    }

    // Used by ForgotPasswordDialog
    public boolean resetPassword(String email, String newPassword) {
        Connection conn = null; PreparedStatement ps = null;
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(
                "UPDATE users SET password = ? WHERE email = ?");
            ps.setString(1, newPassword);
            ps.setString(2, email);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
        finally { DBConnection.close(conn, ps, null); }
    }

    // Used by OrderTrackingDialog
    public List<String[]> getOrderTracking(int userId) {
        List<String[]> rows = new ArrayList<>();
        Connection conn = null; PreparedStatement ps = null; ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(
                "SELECT o.order_id, o.order_date, o.total_amount, o.status, " +
                "COUNT(oi.order_item_id) AS item_count " +
                "FROM orders o " +
                "LEFT JOIN order_items oi ON o.order_id = oi.order_id " +
                "WHERE o.user_id = ? " +
                "GROUP BY o.order_id, o.order_date, o.total_amount, o.status " +
                "ORDER BY o.order_date DESC");
            ps.setInt(1, userId);
            rs = ps.executeQuery();
            while (rs.next()) {
                rows.add(new String[]{
                    String.valueOf(rs.getInt("order_id")),
                    rs.getString("order_date"),
                    String.format("%,.0f", rs.getDouble("total_amount")),
                    rs.getString("status"),
                    String.valueOf(rs.getInt("item_count"))
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBConnection.close(conn, ps, rs); }
        return rows;
    }
}
