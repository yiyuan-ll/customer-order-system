package com.order.ui;

import com.order.dao.InvoiceDAO;
import com.order.dao.OrderDAO;
import com.order.model.Invoice;
import com.order.model.Order;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * 发票管理 — 匹配原型 Image5:
 * - 顶部三个汇总卡片（已收款 / 待收款 / 逾期发票数）
 * - 状态过滤 + 搜索
 * - 表格含发票ID、关联订单(橙色链接)、客户名、金额、开票日期、状态徽章、操作
 */
public class InvoicePanel extends JPanel {

    private final MainFrame frame;
    private final InvoiceDAO invoiceDAO = new InvoiceDAO();
    private final OrderDAO   orderDAO   = new OrderDAO();

    private JTextField searchField;
    private JPanel listPanel;
    private JLabel lblPaid, lblUnpaid, lblOverdue;
    private String currentFilter = "全部";

    public InvoicePanel(MainFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout(0, 0));
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(24, 24, 24, 24));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
        loadData();
    }

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout(0, 16));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(0, 0, 20, 0));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JPanel left = new JPanel(new BorderLayout(0, 4));
        left.setOpaque(false);
        JLabel title = new JLabel("发票管理");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT);

        try {
            int cnt = invoiceDAO.findAll().size();
            JLabel sub = new JLabel("共 " + cnt + " 张发票");
            sub.setFont(Theme.FONT_SMALL);
            sub.setForeground(Theme.TEXT_SUB);
            left.add(title, BorderLayout.NORTH);
            left.add(sub, BorderLayout.SOUTH);
        } catch (Exception e) { left.add(title, BorderLayout.CENTER); }

        JButton btnAdd = Theme.btnPrimary("+ 开具发票");
        btnAdd.addActionListener(e -> showIssueDialog());
        top.add(left, BorderLayout.WEST);
        top.add(btnAdd, BorderLayout.EAST);

        // Stats cards
        JPanel cards = new JPanel(new GridLayout(1, 3, 12, 0));
        cards.setOpaque(false);
        lblPaid    = new JLabel("¥—");
        lblUnpaid  = new JLabel("¥—");
        lblOverdue = new JLabel("—");
        lblPaid.setFont(new Font("微软雅黑", Font.BOLD, 22));
        lblUnpaid.setFont(new Font("微软雅黑", Font.BOLD, 22));
        lblOverdue.setFont(new Font("微软雅黑", Font.BOLD, 22));
        lblPaid.setForeground(Theme.GREEN);
        lblUnpaid.setForeground(Theme.AMBER);
        lblOverdue.setForeground(Theme.RED);

        cards.add(summaryCard("已收款", lblPaid));
        cards.add(summaryCard("待收款", lblUnpaid));
        cards.add(summaryCard("逾期发票", lblOverdue));

        p.add(top, BorderLayout.NORTH);
        p.add(cards, BorderLayout.SOUTH);
        return p;
    }

    private JPanel summaryCard(String title, JLabel numLbl) {
        JPanel c = new JPanel(new BorderLayout(0, 8));
        c.setBackground(Theme.CARD_BG);
        c.setBorder(new CompoundBorder(new RoundedBorder(Theme.BORDER, 8), new EmptyBorder(16, 20, 16, 20)));
        JLabel t = new JLabel(title);
        t.setFont(Theme.FONT_SMALL);
        t.setForeground(Theme.TEXT_SUB);
        c.add(t, BorderLayout.NORTH);
        c.add(numLbl, BorderLayout.CENTER);
        return c;
    }

    private JPanel buildContent() {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(Theme.CARD_BG);
        card.setBorder(new CompoundBorder(new RoundedBorder(Theme.BORDER, 10), new EmptyBorder(20, 20, 20, 20)));

        // Search + filter
        JPanel topRow = new JPanel(new BorderLayout(12, 0));
        topRow.setOpaque(false);
        searchField = Theme.searchField("搜索客户名称、发票ID、订单ID...");
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { loadData(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { loadData(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { loadData(); }
        });
        topRow.add(searchField, BorderLayout.WEST);

        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        filterRow.setOpaque(false);
        ButtonGroup bg = new ButtonGroup();
        String[] filters = {"全部", "未付款", "已付款", "已逾期"};
        for (String f : filters) {
            JToggleButton btn = Theme.filterBtn(f, f.equals(currentFilter));
            bg.add(btn); btn.setSelected(f.equals(currentFilter));
            btn.addActionListener(e -> { currentFilter = f; loadData(); });
            filterRow.add(btn);
        }
        topRow.add(filterRow, BorderLayout.EAST);
        card.add(topRow, BorderLayout.NORTH);

        // Table
        JPanel tableArea = new JPanel(new BorderLayout(0, 0));
        tableArea.setBackground(Theme.CARD_BG);
        tableArea.setBorder(new EmptyBorder(12, 0, 0, 0));
        tableArea.add(buildTableHeader(), BorderLayout.NORTH);

        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(Theme.CARD_BG);
        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setBorder(null);
        tableArea.add(scroll, BorderLayout.CENTER);
        card.add(tableArea, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildTableHeader() {
        JPanel h = new JPanel(new GridLayout(1, 6, 0, 0));
        h.setBackground(Theme.BG);
        h.setBorder(new EmptyBorder(10, 12, 10, 12));
        String[] cols = {"发票ID", "关联订单", "客户名称", "金额", "开票日期", "状态", "操作"};
        // 7 cols
        h.setLayout(new GridLayout(1, 7, 0, 0));
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
            // Update summary
            lblPaid.setText("¥" + String.format("%,.0f", invoiceDAO.sumByStatus("已付款")));
            lblUnpaid.setText("¥" + String.format("%,.0f", invoiceDAO.sumByStatus("未付款")));
            lblOverdue.setText(invoiceDAO.countByStatus("已逾期") + " 张");

            String kw = searchField.getText().trim();
            List<Invoice> list = kw.isEmpty() ? ("全部".equals(currentFilter) ? invoiceDAO.findAll() : invoiceDAO.findByStatus(currentFilter)) : invoiceDAO.search(kw);

            listPanel.removeAll();
            for (int i = 0; i < list.size(); i++) listPanel.add(invoiceRow(list.get(i), i % 2 == 0));
            if (list.isEmpty()) {
                JLabel empty = new JLabel("暂无发票数据", SwingConstants.CENTER);
                empty.setFont(Theme.FONT_REG); empty.setForeground(Theme.TEXT_SUB);
                empty.setBorder(new EmptyBorder(40, 0, 40, 0)); empty.setAlignmentX(CENTER_ALIGNMENT);
                listPanel.add(empty);
            }
            listPanel.revalidate(); listPanel.repaint();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private JPanel invoiceRow(Invoice inv, boolean even) {
        JPanel row = new JPanel(new GridLayout(1, 7, 0, 0));
        row.setBackground(even ? Theme.CARD_BG : new Color(249, 250, 251));
        row.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, Theme.BORDER), new EmptyBorder(12, 12, 12, 12)));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));

        // Invoice ID
        JLabel idLbl = new JLabel("INV" + String.format("%03d", inv.getInvoiceId()));
        idLbl.setFont(Theme.FONT_SMALL); idLbl.setForeground(Theme.TEXT_SUB);

        // Order link (orange)
        JLabel ordLink = new JLabel("ORD" + String.format("%03d", inv.getOrderId()));
        ordLink.setFont(Theme.FONT_SMALL); ordLink.setForeground(Theme.ORANGE);
        ordLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        ordLink.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { frame.switchTo("order"); }
        });

        // Customer
        JLabel custLbl = new JLabel(nvl(inv.getCustomerName()));
        custLbl.setFont(Theme.FONT_REG); custLbl.setForeground(Theme.TEXT);

        // Amount
        JLabel amtLbl = new JLabel("¥" + String.format("%,.0f", inv.getTotalAmount()));
        amtLbl.setFont(Theme.FONT_BOLD); amtLbl.setForeground(Theme.ORANGE);

        // Date
        String date = inv.getIssueDate() != null && inv.getIssueDate().length() >= 10 ? inv.getIssueDate().substring(0, 10) : "";
        JLabel dateLbl = new JLabel(date);
        dateLbl.setFont(Theme.FONT_REG); dateLbl.setForeground(Theme.TEXT_SUB);

        // Status badge
        Color[] sc = Theme.statusColors(inv.getStatus());
        JPanel statusWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 4));
        statusWrap.setOpaque(false);
        statusWrap.add(Theme.badge(inv.getStatus(), sc[0], sc[1]));

        // Actions
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
        actions.setOpaque(false);
        JButton btnEdit = Theme.iconBtn("编辑", Theme.BLUE);
        JButton btnDel  = Theme.iconBtn("删除", Theme.RED);
        btnEdit.addActionListener(e -> {
            String[] statuses = {"未付款", "已付款", "已逾期"};
            String sel = (String) JOptionPane.showInputDialog(this, "更新发票状态：", "更新状态",
                JOptionPane.PLAIN_MESSAGE, null, statuses, inv.getStatus());
            if (sel != null) {
                try { invoiceDAO.updateStatus(inv.getInvoiceId(), sel); loadData(); }
                catch (Exception ex) { JOptionPane.showMessageDialog(this, ex.getMessage()); }
            }
        });
        btnDel.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this, "确定删除此发票？", "确认", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                try { invoiceDAO.delete(inv.getInvoiceId()); loadData(); frame.showToast("发票已删除", true); }
                catch (Exception ex) { JOptionPane.showMessageDialog(this, ex.getMessage()); }
            }
        });
        actions.add(btnEdit); actions.add(btnDel);

        row.add(idLbl); row.add(ordLink); row.add(custLbl);
        row.add(amtLbl); row.add(dateLbl); row.add(statusWrap); row.add(actions);
        return row;
    }

    private void showIssueDialog() {
        String input = JOptionPane.showInputDialog(this, "请输入要开票的订单ID：", "开具发票", JOptionPane.PLAIN_MESSAGE);
        if (input == null || input.trim().isEmpty()) return;
        try {
            int orderId = Integer.parseInt(input.trim());
            if (invoiceDAO.existsForOrder(orderId)) {
                JOptionPane.showMessageDialog(this, "该订单已开具发票！"); return;
            }
            Order order = orderDAO.findById(orderId);
            if (order == null) { JOptionPane.showMessageDialog(this, "订单 #" + orderId + " 不存在"); return; }

            double taxRate = 0.13;
            double taxAmt  = order.getTotalAmount() * taxRate;
            double total   = order.getTotalAmount() + taxAmt;

            int confirm = JOptionPane.showConfirmDialog(this,
                String.format("订单 #%d  客户：%s\n不含税金额：¥%,.2f\n税率：13%%  税额：¥%,.2f\n含税总额：¥%,.2f\n\n确认开具？",
                    orderId, order.getCustomerName(), order.getTotalAmount(), taxAmt, total),
                "确认开票", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;

            Invoice inv = new Invoice();
            inv.setOrderId(orderId);
            inv.setInvoiceNo(invoiceDAO.generateInvoiceNo());
            inv.setAmount(order.getTotalAmount());
            inv.setTaxRate(taxRate);
            inv.setTaxAmount(taxAmt);
            inv.setTotalAmount(total);
            inv.setStatus("未付款");
            invoiceDAO.insert(inv);
            loadData();
            frame.showToast("发票开具成功！", true);
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "请输入有效的整数订单ID");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "操作失败：" + ex.getMessage());
        }
    }

    private String nvl(String s) { return s != null ? s : ""; }
}
