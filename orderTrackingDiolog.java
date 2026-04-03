
package database_setup;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

public class OrderTrackingDialog extends JDialog {

    private static final Color DARK_NAVY = new Color(30, 40, 60);
    private static final Color LIGHT_BG  = new Color(245, 246, 250);
    private static final Color ORANGE    = new Color(255, 165, 0);

    private UserDAO userDAO;
    private OrderDAO orderDAO;
    private int userId;

    public OrderTrackingDialog(JFrame parent, int userId,
                               UserDAO userDAO, OrderDAO orderDAO) {
        super(parent, "Order Tracking", true);
        this.userId = userId;
        this.userDAO = userDAO;
        this.orderDAO = orderDAO;

        setSize(680, 460);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        buildUI();
        setVisible(true);
    }

    private void buildUI() {

        getContentPane().setBackground(LIGHT_BG);

        String[] cols = {"Order ID", "Date", "Total (₹)", "Items", "Status"};

        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        List<String[]> orders = userDAO.getOrderTracking(userId);

        for (String[] row : orders) {
            model.addRow(row);
        }

        JTable table = new JTable(model);
        table.setRowHeight(32);
        table.setFont(new Font("Arial", Font.PLAIN, 13));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JTableHeader header = table.getTableHeader();
        header.setBackground(DARK_NAVY);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Arial", Font.BOLD, 13));

        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {

            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int r, int c) {

                super.getTableCellRendererComponent(t, val, sel, foc, r, c);

                String status = val != null ? val.toString() : "";

                if (!sel) {
                    switch (status.toLowerCase()) {

                        case "delivered":
                            setBackground(new Color(220, 255, 220));
                            setForeground(new Color(0, 130, 0));
                            break;

                        case "shipped":
                            setBackground(new Color(220, 235, 255));
                            setForeground(new Color(0, 60, 180));
                            break;

                        case "processing":
                            setBackground(new Color(255, 248, 220));
                            setForeground(new Color(180, 120, 0));
                            break;

                        case "cancelled":
                            setBackground(new Color(255, 220, 220));
                            setForeground(new Color(180, 0, 0));
                            break;

                        default:
                            setBackground(Color.WHITE);
                            setForeground(Color.BLACK);
                    }
                }

                setHorizontalAlignment(CENTER);

                return this;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createTitledBorder("Your Orders"));

        add(scroll, BorderLayout.CENTER);

        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        legend.setBackground(LIGHT_BG);

        legend.add(badge("Processing",
                new Color(255, 248, 220),
                new Color(180, 120, 0)));

        legend.add(badge("Shipped",
                new Color(220, 235, 255),
                new Color(0, 60, 180)));

        legend.add(badge("Delivered",
                new Color(220, 255, 220),
                new Color(0, 130, 0)));

        legend.add(badge("Cancelled",
                new Color(255, 220, 220),
                new Color(180, 0, 0)));

        add(legend, BorderLayout.NORTH);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        bottom.setBackground(LIGHT_BG);

        JButton detailBtn = new JButton("View Details");
        detailBtn.setBackground(ORANGE);
        detailBtn.setForeground(Color.WHITE);
        detailBtn.setFont(new Font("Arial", Font.BOLD, 12));
        detailBtn.setFocusPainted(false);
        detailBtn.setBorderPainted(false);

        detailBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        detailBtn.addActionListener(e -> {

            int row = table.getSelectedRow();

            if (row < 0) {
                JOptionPane.showMessageDialog(this,
                        "Select an order first.");
                return;
            }

            int orderId = Integer.parseInt(orders.get(row)[0]);

            showOrderDetail(orderId);
        });

        JButton closeBtn = new JButton("Close");

        closeBtn.setBackground(DARK_NAVY);
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setFont(new Font("Arial", Font.BOLD, 12));

        closeBtn.setFocusPainted(false);
        closeBtn.setBorderPainted(false);

        closeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        closeBtn.addActionListener(e -> dispose());

        bottom.add(detailBtn);
        bottom.add(closeBtn);

        add(bottom, BorderLayout.SOUTH);
    }

    private void showOrderDetail(int orderId) {

        JDialog dlg = new JDialog(this,
                "Order #" + orderId + " — Details", true);

        dlg.setSize(560, 360);
        dlg.setLocationRelativeTo(this);

        dlg.setLayout(new BorderLayout());

        List<String[]> items = orderDAO.getOrderItems(orderId);

        String[] cols = {"Product", "Qty", "Unit Price (₹)", "Subtotal (₹)"};

        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        for (String[] row : items) {
            model.addRow(row);
        }

        JTable table = new JTable(model);

        table.setRowHeight(28);
        table.setFont(new Font("Arial", Font.PLAIN, 13));

        JTableHeader header = table.getTableHeader();
        header.setBackground(DARK_NAVY);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Arial", Font.BOLD, 13));

        DefaultTableCellRenderer rightAlign =
                new DefaultTableCellRenderer();

        rightAlign.setHorizontalAlignment(SwingConstants.RIGHT);

        table.getColumnModel().getColumn(1).setCellRenderer(rightAlign);
        table.getColumnModel().getColumn(2).setCellRenderer(rightAlign);
        table.getColumnModel().getColumn(3).setCellRenderer(rightAlign);

        JScrollPane scroll =
                new JScrollPane(table);

        scroll.setBorder(
                BorderFactory.createTitledBorder(
                        "Items in Order #" + orderId));

        dlg.add(scroll, BorderLayout.CENTER);

        double total = items.stream()
                .mapToDouble(r -> {
                    try {
                        return Double.parseDouble(
                                r[3].replace(",", ""));
                    } catch (Exception e) {
                        return 0;
                    }
                }).sum();

        JPanel foot = new JPanel(new BorderLayout());

        foot.setBorder(
                BorderFactory.createEmptyBorder(8, 16, 8, 16));

        foot.setBackground(LIGHT_BG);

        JLabel totalLabel =
                new JLabel("Order Total: ₹"
                        + String.format("%,.0f", total));

        totalLabel.setFont(new Font("Arial", Font.BOLD, 14));
        totalLabel.setForeground(DARK_NAVY);

        foot.add(totalLabel, BorderLayout.WEST);

        JButton closeBtn = new JButton("Close");

        closeBtn.setBackground(DARK_NAVY);
        closeBtn.setForeground(Color.WHITE);

        closeBtn.setFont(new Font("Arial", Font.BOLD, 12));

        closeBtn.setFocusPainted(false);
        closeBtn.setBorderPainted(false);

        closeBtn.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        closeBtn.addActionListener(e -> dlg.dispose());

        foot.add(closeBtn, BorderLayout.EAST);

        dlg.add(foot, BorderLayout.SOUTH);

        dlg.setVisible(true);
    }

    private JLabel badge(String text, Color bg, Color fg) {

        JLabel lbl = new JLabel("  " + text + "  ");

        lbl.setOpaque(true);
        lbl.setBackground(bg);
        lbl.setForeground(fg);

        lbl.setFont(new Font("Arial", Font.BOLD, 11));

        lbl.setBorder(
                BorderFactory.createLineBorder(fg, 1));

        return lbl;
    }
}

