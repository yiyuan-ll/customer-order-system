package com.order.model;

import java.util.List;
import java.util.ArrayList;

public class Order {
    private int orderId;
    private int customerId;
    private String customerName;
    private String orderDate;
    private double totalAmount;
    private String status;
    private String remark;
    private List<OrderItem> items = new ArrayList<>();

    public Order() {}

    public int getOrderId()             { return orderId; }
    public void setOrderId(int v)       { this.orderId = v; }
    public int getCustomerId()          { return customerId; }
    public void setCustomerId(int v)    { this.customerId = v; }
    public String getCustomerName()     { return customerName; }
    public void setCustomerName(String v){ this.customerName = v; }
    public String getOrderDate()        { return orderDate; }
    public void setOrderDate(String v)  { this.orderDate = v; }
    public double getTotalAmount()      { return totalAmount; }
    public void setTotalAmount(double v){ this.totalAmount = v; }
    public String getStatus()           { return status; }
    public void setStatus(String v)     { this.status = v; }
    public String getRemark()           { return remark; }
    public void setRemark(String v)     { this.remark = v; }
    public List<OrderItem> getItems()   { return items; }
    public void setItems(List<OrderItem> v){ this.items = v; }
}
