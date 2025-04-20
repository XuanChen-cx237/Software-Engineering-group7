package com.project.model;

import java.util.Date;

/**
 * 交易数据模型类
 */
public class Transcation {
    private int id;
    private Date date;
    private double amount;
    private boolean income; // true表示收入，false表示支出
    private String category;
    private String description;

    /**
     * 默认构造函数
     */
    public Transcation() {
        this.date = new Date(); // 默认为当前日期
    }

    /**
     * 带参数的构造函数
     */
    public Transcation(double amount, boolean income, String category, String description) {
        this.date = new Date();
        this.amount = amount;
        this.income = income;
        this.category = category;
        this.description = description;
    }

    // Getter和Setter方法
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public boolean isIncome() {
        return income;
    }

    public void setIncome(boolean income) {
        this.income = income;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Transaction [id=" + id + ", date=" + date + ", amount=" + amount +
                ", type=" + (income ? "收入" : "支出") +
                ", category=" + category + ", description=" + description + "]";
    }
}