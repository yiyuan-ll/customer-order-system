package com.order.model;

public class Customer {
    private int customerId;
    private String name;
    private String contact;
    private String phone;
    private String email;
    private String address;
    private String createdAt;
    // computed fields from joins
    private int orderCount;
    private double totalAmount;

    public Customer() {}
    public Customer(int customerId, String name, String contact, String phone, String email, String address, String createdAt) {
        this.customerId = customerId; this.name = name; this.contact = contact;
        this.phone = phone; this.email = email; this.address = address; this.createdAt = createdAt;
    }

    public int getCustomerId()        { return customerId; }
    public void setCustomerId(int v)  { this.customerId = v; }
    public String getName()           { return name; }
    public void setName(String v)     { this.name = v; }
    public String getContact()        { return contact; }
    public void setContact(String v)  { this.contact = v; }
    public String getPhone()          { return phone; }
    public void setPhone(String v)    { this.phone = v; }
    public String getEmail()          { return email; }
    public void setEmail(String v)    { this.email = v; }
    public String getAddress()        { return address; }
    public void setAddress(String v)  { this.address = v; }
    public String getCreatedAt()      { return createdAt; }
    public void setCreatedAt(String v){ this.createdAt = v; }
    public int getOrderCount()        { return orderCount; }
    public void setOrderCount(int v)  { this.orderCount = v; }
    public double getTotalAmount()    { return totalAmount; }
    public void setTotalAmount(double v){ this.totalAmount = v; }

    @Override public String toString() { return name; }
}
