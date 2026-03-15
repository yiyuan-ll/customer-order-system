package com.order.ui;

import com.order.db.DatabaseManager;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import javax.swing.border.CompoundBorder;

public class MainFrame extends JFrame {

    private HomePanel homePanel;
    private JPanel contentArea;
    private CardLayout cardLayout;
    private JLabel[] navItems;
    private static final String[] NAV_NAMES = {"首页", "客户管理", "货物管理", "订单管理", "发票管理", "统计分析"};
    private static final String[] CARD_NAMES = {"home", "customer", "goods", "order", "invoice", "stat"};
    private int activeNav = 0;

    public MainFrame() {
        setTitle("客户订购管理系统 v4.0");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 820);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1100, 700));
        Theme.applyGlobalFont();

        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}

        setLayout(new BorderLayout(0, 0));

        // Top navigation bar (dark)
        add(buildTopBar(), BorderLayout.NORTH);

        // Main content with CardLayout
        cardLayout = new CardLayout();
        contentArea = new JPanel(cardLayout);
        contentArea.setBackground(Theme.BG);

        homePanel = new HomePanel(this);
        contentArea.add(homePanel, "home");
        contentArea.add(new CustomerPanel(this), "customer");
        contentArea.add(new GoodsPanel(this), "goods");
        contentArea.add(new OrderPanel(this), "order");
        contentArea.add(new InvoicePanel(this), "invoice");
        contentArea.add(new StatPanel(this), "stat");

        add(contentArea, BorderLayout.CENTER);

        // Status bar
        add(buildStatusBar(), BorderLayout.SOUTH);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) {
                DatabaseManager.getInstance().close();
            }
        });
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Theme.DARK_BG);
        bar.setPreferredSize(new Dimension(0, 56));
        bar.setBorder(new EmptyBorder(0, 20, 0, 20));

        // Left: app title
        JLabel logo = new JLabel("客户订购管理系统 v4.0");
        logo.setFont(new Font("微软雅黑", Font.BOLD, 14));
        logo.setForeground(Color.WHITE);
        bar.add(logo, BorderLayout.WEST);

        // Center: nav tabs
        JPanel nav = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        nav.setOpaque(false);
        navItems = new JLabel[NAV_NAMES.length];
        for (int i = 0; i < NAV_NAMES.length; i++) {
            final int idx = i;
            JLabel lbl = new JLabel(NAV_NAMES[i], SwingConstants.CENTER);
            lbl.setFont(new Font("微软雅黑", Font.PLAIN, 14));
            lbl.setForeground(i == 0 ? Color.WHITE : new Color(156, 163, 175));
            lbl.setBorder(new EmptyBorder(16, 16, 16, 16));
            lbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            lbl.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent e) { switchTo(idx); }
                public void mouseEntered(java.awt.event.MouseEvent e) { if (activeNav != idx) lbl.setForeground(Color.WHITE); }
                public void mouseExited(java.awt.event.MouseEvent e)  { if (activeNav != idx) lbl.setForeground(new Color(156,163,175)); }
            });
            navItems[i] = lbl;
            nav.add(lbl);
        }
        bar.add(nav, BorderLayout.CENTER);

        return bar;
    }

    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        bar.setBackground(new Color(31, 41, 55));
        bar.setPreferredSize(new Dimension(0, 30));
        bar.setBorder(new EmptyBorder(0, 16, 0, 0));

        JLabel lbl = new JLabel("系统就绪  |  数据库: order_system.db  |  AI: DeepSeek-V3");
        lbl.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        lbl.setForeground(new Color(156, 163, 175));
        bar.add(lbl);
        return bar;
    }

    public void switchTo(int idx) {
        activeNav = idx;
        for (int i = 0; i < navItems.length; i++) {
            navItems[i].setForeground(i == idx ? Color.WHITE : new Color(156, 163, 175));
        }
        cardLayout.show(contentArea, CARD_NAMES[idx]);
        // Refresh home stats when switching to home
        if (idx == 0 && homePanel != null) homePanel.refreshStats();
    }

    public void switchTo(String name) {
        for (int i = 0; i < CARD_NAMES.length; i++) {
            if (CARD_NAMES[i].equals(name)) { switchTo(i); return; }
        }
    }

    public void showToast(String message, boolean success) {
        // Show floating toast notification
        JWindow toast = new JWindow(this);
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        panel.setBackground(success ? new Color(240, 253, 244) : new Color(254, 242, 242));
        panel.setBorder(new CompoundBorder(
            new RoundedBorder(success ? Theme.GREEN : Theme.RED, 8),
            new EmptyBorder(2, 6, 2, 12)
        ));
        JLabel icon = new JLabel(success ? "✓" : "✕");
        icon.setFont(new Font("Arial", Font.BOLD, 16));
        icon.setForeground(success ? Theme.GREEN : Theme.RED);
        JLabel msg = new JLabel(message);
        msg.setFont(Theme.FONT_BOLD);
        msg.setForeground(success ? Theme.GREEN_TEXT : Theme.RED_TEXT);
        panel.add(icon); panel.add(msg);
        toast.add(panel);
        toast.pack();

        // Position top-right
        Point loc = getLocationOnScreen();
        toast.setLocation(loc.x + getWidth() - toast.getWidth() - 20, loc.y + 70);
        toast.setVisible(true);

        new Timer(2500, e -> toast.dispose()).start();
    }

    public static void main(String[] args) {
        DatabaseManager.getInstance();
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
