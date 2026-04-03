package database_setup;

import java.sql.*;
import java.util.*;

public class AdminDAO {

    public boolean isAdmin(int userId) {
        Connection conn = null; PreparedStatement ps = null; ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(
                "SELECT 1 FROM admins WHERE user_id = ?");
            ps.setInt(1, userId);
            rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DBConnection.close(conn, ps, rs);
        }
    }

    public List<String[]> getAllUsers() {
        List<String[]> users = new ArrayList<>();
        Connection conn = null; PreparedStatement ps = null; ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(
                "SELECT u.user_id, u.username, u.email, u.full_name, " +
                "CASE WHEN a.user_id IS NOT NULL THEN 'Admin' ELSE 'User' END AS role " +
                "FROM users u LEFT JOIN admins a ON u.user_id = a.user_id " +
                "ORDER BY u.user_id");
            rs = ps.executeQuery();
            while (rs.next()) {
                users.add(new String[]{
                    String.valueOf(rs.getInt("user_id")),
                    rs.getString("username"),
                    rs.getString("email"),
                    rs.getString("full_name"),
                    rs.getString("role")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.close(conn, ps, rs);
        }
        return users;
    }

    public boolean updateUser(int userId, String email, String fullName) {
        Connection conn = null; PreparedStatement ps = null;
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(
                "UPDATE users SET email = ?, full_name = ? WHERE user_id = ?");
            ps.setString(1, email.trim());
            ps.setString(2, fullName.trim());
            ps.setInt(3, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DBConnection.close(conn, ps, null);
        }
    }

    public boolean deleteUser(int userId) {
        Connection conn = null; PreparedStatement ps = null;
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(
                "DELETE FROM users WHERE user_id = ?");
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DBConnection.close(conn, ps, null);
        }
    }
}
