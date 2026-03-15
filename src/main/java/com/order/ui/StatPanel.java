package com.order.ui;

import com.order.dao.OrderDAO;
import com.order.util.AIHelper;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.List;

/**
 * 统计分析 — 匹配原型 Image6:
 * 左侧：按客户/货物切换Tab + 扇形图 + 明细表 + 刷新按钮
 * 右侧：AI智能销售分析卡（评分圆环 + 分析结果）+ 重新AI分析按钮
 * 底部：货物销售收入对比柱状图
 */
public class StatPanel extends JPanel {

    private final MainFrame frame;
    private final OrderDAO orderDAO = new OrderDAO();

    private static final Color[] PIE_COLORS = {
        new Color(234, 88,  12),
        new Color(16,  185, 129),
        new Color(245, 158, 11),
        new Color(59,  130, 246),
        new Color(168, 85,  247),
        new Color(20,  184, 166),
        new Color(239, 68,  68),
        new Color(99,  102, 241),
    };

    // Left side
    private PiePanel customerPie, goodsPie;
    private JPanel customerTablePanel, goodsTablePanel;
    private JLabel customerSummary, goodsSummary;
    private JTabbedPane leftTabs;

    // Right side AI
    private RingPanel scoreRing;
    private JLabel    scoreLabel, scoreGrade;
    private JPanel    aiResultPanel;
    private JButton   btnAnalyze;
    private List<Object[]> customerData, goodsData;

    public StatPanel(MainFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout(0, 0));
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(24, 24, 24, 24));

        add(buildPageHeader(), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildLeftCard(), buildRightCard());
        split.setResizeWeight(0.52);
        split.setBorder(null);
        split.setDividerSize(12);
        split.setBackground(Theme.BG);
        add(split, BorderLayout.CENTER);

        loadData();
    }

    private JPanel buildPageHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(0, 0, 20, 0));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        JLabel title = new JLabel("统计分析");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT);

        JPanel aiTag = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        aiTag.setBackground(Theme.ORANGE_BG);
        aiTag.setBorder(new CompoundBorder(new RoundedBorder(new Color(254,215,170), 4), new EmptyBorder(2, 8, 2, 8)));
        JLabel aiIcon = new JLabel(" ");
        aiIcon.setFont(new Font("Arial", Font.PLAIN, 13));
        aiIcon.setForeground(Theme.ORANGE);
        JLabel aiLbl = new JLabel("含 AI 智能销售分析 · DeepSeek-V3");
        aiLbl.setFont(Theme.FONT_SMALL);
        aiLbl.setForeground(Theme.ORANGE);
        aiTag.add(aiIcon); aiTag.add(aiLbl);

        left.add(title); left.add(aiTag);
        p.add(left, BorderLayout.WEST);
        return p;
    }

    // ── 左侧图表卡片 ──────────────────────────────────────────────────────────
    private JPanel buildLeftCard() {
        JPanel card = new JPanel(new BorderLayout(0, 0));
        card.setBackground(Theme.CARD_BG);
        card.setBorder(new CompoundBorder(new RoundedBorder(Theme.BORDER, 10), new EmptyBorder(16, 16, 16, 16)));

        leftTabs = new JTabbedPane();
        leftTabs.setFont(Theme.FONT_REG);
        leftTabs.setBackground(Theme.CARD_BG);

        leftTabs.addTab("按客户统计", buildCustomerTab());
        leftTabs.addTab("按货物统计", buildGoodsTab());

        // Style the tab header via UI
        leftTabs.setForeground(Theme.TEXT);

        JButton btnRefresh = Theme.btnSecondary("刷新数据");
        btnRefresh.addActionListener(e -> loadData());
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 4));
        footer.setOpaque(false);
        footer.add(btnRefresh);

        card.add(leftTabs, BorderLayout.CENTER);
        card.add(footer,   BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildCustomerTab() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(8, 0, 0, 0));

        customerPie = new PiePanel();
        customerPie.setPreferredSize(new Dimension(0, 230));

        customerSummary = new JLabel("加载中...");
        customerSummary.setFont(Theme.FONT_SMALL);
        customerSummary.setForeground(Theme.TEXT_SUB);
        customerSummary.setBorder(new EmptyBorder(4, 4, 4, 4));

        customerTablePanel = new JPanel();
        customerTablePanel.setLayout(new BoxLayout(customerTablePanel, BoxLayout.Y_AXIS));
        customerTablePanel.setBackground(Theme.CARD_BG);
        JScrollPane scroll = new JScrollPane(customerTablePanel);
        scroll.setBorder(new RoundedBorder(Theme.BORDER, 6));
        scroll.setPreferredSize(new Dimension(0, 150));

        p.add(customerPie,    BorderLayout.NORTH);
        p.add(customerSummary,BorderLayout.CENTER);
        p.add(scroll,         BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildGoodsTab() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(8, 0, 0, 0));

        goodsPie = new PiePanel();
        goodsPie.setPreferredSize(new Dimension(0, 230));

        goodsSummary = new JLabel("加载中...");
        goodsSummary.setFont(Theme.FONT_SMALL);
        goodsSummary.setForeground(Theme.TEXT_SUB);
        goodsSummary.setBorder(new EmptyBorder(4, 4, 4, 4));

        goodsTablePanel = new JPanel();
        goodsTablePanel.setLayout(new BoxLayout(goodsTablePanel, BoxLayout.Y_AXIS));
        goodsTablePanel.setBackground(Theme.CARD_BG);
        JScrollPane scroll = new JScrollPane(goodsTablePanel);
        scroll.setBorder(new RoundedBorder(Theme.BORDER, 6));
        scroll.setPreferredSize(new Dimension(0, 150));

        p.add(goodsPie,    BorderLayout.NORTH);
        p.add(goodsSummary,BorderLayout.CENTER);
        p.add(scroll,      BorderLayout.SOUTH);
        return p;
    }

    // ── 右侧AI分析卡片 ────────────────────────────────────────────────────────
    private JPanel buildRightCard() {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(Theme.CARD_BG);
        card.setBorder(new CompoundBorder(new RoundedBorder(Theme.BORDER, 10), new EmptyBorder(20, 20, 20, 20)));

        // Title
        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titleRow.setOpaque(false);
        JLabel icon = new JLabel(" ");
        icon.setFont(new Font("Arial", Font.PLAIN, 16));
        icon.setForeground(Theme.ORANGE);
        JLabel title = new JLabel("AI 智能销售分析");
        title.setFont(Theme.FONT_H2);
        title.setForeground(Theme.TEXT);
        titleRow.add(icon); titleRow.add(title);

        // Score card
        JPanel scoreCard = buildScoreCard();

        // AI result panel (scrollable)
        aiResultPanel = new JPanel();
        aiResultPanel.setLayout(new BoxLayout(aiResultPanel, BoxLayout.Y_AXIS));
        aiResultPanel.setBackground(Theme.CARD_BG);
        aiResultPanel.setBorder(new EmptyBorder(4, 0, 4, 0));

        // Initial placeholder
        addPlaceholder();

        JScrollPane scroll = new JScrollPane(aiResultPanel);
        scroll.setBorder(new RoundedBorder(Theme.BORDER, 6));
        scroll.getVerticalScrollBar().setUnitIncrement(10);

        // Analyze button (full width, orange)
        btnAnalyze = new JButton("重新 AI 分析");
        btnAnalyze.setFont(new Font("微软雅黑", Font.BOLD, 15));
        btnAnalyze.setBackground(Theme.ORANGE);
        btnAnalyze.setForeground(Color.WHITE);
        btnAnalyze.setFocusPainted(false);
        btnAnalyze.setBorderPainted(false);
        btnAnalyze.setOpaque(true);
        btnAnalyze.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnAnalyze.setPreferredSize(new Dimension(0, 48));
        btnAnalyze.addActionListener(e -> doAnalyze());

        JPanel body = new JPanel(new BorderLayout(0, 10));
        body.setOpaque(false);
        body.add(scoreCard,  BorderLayout.NORTH);
        body.add(scroll,     BorderLayout.CENTER);
        body.add(btnAnalyze, BorderLayout.SOUTH);

        card.add(titleRow, BorderLayout.NORTH);
        card.add(body,     BorderLayout.CENTER);
        return card;
    }

    private JPanel buildScoreCard() {
        JPanel p = new JPanel(new BorderLayout(16, 0));
        p.setBackground(new Color(255, 247, 237));
        p.setBorder(new CompoundBorder(new RoundedBorder(new Color(254, 215, 170), 8), new EmptyBorder(14, 16, 14, 16)));

        scoreRing = new RingPanel();
        scoreRing.setPreferredSize(new Dimension(72, 72));

        JPanel info = new JPanel(new GridLayout(3, 1, 0, 2));
        info.setOpaque(false);

        JLabel cap = new JLabel("销售健康评分");
        cap.setFont(Theme.FONT_BOLD);
        cap.setForeground(Theme.TEXT);

        scoreLabel = new JLabel("-- / 100");
        scoreLabel.setFont(new Font("微软雅黑", Font.BOLD, 22));
        scoreLabel.setForeground(Theme.ORANGE);

        scoreGrade = new JLabel("等待分析...");
        scoreGrade.setFont(Theme.FONT_SMALL);
        scoreGrade.setForeground(Theme.TEXT_SUB);

        info.add(cap); info.add(scoreLabel); info.add(scoreGrade);
        p.add(scoreRing, BorderLayout.WEST);
        p.add(info,      BorderLayout.CENTER);
        return p;
    }

    private void addPlaceholder() {
        aiResultPanel.removeAll();
        String[] items = {"客户贡献度与集中度分析", "热销 / 注意货物识别", "具体销售建议（3条）", "潜在增长机会"};
        for (String s : items) {
            JLabel l = new JLabel("· " + s);
            l.setFont(Theme.FONT_SMALL);
            l.setForeground(Theme.TEXT_SUB);
            l.setBorder(new EmptyBorder(4, 8, 4, 8));
            l.setAlignmentX(LEFT_ALIGNMENT);
            aiResultPanel.add(l);
        }
    }

    // ── 数据加载 ──────────────────────────────────────────────────────────────
    private void loadData() {
        try {
            customerData = orderDAO.statByCustomer();
            goodsData    = orderDAO.statByGoods();

            // Customer pie
            String[] cn = new String[customerData.size()];
            double[] cv = new double[customerData.size()];
            double totalC = 0;
            customerTablePanel.removeAll();
            customerTablePanel.add(tableHeader(new String[]{"客户", "订单数", "金额(¥)"}));
            for (int i = 0; i < customerData.size(); i++) {
                Object[] r = customerData.get(i);
                cn[i] = (String) r[0]; cv[i] = (double) r[2]; totalC += cv[i];
                customerTablePanel.add(tableRow((String)r[0], String.valueOf(r[1]), String.format("%.2f", r[2]), i));
            }
            customerPie.setData(cn, cv);
            customerSummary.setText("  共 " + customerData.size() + " 位客户下单，累计 ¥" + String.format("%,.2f", totalC));
            customerTablePanel.revalidate();

            // Goods pie
            String[] gn = new String[goodsData.size()];
            double[] gv = new double[goodsData.size()];
            double totalG = 0; int totalQty = 0;
            goodsTablePanel.removeAll();
            goodsTablePanel.add(tableHeader(new String[]{"货物", "销量", "销售额(¥)"}));
            for (int i = 0; i < goodsData.size(); i++) {
                Object[] r = goodsData.get(i);
                gn[i] = (String) r[0]; gv[i] = (double) r[2]; totalG += gv[i]; totalQty += (int) r[1];
                goodsTablePanel.add(tableRow((String)r[0], String.valueOf(r[1]), String.format("%.2f", r[2]), i));
            }
            goodsPie.setData(gn, gv);
            goodsSummary.setText("  共 " + goodsData.size() + " 种货物，总销量 " + totalQty + "，销售额 ¥" + String.format("%,.2f", totalG));
            goodsTablePanel.revalidate();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "加载统计数据失败：" + e.getMessage());
        }
    }

    private JPanel tableHeader(String[] cols) {
        JPanel h = new JPanel(new GridLayout(1, cols.length));
        h.setBackground(Theme.BG);
        h.setBorder(new EmptyBorder(6, 8, 6, 8));
        h.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        for (String c : cols) {
            JLabel l = new JLabel(c);
            l.setFont(new Font("微软雅黑", Font.PLAIN, 11));
            l.setForeground(Theme.TEXT_SUB);
            h.add(l);
        }
        return h;
    }

    private JPanel tableRow(String name, String qty, String amt, int idx) {
        JPanel r = new JPanel(new GridLayout(1, 3));
        r.setBackground(idx % 2 == 0 ? Theme.CARD_BG : new Color(249, 250, 251));
        r.setBorder(new EmptyBorder(5, 8, 5, 8));
        r.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JLabel dot = new JLabel("● " + name);
        dot.setFont(Theme.FONT_SMALL);
        dot.setForeground(PIE_COLORS[idx % PIE_COLORS.length]);

        JLabel qLbl = new JLabel(qty);
        qLbl.setFont(Theme.FONT_SMALL);
        qLbl.setForeground(Theme.TEXT);

        JLabel aLbl = new JLabel(amt);
        aLbl.setFont(Theme.FONT_SMALL);
        aLbl.setForeground(Theme.ORANGE);

        r.add(dot); r.add(qLbl); r.add(aLbl);
        return r;
    }

    // ── AI 分析 ───────────────────────────────────────────────────────────────
    private void doAnalyze() {
        if (customerData == null || customerData.isEmpty()) {
            JOptionPane.showMessageDialog(this, "暂无销售数据，请先创建订单"); return;
        }
        btnAnalyze.setEnabled(false);
        btnAnalyze.setText("AI分析中...");
        aiResultPanel.removeAll();
        JLabel loading = new JLabel("正在分析，请稍候...");
        loading.setFont(Theme.FONT_REG);
        loading.setForeground(Theme.TEXT_SUB);
        loading.setBorder(new EmptyBorder(20, 8, 20, 8));
        loading.setAlignmentX(LEFT_ALIGNMENT);
        aiResultPanel.add(loading);
        aiResultPanel.revalidate();

        StringBuilder ds = new StringBuilder("当前销售数据：\n\n客户统计：\n");
        double total = 0;
        for (Object[] r : customerData) {
            ds.append("  ").append(r[0]).append("：").append(r[1]).append("笔，¥").append(String.format("%.2f", (double)r[2])).append("\n");
            total += (double) r[2];
        }
        ds.append("\n货物统计：\n");
        for (Object[] r : goodsData) {
            ds.append("  ").append(r[0]).append("：销量").append(r[1]).append("，¥").append(String.format("%.2f", (double)r[2])).append("\n");
        }
        ds.append("\n总销售额：¥").append(String.format("%.2f", total));
        final String data = ds.toString();

        new SwingWorker<String, Void>() {
            @Override protected String doInBackground() throws Exception {
                return AIHelper.analyzeSales(data);
            }
            @Override protected void done() {
                btnAnalyze.setEnabled(true);
                btnAnalyze.setText("重新 AI 分析");
                try {
                    String result = get();
                    renderAIResult(result);
                } catch (Exception e) {
                    aiResultPanel.removeAll();
                    JLabel err = new JLabel("<html><font color='red'>AI分析失败：" + e.getMessage() + "</font></html>");
                    err.setFont(Theme.FONT_SMALL);
                    err.setBorder(new EmptyBorder(8, 8, 8, 8));
                    aiResultPanel.add(err);
                    aiResultPanel.revalidate();
                }
            }
        }.execute();
    }

    private void renderAIResult(String text) {
        aiResultPanel.removeAll();
        // Parse score
        int score = 0;
        String grade = "";
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("评分[：:]*\\s*(\\d+)\\s*/\\s*100").matcher(text);
        if (m.find()) { score = Math.min(100, Math.max(0, Integer.parseInt(m.group(1)))); }
        java.util.regex.Matcher mg = java.util.regex.Pattern.compile("评级[：:]*\\s*(\\S+)").matcher(text);
        if (mg.find()) grade = mg.group(1);

        Color scoreColor = score >= 70 ? Theme.GREEN : score >= 40 ? Theme.AMBER : Theme.RED;
        scoreLabel.setText(score + " / 100");
        scoreLabel.setForeground(scoreColor);
        scoreGrade.setText(grade.isEmpty() ? (score >= 70 ? "优秀" : score >= 40 ? "良好" : "需改进") : grade);
        scoreGrade.setForeground(scoreColor);
        scoreRing.setScore(score, scoreColor);

        // Clean markdown before rendering
        String cleaned = stripMarkdown(text);

        // Parse sections and render as cards
        String[] sections = cleaned.split("\\n(?=\\d+[.．【])");
        for (String section : sections) {
            if (section.trim().isEmpty()) continue;
            aiResultPanel.add(buildSectionCard(section.trim()));
            aiResultPanel.add(Box.createVerticalStrut(6));
        }
        aiResultPanel.revalidate();
        aiResultPanel.repaint();
    }

    /** 去除AI返回文本中的Markdown标记 */
    private String stripMarkdown(String text) {
        // Remove **bold** markers
        text = text.replaceAll("\\*\\*(.+?)\\*\\*", "$1");
        // Remove *italic* markers
        text = text.replaceAll("\\*(.+?)\\*", "$1");
        // Remove ## headings (keep content)
        text = text.replaceAll("^#{1,6}\\s*", "");
        text = text.replaceAll("\\n#{1,6}\\s*", "\\n");
        // Remove 【 】 bracket markers from section titles - keep the text
        // (We keep these as they help with section identification)
        // Remove leading dashes used as list bullets (replace with proper spacing)
        text = text.replaceAll("\\n-\\s+", "\\n· ");
        text = text.replaceAll("^-\\s+", "· ");
        // Remove backtick code markers
        text = text.replaceAll("`(.+?)`", "$1");
        return text;
    }

    private JPanel buildSectionCard(String text) {
        // Determine card style based on content
        Color bg, border, fg;
        if (text.contains("客户集中") || text.contains("客户分析")) {
            bg = Theme.BLUE_BG; border = Theme.BLUE; fg = Theme.BLUE_TEXT;
        } else if (text.contains("热销")) {
            bg = Theme.GREEN_BG; border = Theme.GREEN; fg = Theme.GREEN_TEXT;
        } else if (text.contains("注意") || text.contains("滞销")) {
            bg = Theme.AMBER_BG; border = Theme.AMBER; fg = Theme.AMBER_TEXT;
        } else if (text.contains("建议")) {
            bg = new Color(240,253,250); border = Theme.TEAL; fg = new Color(15,118,110);
        } else if (text.contains("增长")) {
            bg = Theme.ORANGE_BG; border = Theme.ORANGE; fg = new Color(154,52,18);
        } else {
            bg = new Color(248,250,252); border = Theme.BORDER; fg = Theme.TEXT;
        }

        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(bg);
        card.setBorder(new CompoundBorder(
            new RoundedBorder(border, 6),
            new EmptyBorder(10, 12, 10, 12)
        ));
        card.setAlignmentX(LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

        JLabel t = new JLabel("<html><body style='width:100%'>" + text.replace("\n", "<br>") + "</body></html>");
        t.setFont(Theme.FONT_SMALL);
        t.setForeground(fg);
        card.add(t, BorderLayout.CENTER);
        return card;
    }

    // ── 扇形图 ────────────────────────────────────────────────────────────────
    static class PiePanel extends JPanel {
        private String[] names  = {};
        private double[] values = {};
        private int hover = -1;

        PiePanel() {
            setBackground(Color.WHITE);
            addMouseMotionListener(new MouseMotionAdapter() {
                @Override public void mouseMoved(MouseEvent e) {
                    int prev = hover;
                    hover = hitTest(e.getX(), e.getY());
                    if (hover != prev) repaint();
                }
            });
            addMouseListener(new MouseAdapter() {
                @Override public void mouseExited(MouseEvent e) { hover = -1; repaint(); }
            });
        }

        void setData(String[] n, double[] v) { names = n; values = v; repaint(); }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            if (values == null || values.length == 0) {
                g2.setFont(Theme.FONT_SMALL);
                g2.setColor(Theme.TEXT_LIGHT);
                g2.drawString("暂无数据", getWidth()/2 - 25, getHeight()/2);
                return;
            }

            int w = getWidth(), h = getHeight();
            int size   = Math.min(w * 52 / 100, h - 24);
            int cx     = size / 2 + 20;
            int cy     = h / 2;
            int outerR = size / 2;
            int innerR = (int)(outerR * 0.50);

            double total = 0;
            for (double v : values) total += v;
            if (total == 0) return;

            double start = -90;
            for (int i = 0; i < values.length; i++) {
                double sweep = values[i] / total * 360.0;
                boolean hl = (i == hover);
                double mid = Math.toRadians(start + sweep / 2);
                int ox = hl ? (int)(Math.cos(mid) * 8) : 0;
                int oy = hl ? (int)(Math.sin(mid) * 8) : 0;

                g2.setColor(PIE_COLORS[i % PIE_COLORS.length]);
                Arc2D.Double arc = new Arc2D.Double(cx - outerR + ox, cy - outerR + oy, size, size, -start, -sweep, Arc2D.PIE);
                g2.fill(arc);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2));
                g2.draw(arc);
                start += sweep;
            }

            // Inner circle
            g2.setColor(Color.WHITE);
            g2.fillOval(cx - innerR, cy - innerR, innerR * 2, innerR * 2);

            // Center text
            if (hover >= 0 && hover < values.length) {
                double pct = values[hover] / total * 100;
                String name = names[hover].length() > 6 ? names[hover].substring(0,5)+"…" : names[hover];
                g2.setFont(new Font("微软雅黑", Font.PLAIN, 11));
                g2.setColor(Theme.TEXT_SUB);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(name, cx - fm.stringWidth(name)/2, cy - 2);
                g2.setFont(new Font("微软雅黑", Font.BOLD, 14));
                g2.setColor(PIE_COLORS[hover % PIE_COLORS.length]);
                fm = g2.getFontMetrics();
                String ps = String.format("%.1f%%", pct);
                g2.drawString(ps, cx - fm.stringWidth(ps)/2, cy + 16);
            } else {
                g2.setFont(Theme.FONT_SMALL);
                g2.setColor(Theme.TEXT_LIGHT);
                String tip = "悬停查看";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(tip, cx - fm.stringWidth(tip)/2, cy + 6);
            }

            // Legend
            int lx = cx + outerR + 18;
            int ly = cy - Math.min(values.length, 8) * 20 / 2;
            for (int i = 0; i < values.length && i < 8; i++) {
                g2.setColor(PIE_COLORS[i % PIE_COLORS.length]);
                g2.fillOval(lx, ly + i*20 + 2, 10, 10);
                g2.setColor(Theme.TEXT);
                g2.setFont(new Font("微软雅黑", Font.PLAIN, 11));
                String lbl = names[i].length() > 7 ? names[i].substring(0,6)+"…" : names[i];
                g2.drawString(lbl + "  " + String.format("%.1f%%", values[i]/total*100), lx + 16, ly + i*20 + 12);
            }
        }

        private int hitTest(int mx, int my) {
            if (values == null || values.length == 0) return -1;
            int h = getHeight(), size = Math.min(getWidth()*52/100, h-24);
            int cx = size/2+20, cy = h/2;
            int outerR = size/2, innerR = (int)(outerR*0.50);
            double dx = mx-cx, dy = my-cy, dist = Math.sqrt(dx*dx+dy*dy);
            if (dist < innerR || dist > outerR) return -1;
            double angle = Math.toDegrees(Math.atan2(dy, dx)) + 90;
            if (angle < 0) angle += 360;
            double total = 0; for (double v : values) total += v;
            double acc = 0;
            for (int i = 0; i < values.length; i++) {
                acc += values[i]/total*360;
                if (angle < acc) return i;
            }
            return -1;
        }
    }

    // ── 评分圆环 ──────────────────────────────────────────────────────────────
    static class RingPanel extends JPanel {
        private int score = 0;
        private Color color = Theme.ORANGE;
        RingPanel() { setOpaque(false); }
        void setScore(int s, Color c) { score = s; color = c; repaint(); }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight(), pad = 5;
            int size = Math.min(w, h) - pad*2;
            int x = (w-size)/2, y = (h-size)/2;
            // Background ring
            g2.setColor(new Color(229, 231, 235));
            g2.setStroke(new BasicStroke(7, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawOval(x, y, size, size);
            // Score arc
            g2.setColor(color);
            g2.drawArc(x, y, size, size, 90, -(int)(score/100.0*360));
            // Center score text
            g2.setFont(new Font("微软雅黑", Font.BOLD, size/3));
            g2.setColor(color);
            String s = String.valueOf(score);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(s, x + (size - fm.stringWidth(s))/2, y + size/2 + fm.getAscent()/2 - 2);
        }
    }
}
