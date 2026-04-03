package database_setup;
import java.sql.*;
import java.util.*;

public class OrderDAO {

    public int placeOrder(int userId, List<CartItem> items, double total) {
        Connection conn = null; PreparedStatement ps = null; ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // Insert into orders — use RETURN_GENERATED_KEYS (MySQL standard)
            ps = conn.prepareStatement(
                "INSERT INTO orders (user_id, total_amount, status) VALUES (?,?,?)",
                Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, userId);
            ps.setDouble(2, total);
            ps.setString(3, "PLACED");
            ps.executeUpdate();

            rs = ps.getGeneratedKeys();
            int orderId = -1;
            if (rs.next()) orderId = rs.getInt(1);
            rs.close(); ps.close();

            if (orderId == -1) {
                conn.rollback();
                return -1;
            }

            // Insert order items in batch
            ps = conn.prepareStatement(
                "INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES (?,?,?,?)");
            for (CartItem item : items) {
                ps.setInt(1, orderId);
                ps.setInt(2, item.getProduct().getProductId());
                ps.setInt(3, item.getQuantity());
                ps.setDouble(4, item.getProduct().getPrice());
                ps.addBatch();
            }
            ps.executeBatch();
            conn.commit();
            return orderId;

        } catch (SQLException e) {
            e.printStackTrace();
            try { if (conn != null) conn.rollback(); } catch (SQLException ignored) {}
            return -1;
        } finally {
            DBConnection.close(conn, ps, rs);
        }
    }

    // Get order history for a user
    public List<String[]> getOrderHistory(int userId) {
        List<String[]> orders = new ArrayList<>();
        Connection conn = null; PreparedStatement ps = null; ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            // MySQL date format function
            String sql = "SELECT order_id, DATE_FORMAT(order_date, '%d-%b-%Y %H:%i'), " +
                         "total_amount, status " +
                         "FROM orders WHERE user_id = ? ORDER BY order_date DESC";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            rs = ps.executeQuery();
            while (rs.next()) {
                orders.add(new String[]{
                    String.valueOf(rs.getInt(1)),
                    rs.getString(2),
                    String.format("%.2f", rs.getDouble(3)),
                    rs.getString(4)
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBConnection.close(conn, ps, rs); }
        return orders;
    }

    // Get line items for a specific order
    public List<String[]> getOrderItems(int orderId) {
        List<String[]> items = new ArrayList<>();
        Connection conn = null; PreparedStatement ps = null; ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            String sql = "SELECT p.product_name, oi.quantity, oi.unit_price, " +
                         "(oi.quantity * oi.unit_price) AS subtotal " +
                         "FROM order_items oi " +
                         "JOIN products p ON oi.product_id = p.product_id " +
                         "WHERE oi.order_id = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, orderId);
            rs = ps.executeQuery();
            while (rs.next()) {
                items.add(new String[]{
                    rs.getString(1),
                    String.valueOf(rs.getInt(2)),
                    String.format("%.2f", rs.getDouble(3)),
                    String.format("%.2f", rs.getDouble(4))
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBConnection.close(conn, ps, rs); }
        return items;
    }
}

