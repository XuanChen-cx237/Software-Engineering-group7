package com.project.service;

import com.project.model.Budget;
import com.project.model.Transcation;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Budget service handles budget data business logic
 */
public class BudgetService {
    private List<Budget> budgets;
    private TranscationService transactionService;
    private int nextId;
    private List<BudgetObserver> observers = new ArrayList<>();

    /**
     * Constructor
     */
    public BudgetService(TranscationService transactionService) {
        this.budgets = new ArrayList<>();
        this.transactionService = transactionService;
        this.nextId = 1;
    }

    /**
     * Add an observer to receive notifications on data changes
     */
    public void addObserver(BudgetObserver observer) {
        observers.add(observer);
    }

    /**
     * Remove an observer
     */
    public void removeObserver(BudgetObserver observer) {
        observers.remove(observer);
    }

    /**
     * Notify all observers of data changes
     */
    private void notifyObservers() {
        for (BudgetObserver observer : observers) {
            observer.onBudgetDataChanged();
        }
    }

    /**
     * Add a budget
     */
    public void addBudget(Budget budget) {
        budget.setId(nextId++);
        budgets.add(budget);
        notifyObservers();
    }

    /**
     * Update an existing budget
     */
    public void updateBudget(Budget budget) {
        for (int i = 0; i < budgets.size(); i++) {
            if (budgets.get(i).getId() == budget.getId()) {
                budgets.set(i, budget);
                notifyObservers();
                break;
            }
        }
    }

    /**
     * Delete a budget
     */
    public void deleteBudget(int budgetId) {
        budgets.removeIf(budget -> budget.getId() == budgetId);
        notifyObservers();
    }

    /**
     * Get all budgets
     */
    public List<Budget> getAllBudgets() {
        return new ArrayList<>(budgets);
    }

    /**
     * Get a budget by ID
     */
    public Budget getBudgetById(int id) {
        return budgets.stream()
                .filter(budget -> budget.getId() == id)
                .findFirst()
                .orElse(null);
    }

    /**
     * Get budgets by category
     */
    public List<Budget> getBudgetsByCategory(String category) {
        return budgets.stream()
                .filter(budget -> budget.getCategory().equals(category))
                .collect(Collectors.toList());
    }

    /**
     * Calculate the spending for a specific budget (only current month expenses, no income)
     * @param budget The budget to calculate spending for
     * @return The total spending amount for this budget
     */
    public double calculateBudgetSpending(Budget budget) {
        // Get current month transactions
        List<Transcation> currentMonthTransactions = getCurrentMonthTransactions();

        // Filter by category and expenses only (no income)
        double totalSpending = 0.0;
        for (Transcation transaction : currentMonthTransactions) {
            if (!transaction.isIncome() && transaction.getCategory().equals(budget.getCategory())) {
                totalSpending += transaction.getAmount();
            }
        }

        return totalSpending;
    }

    /**
     * Calculate budget remaining amount
     * @param budget The budget to calculate for
     * @return Remaining amount (can be negative if overspent)
     */
    public double calculateBudgetRemaining(Budget budget) {
        double spent = calculateBudgetSpending(budget);
        return budget.getAmount() - spent;
    }

    /**
     * Calculate budget usage percentage
     * @param budget The budget to calculate for
     * @return Percentage of budget used (0-100, can be >100 if overspent)
     */
    public double calculateBudgetUsagePercentage(Budget budget) {
        double spent = calculateBudgetSpending(budget);
        if (budget.getAmount() <= 0) {
            return 0; // Avoid division by zero
        }
        return (spent / budget.getAmount()) * 100;
    }

    /**
     * Get budget summary data for all budgets (based on current month)
     * @return Map of budget categories with their spent/total amounts
     */
    public Map<String, Map<String, Double>> getBudgetSummary() {
        Map<String, Map<String, Double>> summary = new HashMap<>();

        for (Budget budget : getAllBudgets()) {
            // Skip total budget
            if (budget.getCategory().equals("Total Budget")) {
                continue;
            }

            String category = budget.getCategory();
            double budgetAmount = budget.getAmount();
            double spentAmount = calculateBudgetSpending(budget);

            Map<String, Double> categoryData = new HashMap<>();
            categoryData.put("budget", budgetAmount);
            categoryData.put("spent", spentAmount);
            categoryData.put("remaining", budgetAmount - spentAmount);

            summary.put(category, categoryData);
        }

        return summary;
    }

    /**
     * Get current month total expense (no income)
     */
    public double getCurrentMonthTotalExpense() {
        List<Transcation> currentMonthTransactions = getCurrentMonthTransactions();
        double totalExpense = 0;

        for (Transcation transaction : currentMonthTransactions) {
            if (!transaction.isIncome()) { // Only count expenses
                totalExpense += transaction.getAmount();
            }
        }

        return totalExpense;
    }

    /**
     * Get current month transactions
     */
    private List<Transcation> getCurrentMonthTransactions() {
        List<Transcation> allTransactions = transactionService.getAllTransactions();
        List<Transcation> currentMonthTransactions = new ArrayList<>();

        if (allTransactions.isEmpty()) {
            return currentMonthTransactions;
        }

        // Get current month's year and month
        Calendar cal = Calendar.getInstance();
        int currentYear = cal.get(Calendar.YEAR);
        int currentMonth = cal.get(Calendar.MONTH);

        // Filter current month transactions
        for (Transcation transaction : allTransactions) {
            if (transaction == null || transaction.getDate() == null) {
                continue;
            }

            // Get transaction date's year and month
            Calendar transCal = Calendar.getInstance();
            transCal.setTime(transaction.getDate());
            int transYear = transCal.get(Calendar.YEAR);
            int transMonth = transCal.get(Calendar.MONTH);

            // Only match year and month
            if (transYear == currentYear && transMonth == currentMonth) {
                currentMonthTransactions.add(transaction);
            }
        }

        return currentMonthTransactions;
    }
}