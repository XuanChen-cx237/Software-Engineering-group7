package com.project.gui;

import com.project.model.Budget;
import com.project.model.Transcation;
import com.project.service.BudgetObserver;
import com.project.service.BudgetService;
import com.project.service.TransactionObserver;
import com.project.service.TranscationService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * Budget management panel
 * Shows budget settings and usage, only counts current month expenses
 */
public class BudgetPanel extends JPanel implements BudgetObserver, TransactionObserver {
    private BudgetService budgetService;
    private TranscationService transactionService;

    // Top label
    private JLabel titleLabel;

    // Budget settings panel components
    private JPanel budgetSettingsPanel;
    private JTextField totalBudgetField;
    private JProgressBar totalProgressBar;
    private JLabel totalSpentLabel;
    private JLabel totalRemainingLabel;
    private JTable budgetTable;
    private DefaultTableModel tableModel;

    // Predefined expense categories
    private final String[] expenseCategories = {"Food", "Transport", "Housing", "Entertainment", "Utilities", "Education", "Healthcare", "Shopping", "Other"};

    // Table column names
    private final String[] columnNames = {"Category", "Budget Amount", "Spent", "Remaining", "Usage", "Description"};

    /**
     * Constructor
     */
    public BudgetPanel(BudgetService budgetService, TranscationService transactionService) {
        this.budgetService = budgetService;
        this.transactionService = transactionService;
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // Register as observer
        budgetService.addObserver(this);
        transactionService.addObserver(this);

        // Create title
        titleLabel = new JLabel("Budget Management", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
        add(titleLabel, BorderLayout.NORTH);

        // Create budget settings panel
        budgetSettingsPanel = createBudgetSettingsPanel();
        add(budgetSettingsPanel, BorderLayout.CENTER);

        // Update data display
        updateBudgetData();
    }

    /**
     * Budget data change callback
     */
    @Override
    public void onBudgetDataChanged() {
        updateBudgetData();
    }

    /**
     * Transaction data change callback
     */
    @Override
    public void onTransactionDataChanged() {
        updateBudgetData();
    }

    /**
     * Create budget settings panel
     */
    private JPanel createBudgetSettingsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));

        // Create total budget panel
        JPanel totalBudgetPanel = createTotalBudgetPanel();
        panel.add(totalBudgetPanel, BorderLayout.NORTH);

        // Create category budget table panel
        JPanel budgetTablePanel = createBudgetTablePanel();
        panel.add(budgetTablePanel, BorderLayout.CENTER);

        // Create button panel
        JPanel buttonPanel = createButtonPanel();
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Create total budget panel
     */
    private JPanel createTotalBudgetPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                "Total Budget",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14)
        ));

        // Create input panel
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JLabel label = new JLabel("Set Monthly Total Budget: $");
        label.setFont(new Font("Arial", Font.PLAIN, 14));
        inputPanel.add(label);

        totalBudgetField = new JTextField(10);
        totalBudgetField.setFont(new Font("Arial", Font.PLAIN, 14));
        inputPanel.add(totalBudgetField);

        JButton setTotalBudgetButton = new JButton("Set");
        setTotalBudgetButton.addActionListener(e -> updateTotalBudget());
        inputPanel.add(setTotalBudgetButton);

        panel.add(inputPanel, BorderLayout.NORTH);

        // Create progress panel
        JPanel progressPanel = new JPanel(new GridLayout(3, 1, 0, 5));
        progressPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Total progress bar
        totalProgressBar = new JProgressBar(0, 100);
        totalProgressBar.setStringPainted(true);
        totalProgressBar.setFont(new Font("Arial", Font.PLAIN, 12));
        totalProgressBar.setPreferredSize(new Dimension(totalProgressBar.getPreferredSize().width, 25));

        // Expense and remaining labels
        totalSpentLabel = new JLabel("Current Month Spent: $0.00", SwingConstants.LEFT);
        totalSpentLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        totalRemainingLabel = new JLabel("Remaining: $0.00", SwingConstants.LEFT);
        totalRemainingLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        progressPanel.add(totalProgressBar);
        progressPanel.add(totalSpentLabel);
        progressPanel.add(totalRemainingLabel);

        panel.add(progressPanel, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Create budget table panel
     */
    private JPanel createBudgetTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                "Category Budgets",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14)
        ));

        // Create table model
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make cells non-editable
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 4) { // "Usage" column
                    return Double.class;
                }
                return Object.class;
            }
        };

        // Create table
        budgetTable = new JTable(tableModel);
        budgetTable.setRowHeight(30);
        budgetTable.getTableHeader().setReorderingAllowed(false);
        budgetTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Set progress bar renderer
        budgetTable.getColumnModel().getColumn(4).setCellRenderer(new ProgressBarRenderer());

        // Set right alignment for amount columns
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        budgetTable.getColumnModel().getColumn(1).setCellRenderer(rightRenderer);
        budgetTable.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
        budgetTable.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);

        JScrollPane scrollPane = new JScrollPane(budgetTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Create button panel
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        panel.setBorder(new EmptyBorder(10, 0, 0, 0));

        JButton addButton = new JButton("Add Budget");
        addButton.setFont(new Font("Arial", Font.PLAIN, 14));
        addButton.addActionListener(e -> showAddBudgetDialog());

        JButton editButton = new JButton("Edit Budget");
        editButton.setFont(new Font("Arial", Font.PLAIN, 14));
        editButton.addActionListener(e -> editSelectedBudget());

        JButton deleteButton = new JButton("Delete Budget");
        deleteButton.setFont(new Font("Arial", Font.PLAIN, 14));
        deleteButton.addActionListener(e -> deleteSelectedBudget());

        panel.add(addButton);
        panel.add(editButton);
        panel.add(deleteButton);

        return panel;
    }

    /**
     * Update total budget
     */
    private void updateTotalBudget() {
        try {
            double amount = Double.parseDouble(totalBudgetField.getText());
            if (amount <= 0) {
                JOptionPane.showMessageDialog(this,
                        "Please enter a budget amount greater than zero",
                        "Input Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Check if total budget already exists
            Budget totalBudget = null;
            List<Budget> existingBudgets = budgetService.getAllBudgets();

            for (Budget budget : existingBudgets) {
                if (budget.getCategory().equals("Total Budget")) {
                    totalBudget = budget;
                    break;
                }
            }

            if (totalBudget == null) {
                // Create new total budget
                totalBudget = new Budget();
                totalBudget.setCategory("Total Budget");
                totalBudget.setAmount(amount);
                totalBudget.setDescription("Monthly total budget");
                budgetService.addBudget(totalBudget);
            } else {
                // Update existing total budget
                totalBudget.setAmount(amount);
                budgetService.updateBudget(totalBudget);
            }

            JOptionPane.showMessageDialog(this,
                    "Total budget updated successfully",
                    "Update Success",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a valid number",
                    "Input Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Show add budget dialog
     */
    private void showAddBudgetDialog() {
        Budget budget = new Budget();
        if (showBudgetDialog(budget, "Add Category Budget")) {
            budgetService.addBudget(budget);
        }
    }

    /**
     * Edit selected budget
     */
    private void editSelectedBudget() {
        int selectedRow = budgetTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select a budget category first",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String category = (String) tableModel.getValueAt(selectedRow, 0);
        Budget selectedBudget = null;

        for (Budget budget : budgetService.getAllBudgets()) {
            if (budget.getCategory().equals(category)) {
                selectedBudget = budget;
                break;
            }
        }

        if (selectedBudget != null && showBudgetDialog(selectedBudget, "Edit Category Budget")) {
            budgetService.updateBudget(selectedBudget);
        }
    }

    /**
     * Delete selected budget
     */
    private void deleteSelectedBudget() {
        int selectedRow = budgetTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select a budget category first",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String category = (String) tableModel.getValueAt(selectedRow, 0);

        // Find budget ID
        int budgetId = -1;
        for (Budget budget : budgetService.getAllBudgets()) {
            if (budget.getCategory().equals(category)) {
                budgetId = budget.getId();
                break;
            }
        }

        if (budgetId != -1) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete the budget for " + category + "?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                budgetService.deleteBudget(budgetId);
            }
        }
    }

    /**
     * Show budget dialog
     */
    private boolean showBudgetDialog(Budget budget, String title) {
        // Create dialog
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), title, true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        // Create form panel
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        formPanel.setBorder(new EmptyBorder(15, 15, 5, 15));

        // Category selection
        JLabel categoryLabel = new JLabel("Category:");
        JComboBox<String> categoryCombo = new JComboBox<>(expenseCategories);

        // If editing, set current category
        if (budget.getCategory() != null && !budget.getCategory().equals("Total Budget")) {
            categoryCombo.setSelectedItem(budget.getCategory());
        }

        // Budget amount
        JLabel amountLabel = new JLabel("Budget Amount ($):");
        JTextField amountField = new JTextField(10);
        if (budget.getAmount() > 0) {
            amountField.setText(String.format("%.2f", budget.getAmount()));
        }

        // Description
        JLabel descLabel = new JLabel("Description:");
        JTextField descField = new JTextField(budget.getDescription());

        // Start date
        JLabel startDateLabel = new JLabel("Start Date:");
        JPanel startDatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));

        // For simplicity, use first day of current month
        JLabel startDateValue = new JLabel(new SimpleDateFormat("yyyy-MM-01").format(new Date()));
        startDatePanel.add(startDateValue);

        // End date
        JLabel endDateLabel = new JLabel("End Date:");
        JPanel endDatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));

        // For simplicity, use last day of current month
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        JLabel endDateValue = new JLabel(new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime()));
        endDatePanel.add(endDateValue);

        // Add components to form
        formPanel.add(categoryLabel);
        formPanel.add(categoryCombo);
        formPanel.add(amountLabel);
        formPanel.add(amountField);
        formPanel.add(startDateLabel);
        formPanel.add(startDatePanel);
        formPanel.add(endDateLabel);
        formPanel.add(endDatePanel);
        formPanel.add(descLabel);
        formPanel.add(descField);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(new EmptyBorder(5, 15, 10, 15));

        JButton cancelButton = new JButton("Cancel");
        JButton saveButton = new JButton("Save");

        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        // Dialog result
        final boolean[] result = {false};

        // Cancel button
        cancelButton.addActionListener(e -> dialog.dispose());

        // Save button
        saveButton.addActionListener(e -> {
            try {
                // Validate input
                double amount = Double.parseDouble(amountField.getText());
                if (amount <= 0) {
                    JOptionPane.showMessageDialog(dialog,
                            "Please enter a budget amount greater than zero",
                            "Input Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Update budget object
                budget.setCategory((String) categoryCombo.getSelectedItem());
                budget.setAmount(amount);
                budget.setDescription(descField.getText());

                // Set dates
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    budget.setStartDate(sdf.parse(startDateValue.getText()));
                    budget.setEndDate(sdf.parse(endDateValue.getText()));
                } catch (Exception ex) {
                    Calendar calendar = Calendar.getInstance();
                    // Set to first day of month
                    calendar.set(Calendar.DAY_OF_MONTH, 1);
                    budget.setStartDate(calendar.getTime());
                    // Set to last day of month
                    calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                    budget.setEndDate(calendar.getTime());
                }

                result[0] = true;
                dialog.dispose();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog,
                        "Please enter a valid number",
                        "Input Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        // Add panels to dialog
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        // Show dialog
        dialog.setVisible(true);

        return result[0];
    }

    /**
     * Update budget data display
     */
    private void updateBudgetData() {
        updateTotalBudgetInfo();
        updateBudgetTable();
    }

    /**
     * Get current month transactions
     */
    private List<Transcation> getCurrentMonthTransactions() {
        List<Transcation> allTransactions = transactionService.getAllTransactions();
        List<Transcation> currentMonthTransactions = new ArrayList<>();

        if (allTransactions.isEmpty()) {
            System.out.println("No transaction data");
            return currentMonthTransactions;
        }

        // Get current month's year and month
        Calendar cal = Calendar.getInstance();
        int currentYear = cal.get(Calendar.YEAR);
        int currentMonth = cal.get(Calendar.MONTH);

        System.out.println("\n===== Finding current month(" + currentYear + "-" + (currentMonth + 1) + ") transactions =====");

        // Filter current month transactions
        for (Transcation transaction : allTransactions) {
            if (transaction == null || transaction.getDate() == null) {
                System.out.println("Skipping: transaction or date is null");
                continue;
            }

            // Get transaction date's year and month
            Calendar transCal = Calendar.getInstance();
            transCal.setTime(transaction.getDate());
            int transYear = transCal.get(Calendar.YEAR);
            int transMonth = transCal.get(Calendar.MONTH);

            // Only match year and month
            boolean isCurrentMonth = (transYear == currentYear && transMonth == currentMonth);

            System.out.println("Transaction: " +
                    "ID=" + transaction.getId() +
                    ", Category=" + transaction.getCategory() +
                    ", Amount=" + transaction.getAmount() +
                    ", IsIncome=" + transaction.isIncome() +
                    ", Date=" + new SimpleDateFormat("yyyy-MM-dd").format(transaction.getDate()) +
                    ", IsCurrentMonth=" + isCurrentMonth);

            if (isCurrentMonth) {
                currentMonthTransactions.add(transaction);
            }
        }

        System.out.println("Current month transactions count: " + currentMonthTransactions.size());
        return currentMonthTransactions;
    }

    /**
     * Calculate current month total expense (excluding income)
     */
    private double getCurrentMonthTotalExpense() {
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
     * Calculate current month category expenses (excluding income)
     */
    private Map<String, Double> getCurrentMonthCategoryExpenses() {
        List<Transcation> currentMonthTransactions = getCurrentMonthTransactions();
        Map<String, Double> categoryExpenses = new HashMap<>();

        System.out.println("\n===== Calculating category expenses =====");
        System.out.println("Current month transactions count: " + currentMonthTransactions.size());

        for (Transcation transaction : currentMonthTransactions) {
            System.out.println("Processing transaction: " +
                    "Category=" + transaction.getCategory() +
                    ", Amount=" + transaction.getAmount() +
                    ", IsIncome=" + transaction.isIncome());

            if (!transaction.isIncome()) { // Only count expenses, exclude income
                String category = transaction.getCategory();
                double amount = transaction.getAmount();
                Double currentTotal = categoryExpenses.getOrDefault(category, 0.0);
                categoryExpenses.put(category, currentTotal + amount);

                System.out.println("  → Category[" + category + "] cumulative expense: " + (currentTotal + amount));
            } else {
                System.out.println("  → Skipped (income)");
            }
        }

        System.out.println("Category expenses results:");
        for (Map.Entry<String, Double> entry : categoryExpenses.entrySet()) {
            System.out.println("  - " + entry.getKey() + ": " + entry.getValue());
        }
        System.out.println("========================\n");

        return categoryExpenses;
    }

    /**
     * Update total budget info
     */
    private void updateTotalBudgetInfo() {
        double totalBudgetAmount = 0;

        // Find total budget
        for (Budget budget : budgetService.getAllBudgets()) {
            if (budget.getCategory().equals("Total Budget")) {
                totalBudgetAmount = budget.getAmount();
                totalBudgetField.setText(String.format("%.2f", totalBudgetAmount));
                break;
            }
        }

        // Calculate current month total expense (expenses only, not income)
        double totalSpent = getCurrentMonthTotalExpense();
        double remaining = totalBudgetAmount - totalSpent;
        double percentage = (totalBudgetAmount > 0) ? (totalSpent / totalBudgetAmount) * 100 : 0;

        // Update UI
        DecimalFormat df = new DecimalFormat("#,##0.00");
        totalSpentLabel.setText("Current Month Spent: $" + df.format(totalSpent));
        totalRemainingLabel.setText("Remaining: $" + df.format(remaining));

        // Update progress bar
        totalProgressBar.setValue((int) percentage);

        // Set colors
        if (percentage > 100) {
            totalProgressBar.setForeground(new Color(231, 76, 60)); // Red
            totalProgressBar.setString(String.format("%.1f%% (Over Budget)", percentage));
            totalRemainingLabel.setForeground(new Color(231, 76, 60));
        } else if (percentage > 85) {
            totalProgressBar.setForeground(new Color(243, 156, 18)); // Yellow/orange
            totalProgressBar.setString(String.format("%.1f%%", percentage));
            totalRemainingLabel.setForeground(new Color(243, 156, 18));
        } else {
            totalProgressBar.setForeground(new Color(46, 204, 113)); // Green
            totalProgressBar.setString(String.format("%.1f%%", percentage));
            totalRemainingLabel.setForeground(new Color(46, 204, 113));
        }
    }

    /**
     * Update budget table
     */
    private void updateBudgetTable() {
        // Clear table
        tableModel.setRowCount(0);

        // Get all budgets
        List<Budget> allBudgets = budgetService.getAllBudgets();

        // Filter category budgets (exclude total budget)
        List<Budget> categoryBudgets = new ArrayList<>();
        for (Budget budget : allBudgets) {
            if (!budget.getCategory().equals("Total Budget")) {
                categoryBudgets.add(budget);
            }
        }

        // Get current month category expenses (only expenses, not income)
        Map<String, Double> categoryExpenses = getCurrentMonthCategoryExpenses();

        // Add budgets to table
        DecimalFormat df = new DecimalFormat("#,##0.00");

        for (Budget budget : categoryBudgets) {
            String category = budget.getCategory();
            double budgetAmount = budget.getAmount();

            // Get current month expense for this category (0 if none)
            double spentAmount = categoryExpenses.getOrDefault(category, 0.0);

            double remainingAmount = budgetAmount - spentAmount;
            double percentage = (budgetAmount > 0) ? (spentAmount / budgetAmount) * 100 : 0;

            Object[] rowData = {
                    category,
                    "$" + df.format(budgetAmount),
                    "$" + df.format(spentAmount),
                    "$" + df.format(remainingAmount),
                    percentage, // This will be rendered as a progress bar
                    budget.getDescription()
            };

            tableModel.addRow(rowData);
        }
    }

    /**
     * Progress bar renderer
     */
    private class ProgressBarRenderer extends DefaultTableCellRenderer {
        private JProgressBar progressBar;

        public ProgressBarRenderer() {
            progressBar = new JProgressBar(0, 100);
            progressBar.setStringPainted(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            if (value instanceof Double) {
                double percentage = (Double) value;
                progressBar.setValue((int) percentage);

                // Set colors
                if (percentage > 100) {
                    progressBar.setForeground(new Color(231, 76, 60)); // Red
                    progressBar.setString(String.format("%.1f%% (Over)", percentage));
                } else if (percentage > 85) {
                    progressBar.setForeground(new Color(243, 156, 18)); // Yellow/orange
                    progressBar.setString(String.format("%.1f%%", percentage));
                } else {
                    progressBar.setForeground(new Color(46, 204, 113)); // Green
                    progressBar.setString(String.format("%.1f%%", percentage));
                }
            }

            return progressBar;
        }
    }
}