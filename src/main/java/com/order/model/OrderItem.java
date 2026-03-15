package com.order.model;

public class OrderItem {
    private int itemId;
    private int orderId;
    private int goodsId;
    private String goodsName;
    private String unit;
    private int quantity;
    private double unitPrice;
    private double subtotal;

    public OrderItem() {}
    public OrderItem(int goodsId, String goodsName, String unit, int quantity, double unitPrice) {
        this.goodsId = goodsId; this.goodsName = goodsName; this.unit = unit;
        this.quantity = quantity; this.unitPrice = unitPrice;
        this.subtotal = quantity * unitPrice;
    }

    public int getItemId()              { return itemId; }
    public void setItemId(int v)        { this.itemId = v; }
    public int getOrderId()             { return orderId; }
    public void setOrderId(int v)       { this.orderId = v; }
    public int getGoodsId()             { return goodsId; }
    public void setGoodsId(int v)       { this.goodsId = v; }
    public String getGoodsName()        { return goodsName; }
    public void setGoodsName(String v)  { this.goodsName = v; }
    public String getUnit()             { return unit; }
    public void setUnit(String v)       { this.unit = v; }
    public int getQuantity()            { return quantity; }
    public void setQuantity(int v)      { this.quantity = v; this.subtotal = v * unitPrice; }
    public double getUnitPrice()        { return unitPrice; }
    public void setUnitPrice(double v)  { this.unitPrice = v; this.subtotal = quantity * v; }
    public double getSubtotal()         { return subtotal; }
    public void setSubtotal(double v)   { this.subtotal = v; }
}
