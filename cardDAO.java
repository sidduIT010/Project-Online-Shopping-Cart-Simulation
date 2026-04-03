package database_setup;
import java.sql.*;
import java.util.*;

public class CartDAO {

    public boolean addToCart(int userId, int productId, int quantity) {
        Connection conn = null; PreparedStatement ps = null;
        try {
            conn = DBConnection.getConnection();

            // Check if already in cart
            ps = conn.prepareStatement(
                "SELECT cart_id, quantity FROM cart WHERE user_id = ? AND product_id = ?");
            ps.setInt(1, userId);
            ps.setInt(2, productId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                // Update existing quantity
                int newQty = rs.getInt("quantity") + quantity;
                int cartId = rs.getInt("cart_id");
                rs.close(); ps.close();
                ps = conn.prepareStatement("UPDATE cart SET quantity = ? WHERE cart_id = ?");
                ps.setInt(1, newQty);
                ps.setInt(2, cartId);
            } else {
                // Insert new cart row
                rs.close(); ps.close();
                ps = conn.prepareStatement(
                    "INSERT INTO cart (user_id, product_id, quantity) VALUES (?,?,?)");
                ps.setInt(1, userId);
                ps.setInt(2, productId);
                ps.setInt(3, quantity);
            }
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
        finally { DBConnection.close(conn, ps, null); }
    }

    public List<CartItem> getCartItems(int userId) {
        List<CartItem> items = new ArrayList<>();
        Connection conn = null; PreparedStatement ps = null; ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            String sql = "SELECT c.cart_id, c.quantity, " +
                         "p.product_id, p.product_name, p.category, p.price, p.stock, p.description " +
                         "FROM cart c " +
                         "JOIN products p ON c.product_id = p.product_id " +
                         "WHERE c.user_id = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            rs = ps.executeQuery();
            while (rs.next()) {
                Product p = new Product(
                    rs.getInt("product_id"),
                    rs.getString("product_name"),
                    rs.getString("category"),
                    rs.getDouble("price"),
                    rs.getInt("stock"),
                    rs.getString("description")
                );
                items.add(new CartItem(rs.getInt("cart_id"), userId, p, rs.getInt("quantity")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBConnection.close(conn, ps, rs); }
        return items;
    }

    public boolean updateQuantity(int cartId, int quantity) {
        Connection conn = null; PreparedStatement ps = null;
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement("UPDATE cart SET quantity = ? WHERE cart_id = ?");
            ps.setInt(1, quantity);
            ps.setInt(2, cartId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
        finally { DBConnection.close(conn, ps, null); }
    }

    public boolean removeFromCart(int cartId) {
        Connection conn = null; PreparedStatement ps = null;
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement("DELETE FROM cart WHERE cart_id = ?");
            ps.setInt(1, cartId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
        finally { DBConnection.close(conn, ps, null); }
    }

    public boolean clearCart(int userId) {
        Connection conn = null; PreparedStatement ps = null;
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement("DELETE FROM cart WHERE user_id = ?");
            ps.setInt(1, userId);
            return ps.executeUpdate() >= 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
        finally { DBConnection.close(conn, ps, null); }
    }
}

