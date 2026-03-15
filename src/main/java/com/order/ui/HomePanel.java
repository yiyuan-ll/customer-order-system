package com.order.ui;

import com.order.dao.*;
import com.order.model.*;
import com.order.util.AIHelper;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * 首页 — 匹配原型图 Image1:
 * - 顶部4个统计卡片（客户/货物/订单/发票）
 * - 左侧AI快速创建订单区
 * - 右侧总营业额 + 最近订单列表
 */
public class HomePanel extends JPanel {

    private final MainFrame frame;
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final GoodsDAO    goodsDAO    = new GoodsDAO();
    private final OrderDAO    orderDAO    = new OrderDAO();
    private final InvoiceDAO  invoiceDAO  = new InvoiceDAO();

    private JLabel lblCust, lblGoods, lblOrders, lblInvoices;
    private JLabel lblRevenue, lblPending;
    private JPanel recentOrdersPanel;
    private JTextArea aiInput;
    private JLabel aiStatus;

    public HomePanel(MainFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout(0, 0));
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(24, 24, 24, 24));

        // Header
        add(buildHeader(), BorderLayout.NORTH);

        // Main body
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildLeft(), buildRight());
        split.setResizeWeight(0.52);
        split.setBorder(null);
        split.setDividerSize(12);
        split.setBackground(Theme.BG);
        add(split, BorderLayout.CENTER);

        refreshStats();
    }

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(0, 0, 20, 0));

        JPanel title = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        title.setOpaque(false);
        JLabel h = new JLabel("客户订购管理系统 ");
        h.setFont(new Font("微软雅黑", Font.BOLD, 24));
        h.setForeground(Theme.TEXT);
        JLabel ai = new JLabel("4.0");
        ai.setFont(new Font("微软雅黑", Font.BOLD, 24));
        ai.setForeground(Theme.ORANGE);
        title.add(h); title.add(ai);

        JLabel sub = new JLabel("智能采购数据管理平台 · Powered by DeepSeek-V3");
        sub.setFont(Theme.FONT_SMALL);
        sub.setForeground(Theme.TEXT_SUB);

        JPanel left = new JPanel(new BorderLayout(0, 4));
        left.setOpaque(false);
        left.add(title, BorderLayout.NORTH);
        left.add(sub, BorderLayout.SOUTH);
        p.add(left, BorderLayout.WEST);

        // Right: powered by + button
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);
        JLabel powered = new JLabel("Powered by DeepSeek-V3");
        powered.setFont(Theme.FONT_SMALL);
        powered.setForeground(Theme.ORANGE);
        JButton btnStat = Theme.btnPrimary("查看分析");
        btnStat.addActionListener(e -> frame.switchTo("stat"));
        right.add(powered); right.add(btnStat);
        p.add(right, BorderLayout.EAST);

        // Stats cards row
        JPanel cards = new JPanel(new GridLayout(1, 4, 12, 0));
        cards.setOpaque(false);
        cards.setBorder(new EmptyBorder(16, 0, 0, 0));

        lblCust    = bigNumLabel();
        lblGoods   = bigNumLabel();
        lblOrders  = bigNumLabel();
        lblInvoices = bigNumLabel();

        cards.add(statCard("客户总数",  lblCust,    new Color(239, 68,  68),  new Color(254, 202, 202)));
        cards.add(statCard("货物种类",  lblGoods,   new Color(16,  185, 129), new Color(167, 243, 208)));
        cards.add(statCard("订单总数",  lblOrders,  new Color(245, 158, 11),  new Color(253, 230, 138)));
        cards.add(statCard("发票总数",  lblInvoices,new Color(59,  130, 246), new Color(191, 219, 254)));

        JPanel north = new JPanel(new BorderLayout(0, 0));
        north.setOpaque(false);
        JPanel topRow = new JPanel(new BorderLayout(0, 0));
        topRow.setOpaque(false);
        topRow.add(left, BorderLayout.WEST);
        topRow.add(right, BorderLayout.EAST);
        north.add(topRow, BorderLayout.NORTH);
        north.add(cards, BorderLayout.SOUTH);

        return north;
    }

    private JPanel statCard(String title, JLabel numLbl, Color bg, Color iconBg) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(bg);
        card.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel t = new JLabel(title, SwingConstants.CENTER);
        t.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        t.setForeground(new Color(255, 255, 255, 220));
        card.add(t, BorderLayout.NORTH);
        card.add(numLbl, BorderLayout.CENTER);
        return card;
    }

    private JLabel bigNumLabel() {
        JLabel l = new JLabel("—", SwingConstants.CENTER);
        l.setFont(new Font("微软雅黑", Font.BOLD, 40));
        l.setForeground(Color.WHITE);
        return l;
    }

    // ── 左侧：AI录入 ─────────────────────────────────────────────────────────
    private JPanel buildLeft() {
        JPanel outer = new JPanel(new BorderLayout(0, 12));
        outer.setOpaque(false);

        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(Theme.CARD_BG);
        card.setBorder(new CompoundBorder(new RoundedBorder(Theme.BORDER, 10), new EmptyBorder(20, 20, 20, 20)));

        // Card header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        titleRow.setOpaque(false);
        JLabel icon = new JLabel("AI");
        icon.setFont(new Font("Arial", Font.PLAIN, 16));
        icon.setForeground(Theme.ORANGE);
        JLabel title = new JLabel("快速创建订单");
        title.setFont(Theme.FONT_H2);
        title.setForeground(Theme.TEXT);
        titleRow.add(icon); titleRow.add(title);

        JLabel hint = new JLabel("例：\u201c张三贸易有限公司买了2台笔记本电脑和3个无线鼠标\u201d");
        hint.setFont(Theme.FONT_SMALL);
        hint.setForeground(Theme.TEXT_SUB);
        header.add(titleRow, BorderLayout.WEST);
        header.add(hint, BorderLayout.EAST);

        // Input area
        aiInput = new JTextArea(5, 0);
        aiInput.setFont(Theme.FONT_REG);
        aiInput.setLineWrap(true);
        aiInput.setWrapStyleWord(true);
        aiInput.setBorder(new CompoundBorder(new RoundedBorder(Theme.BORDER_MED, 6), new EmptyBorder(12, 12, 12, 12)));
        // Ctrl+Enter to submit
        aiInput.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_ENTER) doAIParse();
            }
        });

        // Buttons
        JButton btnParse  = Theme.btnPrimary("AI解析并录入 (Ctrl+Enter)");
        JButton btnClear  = Theme.btnSecondary("清空");
        btnClear.setFont(Theme.FONT_REG);
        btnParse.addActionListener(e -> doAIParse());
        btnClear.addActionListener(e -> { aiInput.setText(""); aiStatus.setText(""); aiStatus.setVisible(false); });

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnRow.setOpaque(false);
        btnRow.add(btnParse); btnRow.add(btnClear);

        // Status label
        aiStatus = new JLabel();
        aiStatus.setFont(Theme.FONT_REG);
        aiStatus.setVisible(false);
        aiStatus.setBorder(new CompoundBorder(new RoundedBorder(Theme.GREEN, 6), new EmptyBorder(10, 14, 10, 14)));

        // Tips
        JLabel tips = new JLabel("<html><font color='#f59e0b'>⚠</font> <font color='#6b7280'>提示：用自然语言描述一笔订单，AI会自动识别客户、货物和数量。<br>" +
            "支持批量导入：每行一条或用分号分隔多条订单描述，AI同时处理。</font></html>");
        tips.setFont(Theme.FONT_SMALL);

        card.add(header,    BorderLayout.NORTH);
        card.add(new JScrollPane(aiInput) {{ setBorder(BorderFactory.createEmptyBorder()); }}, BorderLayout.CENTER);
        JPanel south = new JPanel(new BorderLayout(0, 8));
        south.setOpaque(false);
        south.add(btnRow, BorderLayout.NORTH);
        south.add(aiStatus, BorderLayout.CENTER);
        south.add(tips, BorderLayout.SOUTH);
        card.add(south, BorderLayout.SOUTH);

        outer.add(card, BorderLayout.CENTER);
        return outer;
    }

    // ── 右侧：营业额 + 最近订单 ───────────────────────────────────────────────
    private JPanel buildRight() {
        JPanel outer = new JPanel(new BorderLayout(0, 12));
        outer.setOpaque(false);

        // Revenue card
        JPanel revCard = new JPanel(new BorderLayout(0, 8));
        revCard.setBackground(Theme.CARD_BG);
        revCard.setBorder(new CompoundBorder(new RoundedBorder(Theme.BORDER, 10), new EmptyBorder(20, 20, 20, 20)));

        JPanel revHeader = new JPanel(new BorderLayout());
        revHeader.setOpaque(false);
        JLabel revTitle = new JLabel("总营业额");
        revTitle.setFont(Theme.FONT_BOLD);
        revTitle.setForeground(Theme.TEXT_SUB);
        JLabel trendIcon = new JLabel("");
        trendIcon.setFont(new Font("Arial", Font.BOLD, 18));
        trendIcon.setForeground(Theme.GREEN);
        revHeader.add(revTitle, BorderLayout.WEST);
        revHeader.add(trendIcon, BorderLayout.EAST);

        lblRevenue = new JLabel("¥0", SwingConstants.LEFT);
        lblRevenue.setFont(new Font("微软雅黑", Font.BOLD, 32));
        lblRevenue.setForeground(Theme.TEXT);

        lblPending = new JLabel("待处理订单 0 笔");
        lblPending.setFont(Theme.FONT_SMALL);
        lblPending.setForeground(Theme.TEXT_SUB);

        revCard.add(revHeader, BorderLayout.NORTH);
        revCard.add(lblRevenue, BorderLayout.CENTER);
        revCard.add(lblPending, BorderLayout.SOUTH);

        // Recent orders card
        JPanel recentCard = new JPanel(new BorderLayout(0, 8));
        recentCard.setBackground(Theme.CARD_BG);
        recentCard.setBorder(new CompoundBorder(new RoundedBorder(Theme.BORDER, 10), new EmptyBorder(16, 16, 16, 16)));

        JPanel recentHeader = new JPanel(new BorderLayout());
        recentHeader.setOpaque(false);
        JPanel rh = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        rh.setOpaque(false);
        JLabel clockIcon = new JLabel("");
        clockIcon.setFont(new Font("Arial", Font.PLAIN, 14));
        clockIcon.setForeground(Theme.ORANGE);
        JLabel recentTitle = new JLabel("最近订单");
        recentTitle.setFont(Theme.FONT_BOLD);
        recentTitle.setForeground(Theme.TEXT);
        rh.add(clockIcon); rh.add(recentTitle);

        JLabel viewAll = new JLabel("查看全部");
        viewAll.setFont(Theme.FONT_SMALL);
        viewAll.setForeground(Theme.ORANGE);
        viewAll.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        viewAll.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { frame.switchTo("order"); }
        });
        recentHeader.add(rh, BorderLayout.WEST);
        recentHeader.add(viewAll, BorderLayout.EAST);

        recentOrdersPanel = new JPanel();
        recentOrdersPanel.setLayout(new BoxLayout(recentOrdersPanel, BoxLayout.Y_AXIS));
        recentOrdersPanel.setOpaque(false);

        JScrollPane scroll = new JScrollPane(recentOrdersPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(10);

        recentCard.add(recentHeader, BorderLayout.NORTH);
        recentCard.add(scroll, BorderLayout.CENTER);

        outer.add(revCard,    BorderLayout.NORTH);
        outer.add(recentCard, BorderLayout.CENTER);
        return outer;
    }

    // ── 刷新数据 ─────────────────────────────────────────────────────────────
    public void refreshStats() {
        try {
            lblCust.setText(String.valueOf(customerDAO.getTotalCount()));
            lblGoods.setText(String.valueOf(goodsDAO.getTotalCount()));

            List<Order> orders = orderDAO.findAll();
            lblOrders.setText(String.valueOf(orders.size()));

            List<Invoice> invoices = invoiceDAO.findAll();
            lblInvoices.setText(String.valueOf(invoices.size()));

            // Revenue
            double total = orders.stream().mapToDouble(Order::getTotalAmount).sum();
            lblRevenue.setText("¥" + String.format("%,.0f", total));

            long pending = orders.stream().filter(o -> "待处理".equals(o.getStatus())).count();
            lblPending.setText("待处理订单 " + pending + " 笔");

            // Recent orders (top 4)
            recentOrdersPanel.removeAll();
            int show = Math.min(4, orders.size());
            for (int i = 0; i < show; i++) {
                recentOrdersPanel.add(recentOrderRow(orders.get(i)));
                if (i < show - 1) recentOrdersPanel.add(Box.createVerticalStrut(4));
            }
            recentOrdersPanel.revalidate();
            recentOrdersPanel.repaint();

        } catch (Exception e) { e.printStackTrace(); }
    }

    private JPanel recentOrderRow(Order o) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(10, 0, 10, 0));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));

        // Avatar circle
        JLabel avatar = new JLabel(o.getCustomerName().substring(0, 1), SwingConstants.CENTER);
        avatar.setFont(new Font("微软雅黑", Font.BOLD, 13));
        avatar.setForeground(Color.WHITE);
        avatar.setBackground(avatarColor(o.getCustomerName()));
        avatar.setOpaque(true);
        avatar.setPreferredSize(new Dimension(36, 36));

        JPanel info = new JPanel(new BorderLayout(0, 3));
        info.setOpaque(false);
        JLabel name = new JLabel(o.getCustomerName());
        name.setFont(Theme.FONT_BOLD);
        name.setForeground(Theme.TEXT);
        String dateStr = o.getOrderDate() != null && o.getOrderDate().length() >= 10 ? o.getOrderDate().substring(0,10) : o.getOrderDate();
        JLabel ordInfo = new JLabel("ORD" + String.format("%03d", o.getOrderId()) + " · " + dateStr);
        ordInfo.setFont(Theme.FONT_SMALL);
        ordInfo.setForeground(Theme.TEXT_SUB);
        info.add(name, BorderLayout.NORTH);
        info.add(ordInfo, BorderLayout.SOUTH);

        JPanel right = new JPanel(new BorderLayout(0, 3));
        right.setOpaque(false);
        JLabel amt = new JLabel("¥" + String.format("%,.0f", o.getTotalAmount()), SwingConstants.RIGHT);
        amt.setFont(Theme.FONT_BOLD);
        amt.setForeground(Theme.TEXT);
        Color[] sc = Theme.statusColors(o.getStatus());
        JLabel statusBadge = Theme.badge(o.getStatus(), sc[0], sc[1]);
        JPanel statusWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        statusWrap.setOpaque(false);
        statusWrap.add(statusBadge);
        right.add(amt,        BorderLayout.NORTH);
        right.add(statusWrap, BorderLayout.SOUTH);

        row.add(avatar, BorderLayout.WEST);
        row.add(info,   BorderLayout.CENTER);
        row.add(right,  BorderLayout.EAST);

        // Separator
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(row, BorderLayout.CENTER);
        JSeparator sep = new JSeparator();
        sep.setForeground(Theme.BORDER);
        wrap.add(sep, BorderLayout.SOUTH);
        return wrap;
    }

    private Color avatarColor(String name) {
        Color[] palette = {Theme.ORANGE, Theme.GREEN, Theme.BLUE, Theme.AMBER, Theme.PURPLE};
        return palette[Math.abs(name.hashCode()) % palette.length];
    }

    // ── AI 解析 ───────────────────────────────────────────────────────────────
    private void doAIParse() {
        String text = aiInput.getText().trim();
        if (text.isEmpty()) {
            aiStatus.setText("请先输入订单描述");
            aiStatus.setForeground(Theme.RED_TEXT);
            aiStatus.setBackground(Theme.RED_BG);
            aiStatus.setVisible(true);
            return;
        }
        aiStatus.setText("AI识别中...");
        aiStatus.setForeground(Theme.TEXT_SUB);
        aiStatus.setBackground(new Color(243, 244, 246));
        aiStatus.setVisible(true);

        new SwingWorker<List<Map<String, Object>>, Void>() {
            @Override protected List<Map<String, Object>> doInBackground() throws Exception {
                return AIHelper.parseBatchOrders(text);
            }
            @Override protected void done() {
                try {
                    List<Map<String, Object>> orders = get();
                    if (orders.isEmpty()) {
                        aiStatus.setText("AI未能识别出有效订单，请检查描述");
                        aiStatus.setForeground(Theme.RED_TEXT);
                        aiStatus.setBackground(Theme.RED_BG);
                        return;
                    }
                    int created = 0; double totalAmt = 0;
                    StringBuilder summary = new StringBuilder();
                    for (Map<String, Object> od : orders) {
                        String custName = (String) od.get("customer");
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> items = (List<Map<String, Object>>) od.get("items");
                        if (custName == null || items == null || items.isEmpty()) continue;

                        Customer cust = customerDAO.findByName(custName);
                        if (cust == null) {
                            int choice = JOptionPane.showConfirmDialog(
                                HomePanel.this,
                                "AI识别到客户「" + custName + "」在数据库中不存在，\n是否立即新建该客户？",
                                "新建客户", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                            if (choice != JOptionPane.YES_OPTION) {
                                aiStatus.setText("已取消：客户「" + custName + "」不存在");
                                aiStatus.setForeground(Theme.AMBER_TEXT);
                                aiStatus.setBackground(Theme.AMBER_BG);
                                return;
                            }
                            // Show new customer form
                            cust = showNewCustomerDialog(custName);
                            if (cust == null) return; // user cancelled form
                        }

                        List<OrderItem> orderItems = new ArrayList<>();
                        for (Map<String, Object> it : items) {
                            String goodsName = (String) it.get("goods");
                            int qty = (int) it.get("qty");
                            Goods g = goodsDAO.findByName(goodsName);
                            if (g == null) {
                                int choice = JOptionPane.showConfirmDialog(
                                    HomePanel.this,
                                    "AI识别到货物「" + goodsName + "」在数据库中不存在，\n是否立即新建该货物？",
                                    "新建货物", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                                if (choice != JOptionPane.YES_OPTION) {
                                    aiStatus.setText("已取消：货物「" + goodsName + "」不存在");
                                    aiStatus.setForeground(Theme.AMBER_TEXT);
                                    aiStatus.setBackground(Theme.AMBER_BG);
                                    return;
                                }
                                g = showNewGoodsDialog(goodsName);
                                if (g == null) return;
                            }
                            orderItems.add(new OrderItem(g.getGoodsId(), g.getGoodsName(), g.getUnit(), qty, g.getUnitPrice()));
                        }

                        double amt = orderItems.stream().mapToDouble(OrderItem::getSubtotal).sum();
                        Order order = new Order();
                        order.setCustomerId(cust.getCustomerId());
                        order.setTotalAmount(amt);
                        order.setStatus("待处理");
                        order.setItems(orderItems);
                        orderDAO.insert(order);
                        created++; totalAmt += amt;
                        if (summary.length() > 0) summary.append("；");
                        summary.append("「").append(custName).append("」¥").append(String.format("%,.0f", amt));
                    }

                    if (created > 0) {
                        String msg = "AI解析成功！已创建 " + created + " 笔订单：" + summary + "，共 ¥" + String.format("%,.0f", totalAmt);
                        aiStatus.setText("<html>" + msg + "</html>");
                        aiStatus.setForeground(Theme.GREEN_TEXT);
                        aiStatus.setBackground(Theme.GREEN_BG);
                        refreshStats();
                        frame.showToast("订单已成功创建！", true);
                    }
                } catch (Exception ex) {
                    aiStatus.setText("AI解析失败：" + ex.getMessage());
                    aiStatus.setForeground(Theme.RED_TEXT);
                    aiStatus.setBackground(Theme.RED_BG);
                }
            }
        }.execute();
    }
    // ── 新建客户对话框（AI触发）────────────────────────────────────────────────
    private Customer showNewCustomerDialog(String prefillName) {
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "新建客户", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(420, 300);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Theme.CARD_BG);
        form.setBorder(new EmptyBorder(16, 20, 12, 20));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;

        JTextField fName    = new JTextField(prefillName, 18);
        JTextField fContact = new JTextField(18);
        JTextField fPhone   = new JTextField(18);
        JTextField fEmail   = new JTextField(18);

        String[] labels = {"客户名称 *", "联系人", "电话", "邮箱"};
        JTextField[] fields = {fName, fContact, fPhone, fEmail};
        for (int i = 0; i < labels.length; i++) {
            g.gridx = 0; g.gridy = i; g.weightx = 0;
            form.add(new JLabel(labels[i]), g);
            g.gridx = 1; g.weightx = 1;
            form.add(fields[i], g);
        }

        JButton btnSave   = Theme.btnPrimary("保存");
        JButton btnCancel = Theme.btnSecondary("取消");
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        btns.setBackground(Theme.CARD_BG);
        btns.add(btnCancel); btns.add(btnSave);
        dlg.add(form, BorderLayout.CENTER);
        dlg.add(btns, BorderLayout.SOUTH);

        final Customer[] result = {null};
        btnCancel.addActionListener(e -> dlg.dispose());
        btnSave.addActionListener(e -> {
            if (fName.getText().trim().isEmpty()) { JOptionPane.showMessageDialog(dlg, "名称不能为空"); return; }
            try {
                Customer c = new Customer();
                c.setName(fName.getText().trim());
                c.setContact(fContact.getText().trim());
                c.setPhone(fPhone.getText().trim());
                c.setEmail(fEmail.getText().trim());
                customerDAO.insert(c);
                result[0] = customerDAO.findByName(c.getName());
                dlg.dispose();
            } catch (Exception ex) { JOptionPane.showMessageDialog(dlg, "保存失败：" + ex.getMessage()); }
        });
        dlg.setVisible(true);
        return result[0];
    }

    // ── 新建货物对话框（AI触发）────────────────────────────────────────────────
    private Goods showNewGoodsDialog(String prefillName) {
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "新建货物", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(420, 320);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Theme.CARD_BG);
        form.setBorder(new EmptyBorder(16, 20, 12, 20));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;

        JTextField fName  = new JTextField(prefillName, 18);
        String[] cats = {"电子设备", "电脑配件", "存储设备", "办公用品", "其他"};
        JComboBox<String> fCat = new JComboBox<>(cats);
        JTextField fUnit  = new JTextField("件", 8);
        JTextField fPrice = new JTextField("0.00", 8);
        JTextField fStock = new JTextField("0", 8);

        String[] labels = {"货物名称 *", "分类", "单位", "单价(¥) *", "库存 *"};
        java.awt.Component[] fields = {fName, fCat, fUnit, fPrice, fStock};
        for (int i = 0; i < labels.length; i++) {
            g.gridx = 0; g.gridy = i; g.weightx = 0;
            form.add(new JLabel(labels[i]), g);
            g.gridx = 1; g.weightx = 1;
            form.add(fields[i], g);
        }

        JButton btnSave   = Theme.btnPrimary("保存");
        JButton btnCancel = Theme.btnSecondary("取消");
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        btns.setBackground(Theme.CARD_BG);
        btns.add(btnCancel); btns.add(btnSave);
        dlg.add(form, BorderLayout.CENTER);
        dlg.add(btns, BorderLayout.SOUTH);

        final Goods[] result = {null};
        btnCancel.addActionListener(e -> dlg.dispose());
        btnSave.addActionListener(e -> {
            if (fName.getText().trim().isEmpty()) { JOptionPane.showMessageDialog(dlg, "名称不能为空"); return; }
            try {
                double price = Double.parseDouble(fPrice.getText().trim());
                int stock = Integer.parseInt(fStock.getText().trim());
                Goods gd = new Goods();
                gd.setGoodsName(fName.getText().trim());
                gd.setCategory((String) fCat.getSelectedItem());
                gd.setUnit(fUnit.getText().trim());
                gd.setUnitPrice(price);
                gd.setStock(stock);
                goodsDAO.insert(gd);
                result[0] = goodsDAO.findByName(gd.getGoodsName());
                dlg.dispose();
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(dlg, "单价和库存请输入有效数字");
            } catch (Exception ex) { JOptionPane.showMessageDialog(dlg, "保存失败：" + ex.getMessage()); }
        });
        dlg.setVisible(true);
        return result[0];
    }

}