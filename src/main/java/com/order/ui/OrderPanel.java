package com.order.ui;

import com.order.dao.*;
import com.order.model.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单管理 — 匹配原型 Image4:
 * - 可折叠行（点击展开明细标签）
 * - 状态过滤标签栏
 * - 每行含客户头像、货物数、金额、状态徽章、操作按钮
 */
public class OrderPanel extends JPanel {

    private final MainFrame frame;
    private final OrderDAO    orderDAO = new OrderDAO();
    private final CustomerDAO custDAO  = new CustomerDAO();
    private final GoodsDAO    goodsDAO = new GoodsDAO();

    private JTextField searchField;
    private JPanel listPanel;
    private JLabel countLabel;
    private String currentFilter = "全部";
    private Set<Integer> expandedOrders = new HashSet<>();

    public OrderPanel(MainFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout(0, 0));
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(24, 24, 24, 24));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
        loadData();
    }

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(0, 0, 20, 0));

        JPanel left = new JPanel(new BorderLayout(0, 4));
        left.setOpaque(false);
        JLabel title = new JLabel("订单管理");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT);
        countLabel = new JLabel("共 — 笔订单 · 总金额 ¥—");
        countLabel.setFont(Theme.FONT_SMALL);
        countLabel.setForeground(Theme.TEXT_SUB);
        left.add(title, BorderLayout.NORTH);
        left.add(countLabel, BorderLayout.SOUTH);
        p.add(left, BorderLayout.WEST);

        JButton btnAdd = Theme.btnPrimary("+ 新建订单");
        btnAdd.addActionListener(e -> showCreateDialog());
        p.add(btnAdd, BorderLayout.EAST);
        return p;
    }

    private JPanel buildContent() {
        JPanel card = new JPanel(new BorderLayout(0, 0));
        card.setBackground(Theme.CARD_BG);
        card.setBorder(new CompoundBorder(new RoundedBorder(Theme.BORDER, 10), new EmptyBorder(20, 20, 20, 20)));

        // Search + filter
        JPanel topRow = new JPanel(new BorderLayout(12, 0));
        topRow.setOpaque(false);
        searchField = Theme.searchField("搜索客户名称、订单ID...");
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { loadData(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { loadData(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { loadData(); }
        });
        topRow.add(searchField, BorderLayout.WEST);

        // Filter buttons
        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        filterRow.setOpaque(false);
        String[] filters = {"全部", "待处理", "已确认", "已发货", "已完成"};
        ButtonGroup bg = new ButtonGroup();
        for (String f : filters) {
            boolean active = f.equals(currentFilter);
            JToggleButton btn = Theme.filterBtn(f, active);
            bg.add(btn); btn.setSelected(active);
            btn.addActionListener(e -> { currentFilter = f; loadData(); });
            filterRow.add(btn);
        }
        topRow.add(filterRow, BorderLayout.EAST);

        // List panel
        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(Theme.CARD_BG);

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(14);

        JPanel tableArea = new JPanel(new BorderLayout(0, 0));
        tableArea.setBackground(Theme.CARD_BG);
        tableArea.setBorder(new EmptyBorder(12, 0, 0, 0));
        tableArea.add(buildTableHeader(), BorderLayout.NORTH);
        tableArea.add(scroll, BorderLayout.CENTER);

        card.add(topRow,     BorderLayout.NORTH);
        card.add(tableArea,  BorderLayout.CENTER);
        return card;
    }

    private JPanel buildTableHeader() {
        JPanel h = new JPanel(new GridLayout(1, 7, 0, 0));
        h.setBackground(Theme.BG);
        h.setBorder(new EmptyBorder(10, 44, 10, 12));
        String[] cols = {"订单ID", "客户名称", "货物数", "总金额", "日期", "状态", "操作"};
        for (String col : cols) {
            JLabel l = new JLabel(col);
            l.setFont(new Font("微软雅黑", Font.PLAIN, 12));
            l.setForeground(Theme.TEXT_SUB);
            h.add(l);
        }
        return h;
    }

    private void loadData() {
        try {
            String kw = searchField.getText().trim();
            List<Order> list = kw.isEmpty() ? orderDAO.findAll() : orderDAO.search(kw);
            if (!"全部".equals(currentFilter)) {
                final String f = currentFilter;
                list = list.stream().filter(o -> f.equals(o.getStatus())).collect(Collectors.toList());
            }

            double total = list.stream().mapToDouble(Order::getTotalAmount).sum();
            countLabel.setText("共 " + list.size() + " 笔订单 · 总金额 ¥" + String.format("%,.0f", total));

            listPanel.removeAll();
            for (Order o : list) {
                listPanel.add(orderRow(o));
                if (expandedOrders.contains(o.getOrderId())) {
                    listPanel.add(detailPanel(o));
                }
            }
            if (list.isEmpty()) {
                JLabel empty = new JLabel("暂无订单数据", SwingConstants.CENTER);
                empty.setFont(Theme.FONT_REG);
                empty.setForeground(Theme.TEXT_SUB);
                empty.setBorder(new EmptyBorder(40, 0, 40, 0));
                empty.setAlignmentX(CENTER_ALIGNMENT);
                listPanel.add(empty);
            }
            listPanel.revalidate();
            listPanel.repaint();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private JPanel orderRow(Order o) {
        JPanel row = new JPanel(new BorderLayout(0, 0));
        row.setBackground(Theme.CARD_BG);
        row.setBorder(new MatteBorder(0, 0, 1, 0, Theme.BORDER));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));

        // Expand toggle
        boolean expanded = expandedOrders.contains(o.getOrderId());
        JLabel toggle = new JLabel(expanded ? "▲" : "▼");
        toggle.setFont(new Font("Arial", Font.PLAIN, 12));
        toggle.setForeground(Theme.TEXT_SUB);
        toggle.setBorder(new EmptyBorder(0, 12, 0, 8));
        toggle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        toggle.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (expandedOrders.contains(o.getOrderId())) expandedOrders.remove(o.getOrderId());
                else expandedOrders.add(o.getOrderId());
                loadData();
            }
        });

        JPanel cells = new JPanel(new GridLayout(1, 7, 0, 0));
        cells.setOpaque(false);
        cells.setBorder(new EmptyBorder(12, 0, 12, 12));

        // Order ID
        JLabel idLbl = new JLabel("ORD" + String.format("%03d", o.getOrderId()));
        idLbl.setFont(Theme.FONT_MONO);
        idLbl.setForeground(Theme.TEXT_SUB);

        // Customer with avatar
        JPanel custCell = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        custCell.setOpaque(false);
        JLabel avatar = makeAvatar(o.getCustomerName(), 28);
        JLabel custLbl = new JLabel(o.getCustomerName());
        custLbl.setFont(Theme.FONT_BOLD);
        custLbl.setForeground(Theme.TEXT);
        custCell.add(avatar); custCell.add(custLbl);

        // Items count
        JLabel itemsCnt;
        try {
            int cnt = orderDAO.findItems(o.getOrderId()).size();
            itemsCnt = new JLabel(cnt + " 种");
        } catch (Exception ex) { itemsCnt = new JLabel("—"); }
        itemsCnt.setFont(Theme.FONT_REG);
        itemsCnt.setForeground(Theme.TEXT_SUB);

        // Amount
        JLabel amtLbl = new JLabel("¥" + String.format("%,.0f", o.getTotalAmount()));
        amtLbl.setFont(Theme.FONT_BOLD);
        amtLbl.setForeground(Theme.ORANGE);

        // Date
        String date = o.getOrderDate() != null && o.getOrderDate().length() >= 10 ? o.getOrderDate().substring(0, 10) : "";
        JLabel dateLbl = new JLabel(date);
        dateLbl.setFont(Theme.FONT_REG);
        dateLbl.setForeground(Theme.TEXT_SUB);

        // Status
        Color[] sc = Theme.statusColors(o.getStatus());
        JPanel statusWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 4));
        statusWrap.setOpaque(false);
        statusWrap.add(Theme.badge(o.getStatus(), sc[0], sc[1]));

        // Actions
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
        actions.setOpaque(false);
        JButton btnEdit = Theme.iconBtn("编辑", Theme.BLUE);
        JButton btnDel  = Theme.iconBtn("删除", Theme.RED);
        btnEdit.addActionListener(e -> showStatusDialog(o));
        btnDel.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this, "确定删除订单 ORD" + String.format("%03d", o.getOrderId()) + "？",
                    "确认", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                try { orderDAO.delete(o.getOrderId()); expandedOrders.remove(o.getOrderId()); loadData(); }
                catch (Exception ex) { JOptionPane.showMessageDialog(this, "删除失败：" + ex.getMessage()); }
            }
        });
        actions.add(btnEdit); actions.add(btnDel);

        cells.add(idLbl); cells.add(custCell); cells.add(itemsCnt);
        cells.add(amtLbl); cells.add(dateLbl); cells.add(statusWrap); cells.add(actions);

        row.add(toggle, BorderLayout.WEST);
        row.add(cells,  BorderLayout.CENTER);
        return row;
    }

    private JPanel detailPanel(Order o) {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(new Color(249, 250, 251));
        wrap.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 1, 0, Theme.BORDER),
            new EmptyBorder(10, 56, 10, 16)
        ));
        wrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JPanel inner = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        inner.setOpaque(false);
        JLabel label = new JLabel("订单明细：");
        label.setFont(Theme.FONT_SMALL);
        label.setForeground(Theme.TEXT_SUB);
        inner.add(label);

        try {
            List<OrderItem> items = orderDAO.findItems(o.getOrderId());
            for (OrderItem it : items) {
                JPanel tag = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
                tag.setBackground(Theme.ORANGE_BG);
                tag.setBorder(new CompoundBorder(new RoundedBorder(new Color(254, 215, 170), 4), new EmptyBorder(3, 8, 3, 8)));
                JLabel t = new JLabel(it.getGoodsName() + " × " + it.getQuantity() + "  ¥" + String.format("%,.0f", it.getSubtotal()));
                t.setFont(Theme.FONT_SMALL);
                t.setForeground(Theme.ORANGE);
                tag.add(t);
                inner.add(tag);
            }
        } catch (Exception ex) { inner.add(new JLabel("加载失败")); }

        wrap.add(inner, BorderLayout.CENTER);
        return wrap;
    }

    private void showCreateDialog() {
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "新建订单", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(720, 540);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout(0, 0));
        dlg.getContentPane().setBackground(Theme.BG);

        JPanel form = new JPanel(new BorderLayout(0, 12));
        form.setBorder(new EmptyBorder(20, 20, 10, 20));
        form.setBackground(Theme.CARD_BG);

        // Customer selector
        JPanel custRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        custRow.setOpaque(false);
        List<Customer> customers;
        try { customers = custDAO.findAll(); } catch (Exception e) { return; }
        JComboBox<String> cbCust = new JComboBox<>(customers.stream().map(Customer::getName).toArray(String[]::new));
        cbCust.setFont(Theme.FONT_REG);
        cbCust.setPreferredSize(new Dimension(220, 34));
        JTextField fRemark = new JTextField(20);
        fRemark.setFont(Theme.FONT_REG);
        fRemark.setBorder(new CompoundBorder(new RoundedBorder(Theme.BORDER_MED, 6), new EmptyBorder(6, 10, 6, 10)));

        JLabel custLbl = new JLabel("客户："); custLbl.setFont(Theme.FONT_BOLD);
        JLabel remLbl  = new JLabel("备注："); remLbl.setFont(Theme.FONT_BOLD);
        custRow.add(custLbl); custRow.add(cbCust); custRow.add(remLbl); custRow.add(fRemark);
        form.add(custRow, BorderLayout.NORTH);

        // Items table
        String[] cols = {"货物ID", "货物名称", "单位", "单价", "数量", "小计"};
        javax.swing.table.DefaultTableModel tm = new javax.swing.table.DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable itemTable = new JTable(tm);
        itemTable.setFont(Theme.FONT_REG); itemTable.setRowHeight(28);
        itemTable.getTableHeader().setFont(Theme.FONT_BOLD);
        itemTable.getTableHeader().setBackground(Theme.BG);

        // Add item row
        List<Goods> goodsList; try { goodsList = goodsDAO.findAll(); } catch (Exception e) { return; }
        JComboBox<String> cbGoods = new JComboBox<>(goodsList.stream().map(Goods::getGoodsName).toArray(String[]::new));
        cbGoods.setFont(Theme.FONT_REG);
        JTextField fQty = new JTextField("1", 5);
        fQty.setFont(Theme.FONT_REG);
        fQty.setBorder(new CompoundBorder(new RoundedBorder(Theme.BORDER_MED, 6), new EmptyBorder(6, 10, 6, 10)));

        JButton btnAddItem = Theme.btnPrimary("添加");
        JButton btnRemItem = Theme.btnSecondary("移除选中");

        JPanel addRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        addRow.setOpaque(false);
        addRow.add(new JLabel("货物：")); addRow.add(cbGoods);
        addRow.add(new JLabel("  数量：")); addRow.add(fQty);
        addRow.add(btnAddItem); addRow.add(btnRemItem);

        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setOpaque(false);
        center.add(addRow, BorderLayout.NORTH);
        center.add(new JScrollPane(itemTable) {{ setBorder(new RoundedBorder(Theme.BORDER, 6)); }}, BorderLayout.CENTER);
        form.add(center, BorderLayout.CENTER);

        List<OrderItem> items = new ArrayList<>();
        JLabel lblTotal = new JLabel("合计：¥0.00");
        lblTotal.setFont(new Font("微软雅黑", Font.BOLD, 16));
        lblTotal.setForeground(Theme.ORANGE);

        Runnable updateTotal = () -> {
            double t = items.stream().mapToDouble(OrderItem::getSubtotal).sum();
            lblTotal.setText("合计：¥" + String.format("%,.2f", t));
        };

        btnAddItem.addActionListener(e -> {
            int idx = cbGoods.getSelectedIndex();
            if (idx < 0) return;
            Goods g = goodsList.get(idx);
            int qty;
            try { qty = Math.max(1, Integer.parseInt(fQty.getText().trim())); }
            catch (NumberFormatException nfe) { JOptionPane.showMessageDialog(dlg, "数量请输入正整数"); return; }
            OrderItem item = new OrderItem(g.getGoodsId(), g.getGoodsName(), g.getUnit(), qty, g.getUnitPrice());
            items.add(item);
            tm.addRow(new Object[]{g.getGoodsId(), g.getGoodsName(), g.getUnit(),
                "¥" + g.getUnitPrice(), qty, "¥" + String.format("%.2f", item.getSubtotal())});
            updateTotal.run();
        });

        btnRemItem.addActionListener(e -> {
            int row = itemTable.getSelectedRow();
            if (row >= 0) { items.remove(row); tm.removeRow(row); updateTotal.run(); }
        });

        JPanel botRow = new JPanel(new BorderLayout());
        botRow.setOpaque(false);
        botRow.setBorder(new EmptyBorder(8, 20, 16, 20));
        JButton btnSave   = Theme.btnPrimary("提交订单");
        JButton btnCancel = Theme.btnSecondary("取消");
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btns.setOpaque(false);
        btns.add(lblTotal); btns.add(btnCancel); btns.add(btnSave);
        botRow.add(btns, BorderLayout.EAST);

        dlg.add(form, BorderLayout.CENTER);
        dlg.add(botRow, BorderLayout.SOUTH);

        btnCancel.addActionListener(e -> dlg.dispose());
        btnSave.addActionListener(e -> {
            if (customers.isEmpty()) { JOptionPane.showMessageDialog(dlg, "请先添加客户"); return; }
            if (items.isEmpty()) { JOptionPane.showMessageDialog(dlg, "请至少添加一种货物"); return; }
            try {
                Customer c = customers.get(cbCust.getSelectedIndex());
                double total = items.stream().mapToDouble(OrderItem::getSubtotal).sum();
                Order order = new Order();
                order.setCustomerId(c.getCustomerId());
                order.setTotalAmount(total);
                order.setStatus("待处理");
                order.setRemark(fRemark.getText().trim());
                order.setItems(items);
                orderDAO.insert(order);
                loadData(); dlg.dispose();
                frame.showToast("订单创建成功！", true);
            } catch (Exception ex) { JOptionPane.showMessageDialog(dlg, "创建失败：" + ex.getMessage()); }
        });
        dlg.setVisible(true);
    }

    private void showStatusDialog(Order o) {
        String[] statuses = {"待处理", "已确认", "已发货", "已完成", "已取消"};
        String sel = (String) JOptionPane.showInputDialog(this, "选择订单状态：", "更新状态",
            JOptionPane.PLAIN_MESSAGE, null, statuses, o.getStatus());
        if (sel != null && !sel.equals(o.getStatus())) {
            try { orderDAO.updateStatus(o.getOrderId(), sel); loadData(); frame.showToast("状态已更新", true); }
            catch (Exception ex) { JOptionPane.showMessageDialog(this, "更新失败：" + ex.getMessage()); }
        }
    }

    private JLabel makeAvatar(String name, int size) {
        JLabel l = new JLabel(name != null && !name.isEmpty() ? name.substring(0, 1) : "?", SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
                super.paintComponent(g);
            }
        };
        Color[] palette = {Theme.ORANGE, Theme.GREEN, Theme.BLUE, Theme.AMBER, Theme.PURPLE};
        l.setBackground(name != null ? palette[Math.abs(name.hashCode()) % palette.length] : Theme.ORANGE);
        l.setForeground(Color.WHITE);
        l.setFont(new Font("微软雅黑", Font.BOLD, size / 2));
        l.setPreferredSize(new Dimension(size, size));
        l.setOpaque(false);
        return l;
    }
}
