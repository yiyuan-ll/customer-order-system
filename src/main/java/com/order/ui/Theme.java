package com.order.ui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

/**
 * 全局主题 — 对应原型图的设计语言
 * 深色顶栏 + 浅灰背景 + 橙色主色调
 */
public class Theme {
    // 主色调
    public static final Color ORANGE     = new Color(234, 88,  12);   // 主橙色
    public static final Color ORANGE_L   = new Color(251,146, 60);    // 浅橙
    public static final Color ORANGE_BG  = new Color(255,237,213);    // 橙色背景

    // 背景
    public static final Color BG         = new Color(243, 244, 246);  // 页面背景
    public static final Color CARD_BG    = Color.WHITE;
    public static final Color DARK_BG    = new Color(17,  24,  39);   // 顶栏深色

    // 文字
    public static final Color TEXT       = new Color(17,  24,  39);
    public static final Color TEXT_SUB   = new Color(107, 114, 128);
    public static final Color TEXT_LIGHT = new Color(156, 163, 175);

    // 状态色
    public static final Color GREEN      = new Color(16,  185, 129);
    public static final Color GREEN_BG   = new Color(209, 250, 229);
    public static final Color GREEN_TEXT = new Color(6,   95,  70);
    public static final Color BLUE       = new Color(59,  130, 246);
    public static final Color BLUE_BG    = new Color(219, 234, 254);
    public static final Color BLUE_TEXT  = new Color(30,  64,  175);
    public static final Color RED        = new Color(239, 68,  68);
    public static final Color RED_BG     = new Color(254, 226, 226);
    public static final Color RED_TEXT   = new Color(153,  27,  27);
    public static final Color AMBER      = new Color(245, 158, 11);
    public static final Color AMBER_BG   = new Color(254, 243, 199);
    public static final Color AMBER_TEXT = new Color(146, 64,  14);
    public static final Color PURPLE     = new Color(168, 85,  247);
    public static final Color PURPLE_BG  = new Color(237, 233, 254);
    public static final Color PURPLE_TEXT= new Color(88,  28, 135);
    public static final Color TEAL       = new Color(20,  184, 166);
    public static final Color TEAL_BG    = new Color(204, 251, 241);

    // 边框
    public static final Color BORDER     = new Color(229, 231, 235);
    public static final Color BORDER_MED = new Color(209, 213, 219);

    // 字体
    public static final Font  FONT_TITLE = new Font("微软雅黑", Font.BOLD, 22);
    public static final Font  FONT_H2    = new Font("微软雅黑", Font.BOLD, 16);
    public static final Font  FONT_BOLD  = new Font("微软雅黑", Font.BOLD, 13);
    public static final Font  FONT_REG   = new Font("微软雅黑", Font.PLAIN, 13);
    public static final Font  FONT_SMALL = new Font("微软雅黑", Font.PLAIN, 12);
    public static final Font  FONT_MONO  = new Font("Consolas", Font.PLAIN, 12);

    /** 圆角卡片容器 */
    public static JPanel card() {
        JPanel p = new JPanel();
        p.setBackground(CARD_BG);
        p.setBorder(new CompoundBorder(
            new RoundedBorder(BORDER, 8),
            new EmptyBorder(16, 16, 16, 16)
        ));
        return p;
    }

    /** 主按钮（橙色填充） */
    public static JButton btnPrimary(String text) {
        JButton b = new JButton(text);
        b.setBackground(ORANGE);
        b.setForeground(Color.WHITE);
        b.setFont(FONT_BOLD);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(8, 16, 8, 16));
        return b;
    }

    /** 次要按钮（白底边框） */
    public static JButton btnSecondary(String text) {
        JButton b = new JButton(text);
        b.setBackground(Color.WHITE);
        b.setForeground(TEXT);
        b.setFont(FONT_REG);
        b.setFocusPainted(false);
        b.setBorder(new CompoundBorder(new RoundedBorder(BORDER_MED, 6), new EmptyBorder(6, 14, 6, 14)));
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    /** 危险按钮 */
    public static JButton btnDanger(String text) {
        JButton b = btnPrimary(text);
        b.setBackground(RED);
        return b;
    }

    /** 标签（状态徽章） */
    public static JLabel badge(String text, Color bg, Color fg) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(new Font("微软雅黑", Font.BOLD, 11));
        l.setForeground(fg);
        l.setBackground(bg);
        l.setOpaque(true);
        l.setBorder(new EmptyBorder(2, 8, 2, 8));
        return l;
    }

    /** 搜索框 */
    public static JTextField searchField(String placeholder) {
        JTextField f = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !isFocusOwner()) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g2.setColor(TEXT_LIGHT);
                    g2.setFont(FONT_REG);
                    g2.drawString(placeholder, 30, getHeight() / 2 + 5);
                }
            }
        };
        f.setFont(FONT_REG);
        f.setBorder(new CompoundBorder(new RoundedBorder(BORDER_MED, 6), new EmptyBorder(6, 32, 6, 12)));
        f.setPreferredSize(new Dimension(320, 36));
        return f;
    }

    /** 图标按钮（小方形，透明背景） */
    public static JButton iconBtn(String text, Color hoverColor) {
        JButton b = new JButton(text);
        b.setFont(FONT_SMALL);
        b.setForeground(TEXT_SUB);
        b.setBackground(new Color(0,0,0,0));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { b.setForeground(hoverColor); }
            public void mouseExited(java.awt.event.MouseEvent e)  { b.setForeground(TEXT_SUB); }
        });
        return b;
    }

    /** 分类过滤按钮 */
    public static JToggleButton filterBtn(String text, boolean active) {
        JToggleButton b = new JToggleButton(text, active);
        b.setFont(FONT_REG);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new CompoundBorder(new RoundedBorder(active ? ORANGE : BORDER, 6), new EmptyBorder(6, 14, 6, 14)));
        if (active) { b.setBackground(ORANGE); b.setForeground(Color.WHITE); b.setOpaque(true); }
        else { b.setBackground(Color.WHITE); b.setForeground(TEXT); b.setOpaque(true); }
        return b;
    }

    public static void applyGlobalFont() {
        Font f = new Font("微软雅黑", Font.PLAIN, 13);
        java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object val = UIManager.get(key);
            if (val instanceof Font) UIManager.put(key, f);
        }
    }

    /** 获取状态对应的颜色组 */
    public static Color[] statusColors(String status) {
        if (status == null) return new Color[]{new Color(243,244,246), TEXT_SUB};
        if ("待处理".equals(status)) return new Color[]{AMBER_BG, AMBER_TEXT};
        if ("已确认".equals(status)) return new Color[]{BLUE_BG, BLUE_TEXT};
        if ("已发货".equals(status)) return new Color[]{PURPLE_BG, PURPLE_TEXT};
        if ("已完成".equals(status)) return new Color[]{GREEN_BG, GREEN_TEXT};
        if ("已取消".equals(status)) return new Color[]{new Color(243,244,246), TEXT_SUB};
        if ("未付款".equals(status)) return new Color[]{AMBER_BG, AMBER_TEXT};
        if ("已付款".equals(status)) return new Color[]{GREEN_BG, GREEN_TEXT};
        if ("已逾期".equals(status)) return new Color[]{RED_BG, RED_TEXT};
        return new Color[]{new Color(243,244,246), TEXT_SUB};
    }
}
