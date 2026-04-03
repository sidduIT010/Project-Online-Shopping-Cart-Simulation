package database_setup;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class MainWindow extends JFrame {

    private int    userId;
    private String username;
    private boolean isAdmin;

    private ProductDAO productDAO = new ProductDAO();
    private CartDAO    cartDAO    = new CartDAO();
    private OrderDAO   orderDAO   = new OrderDAO();
    private AdminDAO   adminDAO   = new AdminDAO();

    // Colors
    private static final Color DARK_NAVY  = new Color(30, 40, 60);
    private static final Color ORANGE     = new Color(255, 165, 0);
    private static final Color LIGHT_BG   = new Color(245, 246, 250);
    private static final Color CARD_WHITE = Color.WHITE;

    private JPanel productGridPanel;
    private JTextField searchField;
    private JComboBox<String> categoryCombo;
    private JLabel cartCountLabel;
    private List<Product> currentProducts = new ArrayList<>();

    public MainWindow(int userId, String username) {
        this.userId   = userId;
        this.username = username;
        this.isAdmin  = adminDAO.isAdmin(userId);
        setTitle("E-Commerce Application");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 680);
        setLocationRelativeTo(null);
        buildUI();
        loadProducts("All");
        setVisible(true);
    }

    // ===================== BUILD UI =====================

    private void buildUI() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(LIGHT_BG);
        add(buildNavBar(), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Arial", Font.PLAIN, 13));
        tabs.addTab("Shop", buildContentPanel());
        if (isAdmin) {
            tabs.addTab("User Management", buildUserManagementPanel());
        }
        add(tabs, BorderLayout.CENTER);
    }

    private JPanel buildNavBar() {
        JPanel nav = new JPanel(new BorderLayout());
        nav.setBackground(DARK_NAVY);
        nav.setPreferredSize(new Dimension(0, 60));
        nav.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        // Logo
        JLabel logo = new JLabel("🛒 ShopCart");
        logo.setForeground(Color.WHITE);
        logo.setFont(new Font("Georgia", Font.BOLD, 20));
        nav.add(logo, BorderLayout.WEST);

        // Search bar (center)
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 12));
        searchPanel.setBackground(DARK_NAVY);
        searchField = new JTextField(25);
        searchField.setFont(new Font("Arial", Font.PLAIN, 13));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(80, 100, 140)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        searchField.setToolTipText("Search products...");

        JButton searchBtn = new JButton("Search");
        styleBtn(searchBtn, ORANGE);
        searchBtn.setPreferredSize(new Dimension(80, 30));
        searchBtn.addActionListener(e -> searchProducts());
        searchField.addActionListener(e -> searchProducts());

        searchPanel.add(searchField);
        searchPanel.add(searchBtn);
        nav.add(searchPanel, BorderLayout.CENTER);

        // Right nav buttons
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 12));
        rightPanel.setBackground(DARK_NAVY);

        cartCountLabel = new JLabel("Cart (0)");
        cartCountLabel.setForeground(Color.WHITE);
        cartCountLabel.setFont(new Font("Arial", Font.BOLD, 12));
        rightPanel.add(cartCountLabel);

        String[] navItems = {"Cart", "Orders", "Profile", "Logout"};
        for (String item : navItems) {
            JButton btn = new JButton(item);
            btn.setForeground(Color.WHITE);
            btn.setBackground(DARK_NAVY);
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.setFont(new Font("Arial", Font.PLAIN, 12));
            btn.addActionListener(e -> handleNavClick(item));
            rightPanel.add(btn);
        }
        nav.add(rightPanel, BorderLayout.EAST);
        return nav;
    }

    private JPanel buildContentPanel() {
        JPanel content = new JPanel(new BorderLayout(0, 10));
        content.setBackground(LIGHT_BG);
        content.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // Category filter row
        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterRow.setBackground(LIGHT_BG);

        JLabel catLabel = new JLabel("Category:");
        catLabel.setFont(new Font("Arial", Font.BOLD, 13));
        filterRow.add(catLabel);

        categoryCombo = new JComboBox<>();
        categoryCombo.setFont(new Font("Arial", Font.PLAIN, 13));
        categoryCombo.setPreferredSize(new Dimension(150, 28));
        for (String cat : productDAO.getCategories()) categoryCombo.addItem(cat);
        categoryCombo.addActionListener(e -> {
            String sel = (String) categoryCombo.getSelectedItem();
            loadProducts(sel);
        });
        filterRow.add(categoryCombo);

        // Only admins can manage products
        if (isAdmin) {
            JButton manageBtn = new JButton("Manage Products");
            styleBtn(manageBtn, DARK_NAVY);
            manageBtn.addActionListener(e -> openProductManager());
            filterRow.add(manageBtn);
        }

        content.add(filterRow, BorderLayout.NORTH);

        // Product grid in scroll pane
        productGridPanel = new JPanel();
        productGridPanel.setBackground(LIGHT_BG);
        JScrollPane scroll = new JScrollPane(productGridPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        content.add(scroll, BorderLayout.CENTER);

        return content;
    }

    // ===================== PRODUCT GRID =====================

    private void loadProducts(String category) {
        if (category == null) category = "All";
        currentProducts = productDAO.getAllProducts(category);
        renderProductGrid(currentProducts);
        updateCartCount();
    }

    private void searchProducts() {
        String kw = searchField.getText().trim();
        if (kw.isEmpty()) { loadProducts("All"); return; }
        List<Product> results = productDAO.searchProducts(kw);
        renderProductGrid(results);
    }

    private void renderProductGrid(List<Product> products) {
        productGridPanel.removeAll();
        if (products.isEmpty()) {
            productGridPanel.setLayout(new FlowLayout());
            JLabel empty = new JLabel("No products found.");
            empty.setFont(new Font("Arial", Font.ITALIC, 15));
            empty.setForeground(Color.GRAY);
            productGridPanel.add(empty);
            productGridPanel.revalidate();
            productGridPanel.repaint();
            return;
        }
        int cols = 3;
        productGridPanel.setLayout(new GridLayout(0, cols, 15, 15));
        productGridPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        for (Product p : products) {
            productGridPanel.add(buildProductCard(p));
        }
        productGridPanel.revalidate();
        productGridPanel.repaint();
    }

    private JPanel buildProductCard(Product p) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(CARD_WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220)),
            BorderFactory.createEmptyBorder(12, 12, 12, 12)));

        // Product image placeholder
        JLabel imgLabel = new JLabel();
        imgLabel.setPreferredSize(new Dimension(0, 120));
        imgLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imgLabel.setBackground(new Color(240, 240, 248));
        imgLabel.setOpaque(true);
        imgLabel.setText("<html><div style='text-align:center;color:#aaa;font-size:30px'>📦</div></html>");
        imgLabel.setFont(new Font("Arial", Font.PLAIN, 40));

        String cat = p.getCategory() != null ? p.getCategory() : "";
        if (cat.equals("Electronics")) imgLabel.setText("<html><center><font size='6'>📱</font></center></html>");
        else if (cat.equals("Clothes")) imgLabel.setText("<html><center><font size='6'>👕</font></center></html>");
        else if (cat.equals("Books"))   imgLabel.setText("<html><center><font size='6'>📚</font></center></html>");

        card.add(imgLabel, BorderLayout.NORTH);

        // Info panel
        JPanel info = new JPanel(new GridLayout(3, 1, 2, 2));
        info.setBackground(CARD_WHITE);

        JLabel nameLabel = new JLabel(p.getProductName());
        nameLabel.setFont(new Font("Arial", Font.BOLD, 13));
        nameLabel.setForeground(new Color(30, 40, 60));

        JLabel priceLabel = new JLabel("₹" + String.format("%,.0f", p.getPrice()));
        priceLabel.setFont(new Font("Arial", Font.BOLD, 14));
        priceLabel.setForeground(new Color(30, 40, 60));

        JLabel stockLabel = new JLabel("In stock: " + p.getStock());
        stockLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        stockLabel.setForeground(p.getStock() > 0 ? new Color(0, 150, 80) : Color.RED);

        info.add(nameLabel);
        info.add(priceLabel);
        info.add(stockLabel);
        card.add(info, BorderLayout.CENTER);

        // Add to Cart button
        JButton addBtn = new JButton("Add to Cart");
        styleBtn(addBtn, ORANGE);
        addBtn.setEnabled(p.getStock() > 0);
        addBtn.addActionListener(e -> addToCart(p));
        card.add(addBtn, BorderLayout.SOUTH);

        return card;
    }

    // ===================== CART ACTIONS =====================

    private void addToCart(Product p) {
        cartDAO.addToCart(userId, p.getProductId(), 1);
        updateCartCount();
        JOptionPane.showMessageDialog(this,
            p.getProductName() + " added to cart!",
            "Added to Cart", JOptionPane.INFORMATION_MESSAGE);
    }

    private void updateCartCount() {
        List<CartItem> items = cartDAO.getCartItems(userId);
        int total = items.stream().mapToInt(CartItem::getQuantity).sum();
        cartCountLabel.setText("Cart (" + total + ")");
    }

    // ===================== NAV CLICKS =====================

    private void handleNavClick(String item) {
        switch (item) {
            case "Cart":    openCart();    break;
            case "Orders":  openOrders();  break;
            case "Profile": openProfile(); break;
            case "Logout":  logout();      break;
        }
    }

    // ===================== CART WINDOW =====================

    private void openCart() {
        JDialog cartDialog = new JDialog(this, "Your Cart", true);
        cartDialog.setSize(700, 450);
        cartDialog.setLocationRelativeTo(this);
        cartDialog.setLayout(new BorderLayout());
        cartDialog.getContentPane().setBackground(Color.WHITE);

        List<CartItem> items = new ArrayList<>(cartDAO.getCartItems(userId));

        String[] cols = {"Product", "Price (₹)", "Qty", "Subtotal (₹)", "Action"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return c == 4; }
        };
        JTable table = new JTable(model);
        table.setRowHeight(35);
        table.getTableHeader().setBackground(DARK_NAVY);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        table.setFont(new Font("Arial", Font.PLAIN, 13));

        JLabel totalLabel = new JLabel();
        totalLabel.setFont(new Font("Arial", Font.BOLD, 16));
        totalLabel.setForeground(DARK_NAVY);

        Runnable refreshTotal = () -> {
            double t = items.stream().mapToDouble(CartItem::getTotalPrice).sum();
            totalLabel.setText("Total: ₹" + String.format("%,.0f", t));
        };

        table.getColumnModel().getColumn(4).setCellRenderer(new ButtonRenderer("Remove"));
        table.getColumnModel().getColumn(4).setCellEditor(
            new ButtonEditor(new JCheckBox(), "Remove", e -> {
                int row = table.getSelectedRow();
                if (row >= 0 && row < items.size()) {
                    cartDAO.removeFromCart(items.get(row).getCartId());
                    items.remove(row);
                    model.removeRow(row);
                    refreshTotal.run();
                    updateCartCount();
                }
            }));

        for (CartItem ci : items) {
            model.addRow(new Object[]{
                ci.getProduct().getProductName(),
                String.format("%,.0f", ci.getProduct().getPrice()),
                ci.getQuantity(),
                String.format("%,.0f", ci.getTotalPrice()),
                "Remove"
            });
        }

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createTitledBorder("Cart Items"));
        cartDialog.add(scroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        bottom.setBackground(LIGHT_BG);

        refreshTotal.run();
        bottom.add(totalLabel, BorderLayout.WEST);

        JButton orderBtn = new JButton("Place Order");
        styleBtn(orderBtn, new Color(30, 160, 80));
        orderBtn.setPreferredSize(new Dimension(140, 36));
        orderBtn.addActionListener(e -> {
            List<CartItem> fresh = cartDAO.getCartItems(userId);
            if (fresh.isEmpty()) {
                JOptionPane.showMessageDialog(cartDialog, "Cart is empty.", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            double t = fresh.stream().mapToDouble(CartItem::getTotalPrice).sum();
            int orderId = orderDAO.placeOrder(userId, fresh, t);
            if (orderId > 0) {
                cartDAO.clearCart(userId);
                updateCartCount();
                cartDialog.dispose();
                JOptionPane.showMessageDialog(this,
                    "Order placed successfully!\nOrder ID: " + orderId,
                    "E-Commerce Application", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(cartDialog, "Failed to place order.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        bottom.add(orderBtn, BorderLayout.EAST);
        cartDialog.add(bottom, BorderLayout.SOUTH);
        cartDialog.setVisible(true);
    }

    // ===================== ORDERS WINDOW =====================

    private void openOrders() {
        JDialog dlg = new JDialog(this, "Order History", true);
        dlg.setSize(650, 450);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        List<String[]> orders = orderDAO.getOrderHistory(userId);

        String[] cols = {"Order ID", "Date", "Total (₹)", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        for (String[] row : orders) model.addRow(row);

        JTable table = new JTable(model);
        table.setRowHeight(30);
        table.getTableHeader().setBackground(DARK_NAVY);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createTitledBorder("Your Orders"));
        dlg.add(scroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton viewBtn = new JButton("View Order Details");
        styleBtn(viewBtn, ORANGE);
        viewBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(dlg, "Select an order."); return; }
            int orderId = Integer.parseInt(orders.get(row)[0]);
            showOrderDetails(orderId);
        });
        bottom.add(viewBtn);
        dlg.add(bottom, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void showOrderDetails(int orderId) {
        JDialog dlg = new JDialog(this, "Order #" + orderId + " Details", true);
        dlg.setSize(550, 350);
        dlg.setLocationRelativeTo(this);

        List<String[]> items = orderDAO.getOrderItems(orderId);
        String[] cols = {"Product", "Qty", "Unit Price (₹)", "Subtotal (₹)"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        for (String[] row : items) model.addRow(row);

        JTable table = new JTable(model);
        table.setRowHeight(28);
        table.getTableHeader().setBackground(DARK_NAVY);
        table.getTableHeader().setForeground(Color.WHITE);
        dlg.add(new JScrollPane(table));
        dlg.setVisible(true);
    }

    // ===================== PRODUCT MANAGER =====================

    private void openProductManager() {
        JDialog dlg = new JDialog(this, "Manage Products", true);
        dlg.setSize(750, 500);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());
        dlg.getContentPane().setBackground(Color.WHITE);

        List<Product> products = new ArrayList<>(productDAO.getAllProducts("All"));

        String[] cols = {"ID", "Name", "Category", "Price (₹)", "Stock", "Description"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        for (Product p : products) {
            model.addRow(new Object[]{
                p.getProductId(), p.getProductName(), p.getCategory(),
                p.getPrice(), p.getStock(), p.getDescription()
            });
        }

        JTable table = new JTable(model);
        table.setRowHeight(28);
        table.getTableHeader().setBackground(DARK_NAVY);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createTitledBorder("Products"));
        dlg.add(scroll, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        btnPanel.setBackground(LIGHT_BG);

        JButton addBtn     = new JButton("Add");
        JButton editBtn    = new JButton("Edit");
        JButton deleteBtn  = new JButton("Delete");
        JButton refreshBtn = new JButton("Refresh");

        styleBtn(addBtn, new Color(30, 160, 80));
        styleBtn(editBtn, ORANGE);
        styleBtn(deleteBtn, Color.RED);
        styleBtn(refreshBtn, DARK_NAVY);

        Runnable refreshProductTable = () -> {
            products.clear();
            products.addAll(productDAO.getAllProducts("All"));
            model.setRowCount(0);
            for (Product p : products) {
                model.addRow(new Object[]{
                    p.getProductId(), p.getProductName(), p.getCategory(),
                    p.getPrice(), p.getStock(), p.getDescription()
                });
            }
        };

        addBtn.addActionListener(e -> {
            showProductForm(null, dlg);
            refreshProductTable.run();
            loadProducts((String) categoryCombo.getSelectedItem());
        });

        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(dlg, "Select a product."); return; }
            showProductForm(products.get(row), dlg);
            refreshProductTable.run();
            loadProducts((String) categoryCombo.getSelectedItem());
        });

        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(dlg, "Select a product."); return; }
            int confirm = JOptionPane.showConfirmDialog(dlg,
                "Delete " + products.get(row).getProductName() + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                if (productDAO.deleteProduct(products.get(row).getProductId())) {
                    refreshProductTable.run();
                    loadProducts((String) categoryCombo.getSelectedItem());
                }
            }
        });

        refreshBtn.addActionListener(e -> refreshProductTable.run());

        btnPanel.add(addBtn); btnPanel.add(editBtn);
        btnPanel.add(deleteBtn); btnPanel.add(refreshBtn);
        dlg.add(btnPanel, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void showProductForm(Product existing, JDialog parent) {
        boolean isEdit = existing != null;
        JDialog form = new JDialog(parent, isEdit ? "Edit Product" : "Add Product", true);
        form.setSize(400, 380);
        form.setLocationRelativeTo(parent);
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));
        panel.setBackground(Color.WHITE);

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(4, 0, 4, 0);
        g.gridx = 0; g.gridy = 0;

        JTextField nameF  = addFormRow(panel, g, "Product Name:", isEdit ? existing.getProductName() : "");
        JTextField catF   = addFormRow(panel, g, "Category:",     isEdit ? existing.getCategory()    : "");
        JTextField priceF = addFormRow(panel, g, "Price (₹):",    isEdit ? String.valueOf(existing.getPrice()) : "");
        JTextField stockF = addFormRow(panel, g, "Stock:",        isEdit ? String.valueOf(existing.getStock()) : "");
        JTextField descF  = addFormRow(panel, g, "Description:",  isEdit ? existing.getDescription() : "");

        g.gridy++;
        g.insets = new Insets(12, 0, 4, 0);
        JButton saveBtn = new JButton(isEdit ? "Update" : "Save");
        styleBtn(saveBtn, new Color(30, 160, 80));
        saveBtn.addActionListener(e -> {
            try {
                Product p = isEdit ? existing : new Product();
                p.setProductName(nameF.getText().trim());
                p.setCategory(catF.getText().trim());
                p.setPrice(Double.parseDouble(priceF.getText().trim()));
                p.setStock(Integer.parseInt(stockF.getText().trim()));
                p.setDescription(descF.getText().trim());
                boolean ok = isEdit ? productDAO.updateProduct(p) : productDAO.addProduct(p);
                if (ok) {
                    JOptionPane.showMessageDialog(form,
                        "Product " + (isEdit ? "updated" : "added") + " successfully!");
                    form.dispose();
                } else {
                    JOptionPane.showMessageDialog(form, "Operation failed.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(form, "Invalid price or stock value.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        panel.add(saveBtn, g);
        form.add(panel);
        form.setVisible(true);
    }

    // ===================== USER MANAGEMENT (Admin only) =====================

    private JPanel buildUserManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(LIGHT_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        String[] cols = {"ID", "Username", "Email", "Full Name", "Role"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        List<String[]> users = new ArrayList<>(adminDAO.getAllUsers());
        for (String[] row : users) model.addRow(row);

        JTable table = new JTable(model);
        table.setRowHeight(30);
        table.setFont(new Font("Arial", Font.PLAIN, 13));
        table.getTableHeader().setBackground(DARK_NAVY);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, val, sel, foc, r, c);
                boolean isAdminRow = "Admin".equals(model.getValueAt(r, 4));
                setBackground(sel
                    ? table.getSelectionBackground()
                    : isAdminRow ? new Color(255, 245, 220) : Color.WHITE);
                setForeground(sel ? table.getSelectionForeground() : Color.BLACK);
                return this;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createTitledBorder("Registered Users"));
        panel.add(scroll, BorderLayout.CENTER);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        btnBar.setBackground(LIGHT_BG);

        JButton editBtn    = new JButton("Edit");
        JButton deleteBtn  = new JButton("Delete");
        JButton refreshBtn = new JButton("Refresh");

        styleBtn(editBtn, ORANGE);
        styleBtn(deleteBtn, Color.RED);
        styleBtn(refreshBtn, DARK_NAVY);

        Runnable refreshUserTable = () -> {
            users.clear();
            users.addAll(adminDAO.getAllUsers());
            model.setRowCount(0);
            for (String[] r : users) model.addRow(r);
        };

        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Select a user first.");
                return;
            }
            int    targetId = Integer.parseInt(users.get(row)[0]);
            String curEmail = users.get(row)[2];
            String curName  = users.get(row)[3];
            if (targetId == userId) {
                JOptionPane.showMessageDialog(this,
                    "Use the Profile section to edit your own details.");
                return;
            }
            showEditUserDialog(targetId, curEmail, curName, refreshUserTable);
        });

        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Select a user first.");
                return;
            }
            int    targetId   = Integer.parseInt(users.get(row)[0]);
            String targetName = users.get(row)[1];
            if (targetId == userId) {
                JOptionPane.showMessageDialog(this,
                    "You cannot delete your own account.", "Not Allowed",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(this,
                "Permanently delete user \"" + targetName + "\"?\n" +
                "This will also remove their orders and cart data.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                if (adminDAO.deleteUser(targetId)) {
                    refreshUserTable.run();
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Delete failed.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        refreshBtn.addActionListener(e -> refreshUserTable.run());

        btnBar.add(editBtn);
        btnBar.add(deleteBtn);
        btnBar.add(refreshBtn);
        panel.add(btnBar, BorderLayout.SOUTH);

        return panel;
    }

    private void showEditUserDialog(int targetId, String currentEmail,
                                    String currentName, Runnable onSuccess) {
        JDialog dlg = new JDialog(this, "Edit User #" + targetId, true);
        dlg.setSize(360, 230);
        dlg.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));
        panel.setBackground(Color.WHITE);

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(4, 0, 4, 0);
        g.gridx = 0; g.gridy = 0;

        JTextField emailField = addFormRow(panel, g, "Email:", currentEmail);
        JTextField nameField  = addFormRow(panel, g, "Full Name:", currentName);

        g.gridy++;
        g.insets = new Insets(12, 0, 4, 0);
        JButton saveBtn = new JButton("Save Changes");
        styleBtn(saveBtn, new Color(30, 160, 80));
        saveBtn.addActionListener(e -> {
            String newEmail = emailField.getText().trim();
            String newName  = nameField.getText().trim();
            if (newEmail.isEmpty() || newName.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Fields cannot be empty.");
                return;
            }
            if (adminDAO.updateUser(targetId, newEmail, newName)) {
                JOptionPane.showMessageDialog(dlg, "User updated successfully.");
                dlg.dispose();
                onSuccess.run();
            } else {
                JOptionPane.showMessageDialog(dlg,
                    "Update failed.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        panel.add(saveBtn, g);
        dlg.add(panel);
        dlg.setVisible(true);
    }

    // ===================== PROFILE =====================

    private void openProfile() {
        JOptionPane.showMessageDialog(this,
            "Logged in as: " + username + (isAdmin ? " (Admin)" : ""),
            "Profile", JOptionPane.INFORMATION_MESSAGE);
    }

    // ===================== LOGOUT =====================

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to logout?", "Logout",
            JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            SwingUtilities.invokeLater(LoginFrame::new);
        }
    }

    // ===================== HELPERS =====================

    private void styleBtn(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private JTextField addFormRow(JPanel panel, GridBagConstraints g,
                                  String label, String value) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Arial", Font.PLAIN, 12));
        panel.add(lbl, g);
        g.gridy++;
        JTextField field = new JTextField(value, 20);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        panel.add(field, g);
        g.gridy++;
        return field;
    }
}

// ===================== TABLE BUTTON HELPERS =====================

class ButtonRenderer extends JButton implements TableCellRenderer {
    public ButtonRenderer(String text) {
        setText(text);
        setFont(new Font("Arial", Font.PLAIN, 11));
        setBackground(Color.RED);
        setForeground(Color.WHITE);
        setFocusPainted(false);
        setBorderPainted(false);
    }
    public Component getTableCellRendererComponent(JTable t, Object v,
            boolean sel, boolean foc, int r, int c) { return this; }
}

class ButtonEditor extends DefaultCellEditor {
    private JButton button;
    private ActionListener listener;
    public ButtonEditor(JCheckBox cb, String text, ActionListener al) {
        super(cb);
        this.listener = al;
        button = new JButton(text);
        button.setFont(new Font("Arial", Font.PLAIN, 11));
        button.setBackground(Color.RED);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.addActionListener(e -> { fireEditingStopped(); listener.actionPerformed(e); });
    }
    public Component getTableCellEditorComponent(JTable t, Object v,
            boolean sel, int r, int c) { return button; }
    public Object getCellEditorValue() { return "Remove"; }
}
