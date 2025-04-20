package com.project.gui;

import com.project.model.Transcation;
import com.project.service.TranscationService;
import com.project.service.TransactionObserver;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

/**
 * Chart panel showing income and expense distribution in pie charts
 */
public class ChartPanel extends JPanel implements TransactionObserver {
    private TranscationService transactionService;
    private PieChartPanel incomeChartPanel;
    private PieChartPanel expenseChartPanel;
    private JPanel summaryPanel;

    public ChartPanel(TranscationService transactionService) {
        this.transactionService = transactionService;
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // Register as observer
        transactionService.addObserver(this);

        // Create title
        JLabel titleLabel = new JLabel("Financial Charts", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));
        add(titleLabel, BorderLayout.NORTH);

        // Create charts panel
        JPanel chartsPanel = new JPanel(new GridLayout(1, 2, 20, 0));

        // Create income pie chart
        incomeChartPanel = new PieChartPanel("Income by Category", true);
        JPanel incomePanel = createChartWithLabel(incomeChartPanel, "Income Distribution");
        chartsPanel.add(incomePanel);

        // Create expense pie chart
        expenseChartPanel = new PieChartPanel("Expense by Category", false);
        JPanel expensePanel = createChartWithLabel(expenseChartPanel, "Expense Distribution");
        chartsPanel.add(expensePanel);

        add(chartsPanel, BorderLayout.CENTER);

        // Create summary panel
        summaryPanel = createSummaryPanel();
        add(summaryPanel, BorderLayout.SOUTH);

        // Update chart data
        updateChartData();
    }

    private JPanel createChartWithLabel(JPanel chartPanel, String title) {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel(title, SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 16));
        label.setBorder(new EmptyBorder(0, 0, 10, 0));

        panel.add(label, BorderLayout.NORTH);
        panel.add(chartPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(15, 0, 0, 0)
        ));

        JPanel summaryGrid = new JPanel(new GridLayout(2, 4, 15, 10));

        // Total Income
        JLabel totalIncomeLabel = new JLabel("Total Income:", SwingConstants.RIGHT);
        totalIncomeLabel.setFont(new Font("Arial", Font.BOLD, 14));
        summaryGrid.add(totalIncomeLabel);

        JLabel totalIncomeValue = new JLabel("$0.00", SwingConstants.LEFT);
        totalIncomeValue.setFont(new Font("Arial", Font.PLAIN, 14));
        totalIncomeValue.setForeground(new Color(46, 204, 113));
        summaryGrid.add(totalIncomeValue);

        // Total Expenses
        JLabel totalExpenseLabel = new JLabel("Total Expenses:", SwingConstants.RIGHT);
        totalExpenseLabel.setFont(new Font("Arial", Font.BOLD, 14));
        summaryGrid.add(totalExpenseLabel);

        JLabel totalExpenseValue = new JLabel("$0.00", SwingConstants.LEFT);
        totalExpenseValue.setFont(new Font("Arial", Font.PLAIN, 14));
        totalExpenseValue.setForeground(new Color(231, 76, 60));
        summaryGrid.add(totalExpenseValue);

        // Net Balance
        JLabel netBalanceLabel = new JLabel("Net Balance:", SwingConstants.RIGHT);
        netBalanceLabel.setFont(new Font("Arial", Font.BOLD, 14));
        summaryGrid.add(netBalanceLabel);

        JLabel netBalanceValue = new JLabel("$0.00", SwingConstants.LEFT);
        netBalanceValue.setFont(new Font("Arial", Font.PLAIN, 14));
        summaryGrid.add(netBalanceValue);

        // Transaction Count
        JLabel transactionCountLabel = new JLabel("Total Transactions:", SwingConstants.RIGHT);
        transactionCountLabel.setFont(new Font("Arial", Font.BOLD, 14));
        summaryGrid.add(transactionCountLabel);

        JLabel transactionCountValue = new JLabel("0", SwingConstants.LEFT);
        transactionCountValue.setFont(new Font("Arial", Font.PLAIN, 14));
        summaryGrid.add(transactionCountValue);

        panel.add(summaryGrid, BorderLayout.CENTER);

        // Store references to update labels later
        panel.putClientProperty("totalIncomeValue", totalIncomeValue);
        panel.putClientProperty("totalExpenseValue", totalExpenseValue);
        panel.putClientProperty("netBalanceValue", netBalanceValue);
        panel.putClientProperty("transactionCountValue", transactionCountValue);

        return panel;
    }

    public void updateChartData() {
        // Get transactions
        List<Transcation> transactions = transactionService.getAllTransactions();

        // Calculate income by category
        Map<String, Double> incomeByCategoryMap = new HashMap<>();
        // Calculate expense by category
        Map<String, Double> expenseByCategoryMap = new HashMap<>();

        for (Transcation transaction : transactions) {
            String category = transaction.getCategory();
            double amount = transaction.getAmount();

            if (transaction.isIncome()) {
                // Update income map
                incomeByCategoryMap.put(category,
                        incomeByCategoryMap.getOrDefault(category, 0.0) + amount);
            } else {
                // Update expense map
                expenseByCategoryMap.put(category,
                        expenseByCategoryMap.getOrDefault(category, 0.0) + amount);
            }
        }

        // Update income chart
        incomeChartPanel.updateData(incomeByCategoryMap);

        // Update expense chart
        expenseChartPanel.updateData(expenseByCategoryMap);

        // Update summary panel
        updateSummaryPanel(transactions.size());
    }

    private void updateSummaryPanel(int transactionCount) {
        double totalIncome = transactionService.getTotalIncome();
        double totalExpense = transactionService.getTotalExpense();
        double netBalance = transactionService.getNetBalance();

        JLabel totalIncomeValue = (JLabel) summaryPanel.getClientProperty("totalIncomeValue");
        totalIncomeValue.setText(String.format("$%.2f", totalIncome));

        JLabel totalExpenseValue = (JLabel) summaryPanel.getClientProperty("totalExpenseValue");
        totalExpenseValue.setText(String.format("$%.2f", totalExpense));

        JLabel netBalanceValue = (JLabel) summaryPanel.getClientProperty("netBalanceValue");
        netBalanceValue.setText(String.format("$%.2f", netBalance));
        netBalanceValue.setForeground(netBalance >= 0 ? new Color(46, 204, 113) : new Color(231, 76, 60));

        JLabel transactionCountValue = (JLabel) summaryPanel.getClientProperty("transactionCountValue");
        transactionCountValue.setText(String.valueOf(transactionCount));
    }

    @Override
    public void onTransactionDataChanged() {
        // Update chart data when notified of changes
        updateChartData();
    }

    /**
     * Custom Pie Chart Panel
     */
    private class PieChartPanel extends JPanel {
        private Map<String, Double> data = new HashMap<>();
        private String title;
        private final boolean isIncome;
        private final Color[] INCOME_COLORS = {
                new Color(46, 204, 113),    // Green
                new Color(39, 174, 96),     // Dark Green
                new Color(26, 188, 156),    // Turquoise
                new Color(22, 160, 133),    // Dark Turquoise
                new Color(52, 152, 219),    // Blue
                new Color(41, 128, 185)     // Dark Blue
        };

        private final Color[] EXPENSE_COLORS = {
                new Color(231, 76, 60),     // Red
                new Color(192, 57, 43),     // Dark Red
                new Color(243, 156, 18),    // Orange
                new Color(230, 126, 34),    // Dark Orange
                new Color(211, 84, 0),      // Darker Orange
                new Color(155, 89, 182),    // Purple
                new Color(142, 68, 173),    // Dark Purple
                new Color(127, 140, 141),   // Gray
                new Color(44, 62, 80)       // Dark Blue
        };

        public PieChartPanel(String title, boolean isIncome) {
            this.title = title;
            this.isIncome = isIncome;
            setPreferredSize(new Dimension(300, 300));
            setBackground(Color.WHITE);
        }

        public void updateData(Map<String, Double> data) {
            this.data = new HashMap<>(data);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();

            // Get total value
            double total = 0;
            for (double value : data.values()) {
                total += value;
            }

            // If no data, show message
            if (data.isEmpty() || total == 0) {
                g2.setFont(new Font("Arial", Font.BOLD, 14));
                String message = "No data available";
                FontMetrics metrics = g2.getFontMetrics();
                int messageWidth = metrics.stringWidth(message);
                g2.drawString(message, (width - messageWidth) / 2, height / 2);
                return;
            }

            // Calculate pie chart dimensions
            int pieSize = Math.min(width, height) - 100; // Leave space for legend
            int pieX = (width - pieSize) / 2;
            int pieY = (height - pieSize) / 2;

            // Draw pie chart
            double currentAngle = 0;
            int colorIndex = 0;

            // Sort entries by value (descending)
            List<Map.Entry<String, Double>> sortedEntries = new ArrayList<>(data.entrySet());
            sortedEntries.sort((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()));

            // Create list to store legend items
            List<LegendItem> legendItems = new ArrayList<>();

            for (Map.Entry<String, Double> entry : sortedEntries) {
                String category = entry.getKey();
                double value = entry.getValue();
                double percentage = value / total;
                double angle = 360 * percentage;

                // Get color based on type
                Color color = isIncome ?
                        INCOME_COLORS[colorIndex % INCOME_COLORS.length] :
                        EXPENSE_COLORS[colorIndex % EXPENSE_COLORS.length];

                // Draw pie slice
                g2.setColor(color);
                g2.fill(new Arc2D.Double(
                        pieX, pieY, pieSize, pieSize,
                        currentAngle, angle, Arc2D.PIE
                ));

                // Add to legend items
                legendItems.add(new LegendItem(category, value, percentage, color));

                currentAngle += angle;
                colorIndex++;
            }

            // Draw legend
            int legendX = 10;
            int legendY = 20;
            int legendItemHeight = 20;

            // Show all items instead of just top 5
            for (int i = 0; i < legendItems.size(); i++) {
                LegendItem item = legendItems.get(i);

                // Draw color box
                g2.setColor(item.color);
                g2.fill(new Rectangle2D.Double(legendX, legendY + i * legendItemHeight, 10, 10));

                // Draw text
                g2.setColor(Color.BLACK);
                g2.setFont(new Font("Arial", Font.PLAIN, 12));
                String text = String.format("%s: $%.2f (%.1f%%)",
                        item.category, item.value, item.percentage * 100);
                g2.drawString(text, legendX + 15, legendY + 10 + i * legendItemHeight);
            }
        }

        /**
         * Helper class for legend items
         */
        private class LegendItem {
            String category;
            double value;
            double percentage;
            Color color;

            public LegendItem(String category, double value, double percentage, Color color) {
                this.category = category;
                this.value = value;
                this.percentage = percentage;
                this.color = color;
            }
        }
    }
}