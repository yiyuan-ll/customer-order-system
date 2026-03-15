package com.order.ui;

import com.order.dao.GoodsDAO;
import com.order.model.Goods;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * 货物管理 — 匹配原型 Image3: 卡片网格布局，含分类过滤标签
 */
public class GoodsPanel extends JPanel {

    private final MainFrame frame;
    private final GoodsDAO dao = new GoodsDAO();
    private JTextField searchField;
    private JPanel gridPanel;
    private JLabel countLabel;
    private String currentCategory = "全部";
    private JPanel filterRow;

    public GoodsPanel(MainFrame frame) {
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
        JLabel title = new JLabel("货物管理");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT);
        countLabel = new JLabel("共 — 种货物");
        countLabel.setFont(Theme.FONT_SMALL);
        countLabel.setForeground(Theme.TEXT_SUB);
        left.add(title, BorderLayout.NORTH);
        left.add(countLabel, BorderLayout.SOUTH);
        p.add(left, BorderLayout.WEST);

        JButton btnAdd = Theme.btnPrimary("+ 新增货物");
        btnAdd.addActionListener(e -> showDialog(null));
        p.add(btnAdd, BorderLayout.EAST);
        return p;
    }

    private JPanel buildContent() {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(Theme.CARD_BG);
        card.setBorder(new CompoundBorder(new RoundedBorder(Theme.BORDER, 10), new EmptyBorder(20, 20, 20, 20)));

        // Search + filter row
        JPanel topRow = new JPanel(new BorderLayout(12, 0));
        topRow.setOpaque(false);
        searchField = Theme.searchField("搜索货物名称、分类...");
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { loadData(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { loadData(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { loadData(); }
        });
        topRow.add(searchField, BorderLayout.WEST);

        // Filter buttons
        filterRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        filterRow.setOpaque(false);
        topRow.add(filterRow, BorderLayout.EAST);

        card.add(topRow, BorderLayout.NORTH);

        // Grid panel with scroll
        gridPanel = new JPanel(new GridLayout(0, 3, 12, 12));
        gridPanel.setBackground(Theme.BG);
        gridPanel.setBorder(new EmptyBorder(4, 0, 0, 0));

        JScrollPane scroll = new JScrollPane(gridPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    private void rebuildFilterButtons(List<Goods> all) {
        filterRow.removeAll();
        java.util.Set<String> cats = new java.util.LinkedHashSet<>();
        cats.add("全部");
        for (Goods g : all) if (g.getCategory() != null && !g.getCategory().isEmpty()) cats.add(g.getCategory());

        ButtonGroup bg = new ButtonGroup();
        for (String cat : cats) {
            boolean active = cat.equals(currentCategory);
            JToggleButton btn = Theme.filterBtn(cat, active);
            bg.add(btn);
            btn.setSelected(active);
            btn.addActionListener(e -> {
                currentCategory = cat;
                rebuildFilterButtons(all);
                filterByCategory();
            });
            filterRow.add(btn);
        }
        filterRow.revalidate();
        filterRow.repaint();
    }

    private void filterByCategory() {
        try {
            String kw = searchField.getText().trim();
            List<Goods> list;
            if (!kw.isEmpty()) {
                list = dao.search(kw);
            } else if ("全部".equals(currentCategory)) {
                list = dao.findAll();
            } else {
                list = dao.findByCategory(currentCategory);
            }
            renderGrid(list);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadData() {
        try {
            String kw = searchField.getText().trim();
            List<Goods> all = dao.findAll();
            rebuildFilterButtons(all);
            List<Goods> list = kw.isEmpty() ? ("全部".equals(currentCategory) ? all : dao.findByCategory(currentCategory)) : dao.search(kw);
            countLabel.setText("共 " + all.size() + " 种货物");
            renderGrid(list);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void renderGrid(List<Goods> list) {
        gridPanel.removeAll();
        for (Goods g : list) gridPanel.add(goodsCard(g));
        if (list.isEmpty()) {
            JLabel empty = new JLabel("暂无货物数据", SwingConstants.CENTER);
            empty.setFont(Theme.FONT_REG);
            empty.setForeground(Theme.TEXT_SUB);
            gridPanel.add(empty);
        }
        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private JPanel goodsCard(Goods g) {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(Theme.CARD_BG);
        card.setBorder(new CompoundBorder(
            new RoundedBorder(Theme.BORDER, 8),
            new EmptyBorder(16, 16, 16, 16)
        ));

        // Header: icon + name + actions
        JPanel header = new JPanel(new BorderLayout(8, 0));
        header.setOpaque(false);

        JPanel iconBox = new JPanel(new BorderLayout());
        iconBox.setBackground(Theme.ORANGE_BG);
        iconBox.setPreferredSize(new Dimension(40, 40));
        iconBox.setBorder(new RoundedBorder(new Color(254, 215, 170), 8));
        JLabel icon = new JLabel("📦", SwingConstants.CENTER);      
        //icon.setFont(new Font("Arial", Font.PLAIN, 18));
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20)); 
        icon.setForeground(Theme.ORANGE);
        iconBox.add(icon, BorderLayout.CENTER);

        JPanel nameBlock = new JPanel(new BorderLayout(0, 3));
        nameBlock.setOpaque(false);
        JLabel nameLbl = new JLabel(g.getGoodsName());
        nameLbl.setFont(Theme.FONT_BOLD);
        nameLbl.setForeground(Theme.TEXT);
        JLabel idLbl = new JLabel("P" + String.format("%03d", g.getGoodsId()));
        idLbl.setFont(Theme.FONT_SMALL);
        idLbl.setForeground(Theme.TEXT_LIGHT);
        nameBlock.add(nameLbl, BorderLayout.NORTH);
        nameBlock.add(idLbl, BorderLayout.SOUTH);

        JPanel actBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        actBtns.setOpaque(false);
        JButton btnEdit = Theme.iconBtn("编辑", Theme.BLUE);
        JButton btnDel  = Theme.iconBtn("删除", Theme.RED);
        btnEdit.addActionListener(e -> showDialog(g));
        btnDel.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this, "确定删除货物【" + g.getGoodsName() + "】？",
                    "确认", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                try { dao.delete(g.getGoodsId()); loadData(); frame.showToast("货物已删除", true); }
                catch (Exception ex) { JOptionPane.showMessageDialog(this, "删除失败：" + ex.getMessage()); }
            }
        });
        actBtns.add(btnEdit); actBtns.add(btnDel);

        header.add(iconBox,   BorderLayout.WEST);
        header.add(nameBlock, BorderLayout.CENTER);
        header.add(actBtns,   BorderLayout.EAST);

        // Category + unit
        JPanel catRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        catRow.setOpaque(false);
        catRow.add(categoryBadge(g.getCategory()));
        JLabel unitLbl = new JLabel("单位：" + g.getUnit());
        unitLbl.setFont(Theme.FONT_SMALL);
        unitLbl.setForeground(Theme.TEXT_SUB);
        catRow.add(unitLbl);

        // Stats: 单价 / 库存 / 已售
        JPanel stats = new JPanel(new GridLayout(1, 3, 0, 0));
        stats.setOpaque(false);
        stats.add(statItem("单价", "¥" + String.format("%,.0f", g.getUnitPrice()), Theme.ORANGE));
        stats.add(statItem("库存", String.valueOf(g.getStock()), Theme.TEXT));
        stats.add(statItem("已售", String.valueOf(g.getSoldQty()), Theme.GREEN));

        card.add(header,  BorderLayout.NORTH);
        card.add(catRow,  BorderLayout.CENTER);
        card.add(stats,   BorderLayout.SOUTH);
        return card;
    }

    private JPanel statItem(String label, String value, Color valColor) {
        JPanel p = new JPanel(new BorderLayout(0, 3));
        p.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(Theme.FONT_SMALL);
        lbl.setForeground(Theme.TEXT_SUB);
        JLabel val = new JLabel(value);
        val.setFont(new Font("微软雅黑", Font.BOLD, 16));
        val.setForeground(valColor);
        p.add(lbl, BorderLayout.NORTH);
        p.add(val, BorderLayout.SOUTH);
        return p;
    }

    private JLabel categoryBadge(String cat) {
        if (cat == null || cat.isEmpty()) cat = "其他";
        Color bg, fg;
        if ("电子设备".equals(cat))      { bg = Theme.BLUE_BG;   fg = Theme.BLUE_TEXT; }
        else if ("电脑配件".equals(cat)) { bg = Theme.GREEN_BG;  fg = Theme.GREEN_TEXT; }
        else if ("存储设备".equals(cat)) { bg = Theme.PURPLE_BG; fg = Theme.PURPLE_TEXT; }
        else                             { bg = Theme.AMBER_BG;  fg = Theme.AMBER_TEXT; }
        return Theme.badge(cat, bg, fg);
    }

    private void showDialog(Goods goods) {
        boolean isNew = (goods == null);
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this),
            isNew ? "新增货物" : "编辑货物", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(480, 400);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Theme.CARD_BG);
        form.setBorder(new EmptyBorder(20, 24, 20, 24));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 8, 8, 8);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;

        JTextField fName  = field(isNew ? "" : goods.getGoodsName());
        String[] categories = {"电子设备", "电脑配件", "存储设备", "办公用品", "其他"};
        JComboBox<String> fCat = new JComboBox<>(categories);
        if (!isNew && goods.getCategory() != null) fCat.setSelectedItem(goods.getCategory());
        JTextField fUnit  = field(isNew ? "件" : goods.getUnit());
        JTextField fPrice = field(isNew ? "0.00" : String.valueOf(goods.getUnitPrice()));
        JTextField fStock = field(isNew ? "0" : String.valueOf(goods.getStock()));
        JTextField fDesc  = field(isNew ? "" : nvl(goods.getDescription()));
        fCat.setFont(Theme.FONT_REG);

        Component[] fields = {fName, fCat, fUnit, fPrice, fStock, fDesc};
        String[] labels = {"货物名称 *", "分类", "单位 *", "单价(¥) *", "库存 *", "描述"};
        for (int i = 0; i < labels.length; i++) {
            g.gridx = 0; g.gridy = i; g.weightx = 0;
            JLabel lbl = new JLabel(labels[i]);
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
            if (fName.getText().trim().isEmpty()) { JOptionPane.showMessageDialog(dlg, "货物名称不能为空！"); return; }
            try {
                double price = Double.parseDouble(fPrice.getText().trim());
                int stock = Integer.parseInt(fStock.getText().trim());
                Goods gd = isNew ? new Goods() : goods;
                gd.setGoodsName(fName.getText().trim());
                gd.setCategory((String) fCat.getSelectedItem());
                gd.setUnit(fUnit.getText().trim());
                gd.setUnitPrice(price);
                gd.setStock(stock);
                gd.setDescription(fDesc.getText().trim());
                if (isNew) dao.insert(gd); else dao.update(gd);
                loadData(); dlg.dispose();
                frame.showToast(isNew ? "货物添加成功！" : "货物已更新！", true);
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(dlg, "单价和库存请输入有效数字！");
            } catch (Exception ex) { JOptionPane.showMessageDialog(dlg, "操作失败：" + ex.getMessage()); }
        });
        dlg.setVisible(true);
    }

    private JTextField field(String val) {
        JTextField f = new JTextField(val, 20);
        f.setFont(Theme.FONT_REG);
        f.setBorder(new CompoundBorder(new RoundedBorder(Theme.BORDER_MED, 6), new EmptyBorder(7, 10, 7, 10)));
        return f;
    }

    private String nvl(String s) { return s != null ? s : ""; }
}
