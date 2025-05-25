package com.project.gui;

import com.project.model.Budget;
import com.project.model.Transcation;
import com.project.service.BudgetService;
import com.project.service.TranscationService;
import com.project.service.TransactionObserver;
import com.project.util.AIConsultant;
import com.project.util.StreamCallback;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * AI Panel for financial advice and analysis
 * 支持流式显示AI响应
 */
public class AIPanel extends JPanel implements TransactionObserver {
    private BudgetService budgetService;
    private TranscationService transactionService;

    // UI Components
    private JTextArea aiResponseArea;
    private JButton consultButton;
    private JLabel statusLabel;
    private JProgressBar analysisProgressBar;

    // 新增成员变量：AI咨询实例
    private AIConsultant aiConsultant;

    /**
     * Constructor
     */
    public AIPanel(BudgetService budgetService, TranscationService transactionService) {
        this.budgetService = budgetService;
        this.transactionService = transactionService;

        // 初始化AI咨询实例
        this.aiConsultant = new AIConsultant();

        // Register as observer to get updates when transactions change
        transactionService.addObserver(this);

        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // Create and add components
        createComponents();
    }

    /**
     * Create panel components
     */
    private void createComponents() {
        // Create header panel
        JPanel headerPanel = new JPanel(new BorderLayout(10, 10));

        JLabel titleLabel = new JLabel("AI Financial Analysis", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        headerPanel.add(titleLabel, BorderLayout.NORTH);

        JLabel descriptionLabel = new JLabel("<html><div style='text-align: center; width: 500px;'>" +
                "Get personalized financial advice based on your current budget and spending patterns. " +
                "Our AI will analyze your data and provide recommendations to help you manage your finances better." +
                "</div></html>", SwingConstants.CENTER);
        descriptionLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        headerPanel.add(descriptionLabel, BorderLayout.CENTER);

        add(headerPanel, BorderLayout.NORTH);

        // Create main panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 0, 10, 0));

        // Create status panel
        JPanel statusPanel = new JPanel(new BorderLayout(5, 5));
        statusPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                "Analysis Status",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14)
        ));

        statusLabel = new JLabel("Ready to analyze your financial data");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        statusPanel.add(statusLabel, BorderLayout.CENTER);

        analysisProgressBar = new JProgressBar(0, 100);
        analysisProgressBar.setIndeterminate(true); // 设置为不确定进度模式
        analysisProgressBar.setVisible(false);
        statusPanel.add(analysisProgressBar, BorderLayout.SOUTH);

        mainPanel.add(statusPanel, BorderLayout.NORTH);

        // Create response area
        aiResponseArea = new JTextArea();
        aiResponseArea.setEditable(false);
        aiResponseArea.setLineWrap(true);
        aiResponseArea.setWrapStyleWord(true);
        aiResponseArea.setFont(new Font("Arial", Font.PLAIN, 14));
        aiResponseArea.setText("Your financial analysis will appear here. Click 'Get AI Analysis' to analyze your budget and spending data.");

        JScrollPane scrollPane = new JScrollPane(aiResponseArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                "AI Recommendations",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14)
        ));
        scrollPane.setPreferredSize(new Dimension(600, 400));

        mainPanel.add(scrollPane, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);

        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));

        consultButton = new JButton("Get AI Analysis");
        consultButton.setFont(new Font("Arial", Font.BOLD, 14));
        consultButton.setIcon(UIManager.getIcon("OptionPane.informationIcon"));
        consultButton.addActionListener(e -> consultAI());

        JButton clearButton = new JButton("Clear");
        clearButton.setFont(new Font("Arial", Font.PLAIN, 14));
        clearButton.addActionListener(e -> clearResponse());

        buttonPanel.add(consultButton);
        buttonPanel.add(clearButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Handle transaction data changes
     */
    @Override
    public void onTransactionDataChanged() {
        // Data has changed, update status to indicate new data is available
        statusLabel.setText("New transaction data available. Click 'Get AI Analysis' for updated analysis.");
    }

    /**
     * Clear the response area
     */
    private void clearResponse() {
        aiResponseArea.setText("Your financial analysis will appear here. Click 'Get AI Analysis' to analyze your budget and spending data.");
        statusLabel.setText("Ready to analyze your financial data");
        analysisProgressBar.setVisible(false);
    }

    /**
     * Consult the AI with budget and spending data
     * 修改为支持流式响应
     */
    private void consultAI() {
        // Prepare data to send to AI
        StringBuilder prompt = new StringBuilder();
        prompt.append("I want financial advice based on my budget and spending. Here is my data:\n\n");

        // Add total budget
        double totalBudgetAmount = 0;
        for (Budget budget : budgetService.getAllBudgets()) {
            if (budget.getCategory().equals("Total Budget")) {
                totalBudgetAmount = budget.getAmount();
                break;
            }
        }
        double totalSpent = getCurrentMonthTotalExpense();

        prompt.append("Total monthly budget: ¥").append(String.format("%.2f", totalBudgetAmount))
                .append("\nTotal spent this month: ¥").append(String.format("%.2f", totalSpent))
                .append("\nRemaining: ¥").append(String.format("%.2f", totalBudgetAmount - totalSpent))
                .append("\n\nCategory breakdown:\n");

        // Add category budgets and spending
        Map<String, Double> categoryExpenses = getCurrentMonthCategoryExpenses();
        DecimalFormat df = new DecimalFormat("#,##0.00");

        for (Budget budget : budgetService.getAllBudgets()) {
            if (!budget.getCategory().equals("Total Budget")) {
                String category = budget.getCategory();
                double budgetAmount = budget.getAmount();
                double spentAmount = categoryExpenses.getOrDefault(category, 0.0);
                double percentUsed = (budgetAmount > 0) ? (spentAmount / budgetAmount) * 100 : 0;

                prompt.append("- ").append(category)
                        .append(": Budget ¥").append(df.format(budgetAmount))
                        .append(", Spent ¥").append(df.format(spentAmount))
                        .append(" (").append(String.format("%.1f", percentUsed)).append("%)")
                        .append("\n");
            }
        }

        prompt.append("\nBased on this information, please provide me with financial advice, suggestions for budget adjustments, and spending optimization. Identify potential areas of concern and where I'm doing well.");

        // Update UI for loading state
        consultButton.setEnabled(false);
        statusLabel.setText("Analyzing financial data, please wait...");
        analysisProgressBar.setVisible(true);

        // 清空响应区域，准备接收流式响应
        aiResponseArea.setText("");

        // 创建流式响应回调
        StreamCallback callback = new StreamCallback() {
            @Override
            public void onToken(String token) {
                // 在EDT线程中更新UI
                SwingUtilities.invokeLater(() -> {
                    // 添加新的文本片段
                    aiResponseArea.append(token);
                    // 滚动到底部
                    aiResponseArea.setCaretPosition(aiResponseArea.getDocument().getLength());
                });
            }

            @Override
            public void onComplete() {
                // 在EDT线程中更新UI状态
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Analysis completed successfully");
                    analysisProgressBar.setVisible(false);
                    consultButton.setEnabled(true);
                });
            }

            @Override
            public void onError(String error) {
                // 在EDT线程中显示错误
                SwingUtilities.invokeLater(() -> {
                    aiResponseArea.append("\n\nError: " + error);
                    statusLabel.setText("Analysis failed");
                    analysisProgressBar.setVisible(false);
                    consultButton.setEnabled(true);
                });
            }
        };

        // 在后台线程中调用API，避免阻塞UI线程
        new Thread(() -> {
            try {
                // 尝试使用流式响应模式
                boolean success = aiConsultant.processStreamingQuery(prompt.toString(), callback);

                // 如果流式处理失败，回退到原始API调用方式
                if (!success) {
                    // 在EDT线程中更新UI
                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText("Retrying with standard API...");
                    });

                    // 尝试使用原来的API调用方式
                    String response = AIConsultant.getAdvice(prompt.toString());

                    // 在EDT线程中更新UI
                    SwingUtilities.invokeLater(() -> {
                        aiResponseArea.setText(response);
                        statusLabel.setText("Analysis completed successfully");
                        analysisProgressBar.setVisible(false);
                        consultButton.setEnabled(true);
                    });
                }
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    aiResponseArea.setText("Error getting AI advice: " + e.getMessage() + "\n\nPlease try again later.");
                    statusLabel.setText("Analysis failed");
                    analysisProgressBar.setVisible(false);
                    consultButton.setEnabled(true);
                });
                e.printStackTrace();
            }
        }).start();
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

        for (Transcation transaction : currentMonthTransactions) {
            if (!transaction.isIncome()) { // Only count expenses, exclude income
                String category = transaction.getCategory();
                double amount = transaction.getAmount();
                Double currentTotal = categoryExpenses.getOrDefault(category, 0.0);
                categoryExpenses.put(category, currentTotal + amount);
            }
        }

        return categoryExpenses;
    }
}