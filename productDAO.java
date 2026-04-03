package database_setup;
import java.sql.*;
import java.util.*;

public class ProductDAO {

    // Get all products, optionally filtered by category
    public List<Product> getAllProducts(String category) {
        List<Product> list = new ArrayList<>();
        Connection conn = null; PreparedStatement ps = null; ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            String sql = "SELECT * FROM products";
            if (category != null && !category.equals("All"))
                sql += " WHERE category = ?";
            sql += " ORDER BY product_name";

            ps = conn.prepareStatement(sql);
            if (category != null && !category.equals("All"))
                ps.setString(1, category);

            rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBConnection.close(conn, ps, rs); }
        return list;
    }

    // Search products by name or ID
    public List<Product> searchProducts(String keyword) {
        List<Product> list = new ArrayList<>();
        Connection conn = null; PreparedStatement ps = null; ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            // MySQL uses UPPER() and CAST()
            String sql = "SELECT * FROM products WHERE UPPER(product_name) LIKE ? OR CAST(product_id AS CHAR) = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, "%" + keyword.toUpperCase() + "%");
            ps.setString(2, keyword);
            rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBConnection.close(conn, ps, rs); }
        return list;
    }

    // Add new product
    public boolean addProduct(Product p) {
        Connection conn = null; PreparedStatement ps = null;
        try {
            conn = DBConnection.getConnection();
            String sql = "INSERT INTO products (product_name, category, price, stock, description) VALUES (?,?,?,?,?)";
            ps = conn.prepareStatement(sql);
            ps.setString(1, p.getProductName());
            ps.setString(2, p.getCategory());
            ps.setDouble(3, p.getPrice());
            ps.setInt(4, p.getStock());
            ps.setString(5, p.getDescription());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
        finally { DBConnection.close(conn, ps, null); }
    }

    // Update existing product
    public boolean updateProduct(Product p) {
        Connection conn = null; PreparedStatement ps = null;
        try {
            conn = DBConnection.getConnection();
            String sql = "UPDATE products SET product_name=?, category=?, price=?, stock=?, description=? WHERE product_id=?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, p.getProductName());
            ps.setString(2, p.getCategory());
            ps.setDouble(3, p.getPrice());
            ps.setInt(4, p.getStock());
            ps.setString(5, p.getDescription());
            ps.setInt(6, p.getProductId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
        finally { DBConnection.close(conn, ps, null); }
    }

    // Delete product
    public boolean deleteProduct(int productId) {
        Connection conn = null; PreparedStatement ps = null;
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement("DELETE FROM products WHERE product_id = ?");
            ps.setInt(1, productId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
        finally { DBConnection.close(conn, ps, null); }
    }

    // Get distinct categories
    public List<String> getCategories() {
        List<String> cats = new ArrayList<>();
        cats.add("All");
        Connection conn = null; Statement st = null; ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            st = conn.createStatement();
            rs = st.executeQuery("SELECT DISTINCT category FROM products ORDER BY category");
            while (rs.next()) cats.add(rs.getString(1));
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBConnection.close(conn, st, rs); }
        return cats;
    }

    private Product mapRow(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setProductId(rs.getInt("product_id"));
        p.setProductName(rs.getString("product_name"));
        p.setCategory(rs.getString("category"));
        p.setPrice(rs.getDouble("price"));
        p.setStock(rs.getInt("stock"));
        p.setDescription(rs.getString("description"));
        return p;
    }
}

