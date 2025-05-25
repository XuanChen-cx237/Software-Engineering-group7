package com.project.model;

import java.util.Date;

/**
 * 预算数据模型类
 */
public class Budget {
    private int id;
    private String category;
    private double amount;
    private Date startDate;
    private Date endDate;
    private String description;

    /**
     * 默认构造函数
     */
    public Budget() {
        // 默认日期为当前月份
        this.startDate = getFirstDayOfCurrentMonth();
        this.endDate = getLastDayOfCurrentMonth();
    }

    /**
     * 带参数的构造函数
     */
    public Budget(String category, double amount, Date startDate, Date endDate, String description) {
        this.category = category;
        this.amount = amount;
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;
    }

    // Getter和Setter方法
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * 获取当前月份的第一天
     */
    private Date getFirstDayOfCurrentMonth() {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.set(java.util.Calendar.DAY_OF_MONTH, 1);
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0);
        calendar.set(java.util.Calendar.MINUTE, 0);
        calendar.set(java.util.Calendar.SECOND, 0);
        calendar.set(java.util.Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    /**
     * 获取当前月份的最后一天
     */
    private Date getLastDayOfCurrentMonth() {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.set(java.util.Calendar.DAY_OF_MONTH, calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH));
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 23);
        calendar.set(java.util.Calendar.MINUTE, 59);
        calendar.set(java.util.Calendar.SECOND, 59);
        calendar.set(java.util.Calendar.MILLISECOND, 999);
        return calendar.getTime();
    }

    @Override
    public String toString() {
        return "Budget [id=" + id + ", category=" + category + ", amount=" + amount +
                ", startDate=" + startDate + ", endDate=" + endDate +
                ", description=" + description + "]";
    }
}