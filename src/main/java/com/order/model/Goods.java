package com.order.model;

public class Goods {
    private int goodsId;
    private String goodsName;
    private String category;
    private String unit;
    private double unitPrice;
    private int stock;
    private String description;
    private String createdAt;
    private int soldQty; // computed

    public Goods() {}
    public Goods(int goodsId, String goodsName, String category, String unit, double unitPrice, int stock, String description, String createdAt) {
        this.goodsId = goodsId; this.goodsName = goodsName; this.category = category;
        this.unit = unit; this.unitPrice = unitPrice; this.stock = stock;
        this.description = description; this.createdAt = createdAt;
    }

    public int getGoodsId()            { return goodsId; }
    public void setGoodsId(int v)      { this.goodsId = v; }
    public String getGoodsName()       { return goodsName; }
    public void setGoodsName(String v) { this.goodsName = v; }
    public String getCategory()        { return category; }
    public void setCategory(String v)  { this.category = v; }
    public String getUnit()            { return unit; }
    public void setUnit(String v)      { this.unit = v; }
    public double getUnitPrice()       { return unitPrice; }
    public void setUnitPrice(double v) { this.unitPrice = v; }
    public int getStock()              { return stock; }
    public void setStock(int v)        { this.stock = v; }
    public String getDescription()     { return description; }
    public void setDescription(String v){ this.description = v; }
    public String getCreatedAt()       { return createdAt; }
    public void setCreatedAt(String v) { this.createdAt = v; }
    public int getSoldQty()            { return soldQty; }
    public void setSoldQty(int v)      { this.soldQty = v; }

    @Override public String toString() { return goodsName + " (¥" + unitPrice + "/" + unit + ")"; }
}
