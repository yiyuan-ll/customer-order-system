package com.order.model;

public class Invoice {
    private int invoiceId;
    private int orderId;
    private String customerName;
    private String invoiceNo;
    private String issueDate;
    private double amount;
    private double taxRate;
    private double taxAmount;
    private double totalAmount;
    private String status;

    public Invoice() {}

    public int getInvoiceId()           { return invoiceId; }
    public void setInvoiceId(int v)     { this.invoiceId = v; }
    public int getOrderId()             { return orderId; }
    public void setOrderId(int v)       { this.orderId = v; }
    public String getCustomerName()     { return customerName; }
    public void setCustomerName(String v){ this.customerName = v; }
    public String getInvoiceNo()        { return invoiceNo; }
    public void setInvoiceNo(String v)  { this.invoiceNo = v; }
    public String getIssueDate()        { return issueDate; }
    public void setIssueDate(String v)  { this.issueDate = v; }
    public double getAmount()           { return amount; }
    public void setAmount(double v)     { this.amount = v; }
    public double getTaxRate()          { return taxRate; }
    public void setTaxRate(double v)    { this.taxRate = v; }
    public double getTaxAmount()        { return taxAmount; }
    public void setTaxAmount(double v)  { this.taxAmount = v; }
    public double getTotalAmount()      { return totalAmount; }
    public void setTotalAmount(double v){ this.totalAmount = v; }
    public String getStatus()           { return status; }
    public void setStatus(String v)     { this.status = v; }
}
