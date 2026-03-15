package com.order.ui;

import com.order.dao.CustomerDAO;
import com.order.model.Customer;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * 客户管理 — 匹配原型 Image2:
 * 表格样式：客户ID(C001格式) / 客户名称(带首字母头像) / 联系人 / 电话 / 邮箱 / 订单数 / 累计金额 / 注册日期 / 操作
 */
public class CustomerPanel extends JPanel {

    private final MainFrame frame;
    private final CustomerDAO dao = new CustomerDAO();
    private JTextField searchField;
    private JPanel listPanel;
    private JLabel countLabel;

    public CustomerPanel(MainFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout(0, 0));
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(24, 24, 24, 24));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
        loadData("");
    }

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(0, 0, 20, 0));

        JPanel left = new JPanel(new BorderLayout(0, 4));
        left.setOpaque(false);
        JLabel title = new JLabel("客户管理");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT);
        countLabel = new JLabel("共 — 位客户");
        countLabel.setFont(Theme.FONT_SMALL);
        countLabel.setForeground(Theme.TEXT_SUB);
        left.add(title, BorderLayout.NORTH);
        left.add(countLabel, BorderLayout.SOUTH);
        p.add(left, BorderLayout.WEST);

        JButton btnAdd = Theme.btnPrimary("+ 新增客户");
        btnAdd.addActionListener(e -> showDialog(null));
        p.add(btnAdd, BorderLayout.EAST);
        return p;
    }

    private JPanel buildContent() {
        JPanel card = new JPanel(new BorderLayout(0, 16));
        card.setBackground(Theme.CARD_BG);
        card.setBorder(new CompoundBorder(new RoundedBorder(Theme.BORDER, 10), new EmptyBorder(20, 20, 20, 20)));

        // Search
        searchField = Theme.searchField("搜索客户名称、联系人、电话...");
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { loadData(searchField.getText()); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { loadData(searchField.getText()); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { loadData(searchField.getText()); }
        });
        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        searchRow.setOpaque(false);
        searchRow.add(searchField);
        card.add(searchRow, BorderLayout.NORTH);

        // Table header
        JPanel tableHeader = buildTableHeader();

        // List
        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(Theme.CARD_BG);

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(14);

        JPanel tableWrap = new JPanel(new BorderLayout());
        tableWrap.setBackground(Theme.CARD_BG);
        tableWrap.add(tableHeader, BorderLayout.NORTH);
        tableWrap.add(scroll, BorderLayout.CENTER);
        card.add(tableWrap, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildTableHeader() {
        JPanel h = new JPanel(new GridLayout(1, 9, 0, 0));
        h.setBackground(Theme.BG);
        h.setBorder(new EmptyBorder(10, 12, 10, 12));
        String[] cols = {"客户ID", "客户名称", "联系人", "联系电话", "邮箱", "订单数", "累计金额", "注册日期", "操作"};
        for (String col : cols) {
            JLabel l = new JLabel(col);
            l.setFont(new Font("微软雅黑", Font.PLAIN, 12));
            l.setForeground(Theme.TEXT_SUB);
            h.add(l);
        }
        return h;
    }

    private void loadData(String kw) {
        try {
            List<Customer> list = kw == null || kw.trim().isEmpty() ? dao.findAll() : dao.search(kw.trim());
            countLabel.setText("共 " + list.size() + " 位客户");
            listPanel.removeAll();
            for (int i = 0; i < list.size(); i++) {
                listPanel.add(customerRow(list.get(i), i % 2 == 0));
            }
            if (list.isEmpty()) {
                JLabel empty = new JLabel("暂无数据", SwingConstants.CENTER);
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

    private JPanel customerRow(Customer c, boolean even) {
        JPanel row = new JPanel(new GridLayout(1, 9, 0, 0));
        row.setBackground(even ? Theme.CARD_BG : new Color(249, 250, 251));
        row.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 1, 0, Theme.BORDER),
            new EmptyBorder(12, 12, 12, 12)
        ));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));

        // ID (C001 format)
        JLabel idLbl = new JLabel("C" + String.format("%03d", c.getCustomerId()));
        idLbl.setFont(Theme.FONT_MONO);
        idLbl.setForeground(Theme.TEXT_SUB);

        // Name with avatar
        JPanel nameCell = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        nameCell.setOpaque(false);
        JLabel avatar = makeAvatar(c.getName(), 28);
        JLabel nameLbl = new JLabel(c.getName());
        nameLbl.setFont(Theme.FONT_BOLD);
        nameLbl.setForeground(Theme.TEXT);
        nameCell.add(avatar); nameCell.add(nameLbl);

        // Order count badge
        JLabel orderCnt = Theme.badge(c.getOrderCount() + " 笔",
            c.getOrderCount() > 0 ? Theme.BLUE_BG : new Color(243,244,246),
            c.getOrderCount() > 0 ? Theme.BLUE_TEXT : Theme.TEXT_SUB);

        // Total amount
        JLabel totalAmt = new JLabel(c.getTotalAmount() > 0 ? "¥" + String.format("%,.0f", c.getTotalAmount()) : "¥0");
        totalAmt.setFont(Theme.FONT_BOLD);
        totalAmt.setForeground(c.getTotalAmount() > 0 ? Theme.GREEN : Theme.TEXT_SUB);

        // Date (YYYY-MM-DD only)
        String date = c.getCreatedAt() != null && c.getCreatedAt().length() >= 10 ? c.getCreatedAt().substring(0, 10) : "";
        JLabel dateLbl = cell(date, Theme.TEXT_SUB);

        // Actions
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        actions.setOpaque(false);
        JButton btnEdit = Theme.iconBtn("编辑", Theme.BLUE);
        JButton btnDel  = Theme.iconBtn("删除", Theme.RED);
        btnEdit.addActionListener(e -> showDialog(c));
        btnDel.addActionListener(e -> confirmDelete(c));
        actions.add(btnEdit); actions.add(btnDel);

        row.add(idLbl);
        row.add(nameCell);
        row.add(cell(nvl(c.getContact()), Theme.TEXT));
        row.add(cell(nvl(c.getPhone()), Theme.TEXT));
        row.add(cell(nvl(c.getEmail()), Theme.TEXT_SUB));
        row.add(wrapCenter(orderCnt));
        row.add(totalAmt);
        row.add(dateLbl);
        row.add(actions);
        return row;
    }

    private void showDialog(Customer cust) {
        boolean isNew = (cust == null);
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this),
            isNew ? "新增客户" : "编辑客户", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(480, 380);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(Theme.BG);
        dlg.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Theme.CARD_BG);
        form.setBorder(new EmptyBorder(20, 24, 20, 24));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 8, 8, 8);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;

        JTextField fName    = field(isNew ? "" : nvl(cust.getName()));
        JTextField fContact = field(isNew ? "" : nvl(cust.getContact()));
        JTextField fPhone   = field(isNew ? "" : nvl(cust.getPhone()));
        JTextField fEmail   = field(isNew ? "" : nvl(cust.getEmail()));
        JTextField fAddr    = field(isNew ? "" : nvl(cust.getAddress()));

        String[][] rows = {{"客户名称 *", null}, {"联系人", null}, {"联系电话", null}, {"邮箱", null}, {"地址", null}};
        JTextField[] fields = {fName, fContact, fPhone, fEmail, fAddr};
        for (int i = 0; i < 5; i++) {
            g.gridx = 0; g.gridy = i; g.weightx = 0;
            JLabel lbl = new JLabel(rows[i][0]);
            lbl.setFont(Theme.FONT_BOLD);
            lbl.setForeground(Theme.TEXT);
            form.add(lbl, g);
            g.gridx = 1; g.weightx = 1;
            form.add(fields[i], g);
        }

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 12));
        btnRow.setBackground(Theme.CARD_BG);
        JButton btnCancel = Theme.btnSecondary("取消");
        JButton btnSave   = Theme.btnPrimary(isNew ? "保存" : "更新");
        btnRow.add(btnCancel); btnRow.add(btnSave);

        dlg.add(form, BorderLayout.CENTER);
        dlg.add(btnRow, BorderLayout.SOUTH);

        btnCancel.addActionListener(e -> dlg.dispose());
        btnSave.addActionListener(e -> {
            if (fName.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "客户名称不能为空！"); return;
            }
            try {
                Customer c = isNew ? new Customer() : cust;
                c.setName(fName.getText().trim()); c.setContact(fContact.getText().trim());
                c.setPhone(fPhone.getText().trim()); c.setEmail(fEmail.getText().trim());
                c.setAddress(fAddr.getText().trim());
                if (isNew) dao.insert(c); else dao.update(c);
                loadData(""); dlg.dispose();
                frame.showToast(isNew ? "客户添加成功！" : "客户信息已更新！", true);
            } catch (Exception ex) { JOptionPane.showMessageDialog(dlg, "操作失败：" + ex.getMessage()); }
        });
        dlg.setVisible(true);
    }

    private void confirmDelete(Customer c) {
        int r = JOptionPane.showConfirmDialog(this, "确定删除客户【" + c.getName() + "】？\n关联的订单数据也将无法查询到此客户。",
            "确认删除", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (r == JOptionPane.YES_OPTION) {
            try { dao.delete(c.getCustomerId()); loadData(""); frame.showToast("客户已删除", true); }
            catch (Exception ex) { JOptionPane.showMessageDialog(this, "删除失败：" + ex.getMessage()); }
        }
    }

    private JLabel makeAvatar(String name, int size) {
        JLabel l = new JLabel(name.substring(0, 1), SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
                super.paintComponent(g);
            }
        };
        Color[] palette = {new Color(234,88,12), new Color(16,185,129), new Color(59,130,246), new Color(245,158,11), new Color(168,85,247)};
        l.setBackground(palette[Math.abs(name.hashCode()) % palette.length]);
        l.setForeground(Color.WHITE);
        l.setFont(new Font("微软雅黑", Font.BOLD, size / 2));
        l.setPreferredSize(new Dimension(size, size));
        l.setOpaque(false);
        return l;
    }

    private JLabel cell(String text, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.FONT_REG);
        l.setForeground(color);
        return l;
    }

    private JPanel wrapCenter(JComponent c) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 6));
        p.setOpaque(false);
        p.add(c);
        return p;
    }

    private JTextField field(String val) {
        JTextField f = new JTextField(val, 20);
        f.setFont(Theme.FONT_REG);
        f.setBorder(new CompoundBorder(new RoundedBorder(Theme.BORDER_MED, 6), new EmptyBorder(7, 10, 7, 10)));
        return f;
    }

    private String nvl(String s) { return s != null ? s : ""; }
}
