package com.project.service;

import com.project.model.Transcation;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Transaction service handles transaction data business logic
 */
public class TranscationService {
    private List<Transcation> transactions;
    private int nextId;
    private List<TransactionObserver> observers = new ArrayList<>();

    /**
     * Constructor
     */
    public TranscationService() {
        this.transactions = new ArrayList<>();
        this.nextId = 1;

    }

    /**
     * Add an observer to receive notifications on data changes
     */
    public void addObserver(TransactionObserver observer) {
        observers.add(observer);
    }

    /**
     * Remove an observer
     */
    public void removeObserver(TransactionObserver observer) {
        observers.remove(observer);
    }

    /**
     * Notify all observers of data changes
     */
    private void notifyObservers() {
        for (TransactionObserver observer : observers) {
            observer.onTransactionDataChanged();
        }
    }

    /**
     * Add a transaction
     */
    public void addTransaction(Transcation transaction) {
        transaction.setId(nextId++);
        transactions.add(transaction);
        notifyObservers();
    }

    /**
     * Get all transactions
     * @return Sorted list of transactions (most recent first)
     */
    public List<Transcation> getAllTransactions() {
        // Return a sorted copy of the transactions (most recent first)
        return transactions.stream()
                .sorted(Comparator.comparing(Transcation::getDate).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Get transactions by category
     */
    public List<Transcation> getTransactionsByCategory(String category) {
        return transactions.stream()
                .filter(t -> t.getCategory().equals(category))
                .collect(Collectors.toList());
    }

    /**
     * Get transactions by type
     */
    public List<Transcation> getTransactionsByType(boolean income) {
        return transactions.stream()
                .filter(t -> t.isIncome() == income)
                .collect(Collectors.toList());
    }

    /**
     * Get transactions by date range
     */
    public List<Transcation> getTransactionsByDateRange(Date startDate, Date endDate) {
        return transactions.stream()
                .filter(t -> !t.getDate().before(startDate) && !t.getDate().after(endDate))
                .collect(Collectors.toList());
    }

    /**
     * Get total income
     */
    public double getTotalIncome() {
        return transactions.stream()
                .filter(Transcation::isIncome)
                .mapToDouble(Transcation::getAmount)
                .sum();
    }

    /**
     * Get total expense
     */
    public double getTotalExpense() {
        return transactions.stream()
                .filter(t -> !t.isIncome())
                .mapToDouble(Transcation::getAmount)
                .sum();
    }

    /**
     * Get net balance
     */
    public double getNetBalance() {
        return getTotalIncome() - getTotalExpense();
    }

    /**
     * Get monthly income data
     * @return Map with month as key and total income as value, sorted by date
     */
    public Map<String, Double> getMonthlyIncome() {
        // 使用特定的月份格式，确保可以按年月排序
        SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM");

        // 收集所有月份数据，用于确保图表显示时间连续性
        Set<String> allMonths = new TreeSet<>();
        Calendar cal = Calendar.getInstance();

        // 找出最早和最晚的日期
        Date earliestDate = null;
        Date latestDate = null;

        for (Transcation transaction : transactions) {
            Date date = transaction.getDate();
            if (earliestDate == null || date.before(earliestDate)) {
                earliestDate = date;
            }
            if (latestDate == null || date.after(latestDate)) {
                latestDate = date;
            }
        }

        // 如果有交易数据，生成所有月份
        if (earliestDate != null && latestDate != null) {
            Calendar startCal = Calendar.getInstance();
            startCal.setTime(earliestDate);
            startCal.set(Calendar.DAY_OF_MONTH, 1);

            Calendar endCal = Calendar.getInstance();
            endCal.setTime(latestDate);
            endCal.set(Calendar.DAY_OF_MONTH, 1);

            while (!startCal.after(endCal)) {
                allMonths.add(monthFormat.format(startCal.getTime()));
                startCal.add(Calendar.MONTH, 1);
            }
        }

        // 创建按月排序的收入数据
        Map<String, Double> monthlyData = new TreeMap<>();

        // 初始化所有月份的值为0
        for (String month : allMonths) {
            monthlyData.put(month, 0.0);
        }

        // 累加收入数据
        for (Transcation transaction : transactions) {
            if (transaction.isIncome()) {
                String monthYear = monthFormat.format(transaction.getDate());
                monthlyData.put(monthYear, monthlyData.getOrDefault(monthYear, 0.0) + transaction.getAmount());
            }
        }

        return monthlyData;
    }

    /**
     * Get monthly expense data
     * @return Map with month as key and total expense as value, sorted by date
     */
    public Map<String, Double> getMonthlyExpenses() {
        // 使用特定的月份格式，确保可以按年月排序
        SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM");

        // 收集所有月份数据，用于确保图表显示时间连续性
        Set<String> allMonths = new TreeSet<>();
        Calendar cal = Calendar.getInstance();

        // 找出最早和最晚的日期
        Date earliestDate = null;
        Date latestDate = null;

        for (Transcation transaction : transactions) {
            Date date = transaction.getDate();
            if (earliestDate == null || date.before(earliestDate)) {
                earliestDate = date;
            }
            if (latestDate == null || date.after(latestDate)) {
                latestDate = date;
            }
        }

        // 如果有交易数据，生成所有月份
        if (earliestDate != null && latestDate != null) {
            Calendar startCal = Calendar.getInstance();
            startCal.setTime(earliestDate);
            startCal.set(Calendar.DAY_OF_MONTH, 1);

            Calendar endCal = Calendar.getInstance();
            endCal.setTime(latestDate);
            endCal.set(Calendar.DAY_OF_MONTH, 1);

            while (!startCal.after(endCal)) {
                allMonths.add(monthFormat.format(startCal.getTime()));
                startCal.add(Calendar.MONTH, 1);
            }
        }

        // 创建按月排序的支出数据
        Map<String, Double> monthlyData = new TreeMap<>();

        // 初始化所有月份的值为0
        for (String month : allMonths) {
            monthlyData.put(month, 0.0);
        }

        // 累加支出数据
        for (Transcation transaction : transactions) {
            if (!transaction.isIncome()) {
                String monthYear = monthFormat.format(transaction.getDate());
                monthlyData.put(monthYear, monthlyData.getOrDefault(monthYear, 0.0) + transaction.getAmount());
            }
        }

        return monthlyData;
    }

}