package com.project.gui;

import com.project.model.Transcation;
import com.project.service.TranscationService;
import com.project.service.TransactionObserver;
import com.project.util.CSVImporter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Line2D;
import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * Transaction panel
 * Displays transaction table and expense chart
 */
public class TranscationPanel extends JPanel implements TransactionObserver {
    private TranscationService transactionService;
    private JTable transactionTable;
    private DefaultTableModel tableModel;
    private JButton addButton;
    private JButton importCsvButton;
    private SimpleExpenseChartPanel chartPanel;

    private final String[] columnNames = {"Date", "Amount", "Type", "Category", "Description"};

    /**
     * Constructor
     */
    public TranscationPanel(TranscationService transactionService) {
        this.transactionService = transactionService;
        setLayout(new BorderLayout(0, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // Register as observer
        transactionService.addObserver(this);

        // Create top panel with buttons
        createTopPanel();

        // Create center panel with table
        JPanel centerPanel = new JPanel(new BorderLayout());

        // Create transaction table
        createTransactionTable();

        // Add table to center panel
        centerPanel.add(new JScrollPane(transactionTable), BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // Create bottom chart panel
        chartPanel = new SimpleExpenseChartPanel();
        add(chartPanel, BorderLayout.SOUTH);

        // Load initial data
        loadTransactionData();

        // No need to explicitly update chart data as it's done in observer method
    }

    @Override
    public void onTransactionDataChanged() {
        // Update UI when transaction data changes
        loadTransactionData();
        updateChartData();
    }

    /**
     * Create top panel with buttons
     */
    private void createTopPanel() {
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        // Create buttons
        addButton = new JButton("Add Transaction");
        importCsvButton = new JButton("Import CSV");

        // Add event listeners
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showAddTransactionDialog();
            }
        });

        importCsvButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                importFromCSV();
            }
        });

        // Add buttons to panel
        topPanel.add(addButton);
        topPanel.add(importCsvButton);

        add(topPanel, BorderLayout.NORTH);
    }

    /**
     * Create transaction table
     */
    private void createTransactionTable() {
        // Create table model
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table cells non-editable
            }
        };

        // Create JTable with model
        transactionTable = new JTable(tableModel);
        transactionTable.setRowHeight(25);
        transactionTable.getTableHeader().setReorderingAllowed(false);
        transactionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    /**
     * Load transaction data to table
     */
    private void loadTransactionData() {
        // Clear existing data
        tableModel.setRowCount(0);

        // Get transactions from service
        List<Transcation> transactions = transactionService.getAllTransactions();

        // Add transactions to table
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        for (Transcation transaction : transactions) {
            Object[] rowData = {
                    dateFormat.format(transaction.getDate()),
                    transaction.getAmount(),
                    transaction.isIncome() ? "Income" : "Expense",
                    transaction.getCategory(),
                    transaction.getDescription()
            };
            tableModel.addRow(rowData);
        }
    }

    /**
     * Update chart data
     */
    private void updateChartData() {
        // Get expense data by month and update chart
        Map<String, Double> monthlyExpenses = transactionService.getMonthlyExpenses();
        chartPanel.updateData(monthlyExpenses);
    }

    /**
     * Show add transaction dialog
     */
    private void showAddTransactionDialog() {
        // Create dialog
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add Transaction", true);
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        // Create form panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridLayout(6, 2, 10, 10));
        formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Form components
        JLabel dateLabel = new JLabel("Date:");

        // Create separate date component panel with grid layout
        JPanel datePanel = new JPanel(new GridLayout(1, 3, 5, 0));

        // Create year dropdown panel
        JPanel yearPanel = new JPanel(new BorderLayout());
        yearPanel.add(new JLabel("Year:"), BorderLayout.NORTH);

        // Create year dropdown (current year and previous 2 years)
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        Integer[] years = {currentYear - 2, currentYear - 1, currentYear};
        JComboBox<Integer> yearComboBox = new JComboBox<>(years);
        yearComboBox.setSelectedItem(currentYear);
        yearPanel.add(yearComboBox, BorderLayout.CENTER);

        // Create month dropdown panel
        JPanel monthPanel = new JPanel(new BorderLayout());
        monthPanel.add(new JLabel("Month:"), BorderLayout.NORTH);

        // Create month dropdown
        String[] months = {"01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"};
        JComboBox<String> monthComboBox = new JComboBox<>(months);
        monthComboBox.setSelectedIndex(Calendar.getInstance().get(Calendar.MONTH));
        monthPanel.add(monthComboBox, BorderLayout.CENTER);

        // Create day dropdown panel
        JPanel dayPanel = new JPanel(new BorderLayout());
        dayPanel.add(new JLabel("Day:"), BorderLayout.NORTH);

        // Create day dropdown (1-31)
        String[] days = new String[31];
        for (int i = 0; i < 31; i++) {
            days[i] = String.format("%02d", i + 1); // Use two digits with leading zero
        }
        JComboBox<String> dayComboBox = new JComboBox<>(days);
        dayComboBox.setSelectedItem(String.format("%02d", Calendar.getInstance().get(Calendar.DAY_OF_MONTH)));
        dayPanel.add(dayComboBox, BorderLayout.CENTER);

        // Add three dropdown panels to date panel
        datePanel.add(yearPanel);
        datePanel.add(monthPanel);
        datePanel.add(dayPanel);

        JLabel amountLabel = new JLabel("Amount:");
        JTextField amountField = new JTextField();

        JLabel typeLabel = new JLabel("Type:");
        String[] types = {"Expense", "Income"};
        JComboBox<String> typeComboBox = new JComboBox<>(types);

        JLabel categoryLabel = new JLabel("Category:");
        JComboBox<String> categoryComboBox = new JComboBox<>();

        // Define category lists
        String[] expenseCategories = {"Food", "Transport", "Housing", "Entertainment", "Utilities", "Education", "Healthcare", "Shopping", "Other"};
        String[] incomeCategories = {"Salary", "Bonus", "Investment", "Gift", "Refund", "Other"};

        // Set initial categories (default to expense)
        for (String category : expenseCategories) {
            categoryComboBox.addItem(category);
        }

        // Add listener to update categories when type changes
        typeComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean isIncome = typeComboBox.getSelectedItem().equals("Income");

                // Remember selected category if possible
                String selectedCategory = categoryComboBox.getSelectedItem() != null ?
                        categoryComboBox.getSelectedItem().toString() : "";

                // Update categories
                categoryComboBox.removeAllItems();

                if (isIncome) {
                    for (String category : incomeCategories) {
                        categoryComboBox.addItem(category);
                    }
                } else {
                    for (String category : expenseCategories) {
                        categoryComboBox.addItem(category);
                    }
                }

                // Try to maintain selection if it exists in new list
                for (int i = 0; i < categoryComboBox.getItemCount(); i++) {
                    if (categoryComboBox.getItemAt(i).equals(selectedCategory)) {
                        categoryComboBox.setSelectedIndex(i);
                        break;
                    }
                }
            }
        });

        JLabel descLabel = new JLabel("Description:");
        JTextField descField = new JTextField();

        // Add components to form
        formPanel.add(dateLabel);
        formPanel.add(datePanel);
        formPanel.add(amountLabel);
        formPanel.add(amountField);
        formPanel.add(typeLabel);
        formPanel.add(typeComboBox);
        formPanel.add(categoryLabel);
        formPanel.add(categoryComboBox);
        formPanel.add(descLabel);
        formPanel.add(descField);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        // Add event listeners
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    // Validate and save transaction
                    double amount = Double.parseDouble(amountField.getText());
                    boolean isIncome = typeComboBox.getSelectedItem().equals("Income");
                    String category = categoryComboBox.getSelectedItem().toString();
                    String description = descField.getText();

                    // Create date from selected values
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.YEAR, (Integer) yearComboBox.getSelectedItem());
                    calendar.set(Calendar.MONTH, monthComboBox.getSelectedIndex());

                    // Parse selected day (from string to int)
                    int day = Integer.parseInt((String) dayComboBox.getSelectedItem());
                    calendar.set(Calendar.DAY_OF_MONTH, day);

                    // Create and save transaction
                    Transcation transaction = new Transcation();
                    transaction.setAmount(amount);
                    transaction.setIncome(isIncome);
                    transaction.setCategory(category);
                    transaction.setDescription(description);
                    transaction.setDate(calendar.getTime());

                    transactionService.addTransaction(transaction);

                    // Transaction service will notify observers of the change
                    // which will trigger UI updates

                    // Close dialog
                    dialog.dispose();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog, "Please enter a valid amount.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });

        // Add panels to dialog
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    /**
     * Import transactions from CSV
     */
    private void importFromCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select CSV File");
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV files (*.csv)", "csv"));

        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                CSVImporter importer = new CSVImporter();
                List<Transcation> importedTransactions = importer.importTransactions(selectedFile.getAbsolutePath());

                // Save imported transactions
                for (Transcation transaction : importedTransactions) {
                    transactionService.addTransaction(transaction);
                }

                // Transaction service will notify observers of the change
                // which will trigger UI updates

                JOptionPane.showMessageDialog(this,
                        "Successfully imported " + importedTransactions.size() + " transactions.",
                        "Import Successful",
                        JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Error importing CSV: " + e.getMessage(),
                        "Import Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Simple expense chart panel inner class
     */
    private class SimpleExpenseChartPanel extends JPanel {
        private Map<String, Double> expenseData = new TreeMap<>(); // TreeMap guarantees sorted by key
        private double maxValue = 1000.0;
        private final Color expenseLineColor = new Color(231, 76, 60);
        private final Color gridColor = new Color(230, 230, 230);
        private final int padding = 30;

        public SimpleExpenseChartPanel() {
            setPreferredSize(new Dimension(600, 200));
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY),
                    BorderFactory.createEmptyBorder(10, 0, 0, 0)
            ));
        }

        public void updateData(Map<String, Double> expenses) {
            // Use TreeMap to ensure sorted by year-month
            this.expenseData = new TreeMap<>(expenses);

            // Calculate max value for vertical scaling
            maxValue = 100.0; // Minimum default value
            for (Double value : expenses.values()) {
                maxValue = Math.max(maxValue, value);
            }

            // Ensure extra space at top
            maxValue *= 1.2;

            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();

            // Draw title
            g2.setFont(new Font("Arial", Font.BOLD, 12));
            String title = "Monthly Expense Trend";
            FontMetrics metrics = g2.getFontMetrics();
            int titleWidth = metrics.stringWidth(title);
            g2.drawString(title, (width - titleWidth) / 2, 15);

            // If no data, show message
            if (expenseData.isEmpty()) {
                String message = "No data available";
                g2.drawString(message, (width - metrics.stringWidth(message)) / 2, height / 2);
                return;
            }

            // Calculate drawing area
            int chartWidth = width - 2 * padding;
            int chartHeight = height - 2 * padding - 20; // Subtract title space
            int startY = 20 + padding; // Start below title

            // Draw coordinate axes
            g2.setColor(Color.BLACK);
            g2.drawLine(padding, height - padding, width - padding, height - padding); // X-axis
            g2.drawLine(padding, height - padding, padding, startY); // Y-axis

            // Draw horizontal grid lines and Y-axis labels
            g2.setFont(new Font("Arial", Font.PLAIN, 9));
            DecimalFormat df = new DecimalFormat("#,##0");

            for (int i = 0; i <= 4; i++) {
                int y = height - padding - (i * chartHeight / 4);

                // Grid lines
                g2.setColor(gridColor);
                g2.drawLine(padding + 1, y, width - padding, y);

                // Y-axis labels
                g2.setColor(Color.BLACK);
                String yLabel = df.format((maxValue * i) / 4);
                g2.drawString(yLabel, padding - metrics.stringWidth(yLabel) - 5, y + 3);
            }

            // Get all months
            List<String> months = new ArrayList<>(expenseData.keySet());

            // If enough data points to draw the line
            if (months.size() > 1) {
                // Calculate X-axis scale
                double xScale = (double) chartWidth / (months.size() - 1);

                // Draw X-axis labels
                for (int i = 0; i < months.size(); i++) {
                    int x = padding + (int) (i * xScale);

                    // X-axis label
                    String month = months.get(i);

                    // Rotate labels to avoid overlap
                    g2.setColor(Color.BLACK);
                    g2.translate(x, height - padding + 5);
                    g2.rotate(Math.PI / 6);
                    g2.drawString(month, 0, 0);
                    g2.rotate(-Math.PI / 6);
                    g2.translate(-(x), -(height - padding + 5));
                }

                // Draw expense line
                g2.setColor(expenseLineColor);
                g2.setStroke(new BasicStroke(2f));

                int[] xPoints = new int[months.size()];
                int[] yPoints = new int[months.size()];

                for (int i = 0; i < months.size(); i++) {
                    String month = months.get(i);
                    double value = expenseData.get(month);

                    xPoints[i] = padding + (int) (i * xScale);
                    yPoints[i] = (int) (height - padding - ((value / maxValue) * chartHeight));
                }

                // Draw line segments
                for (int i = 0; i < xPoints.length - 1; i++) {
                    g2.draw(new Line2D.Double(xPoints[i], yPoints[i], xPoints[i + 1], yPoints[i + 1]));
                }

                // Draw data points and show amount labels
                for (int i = 0; i < xPoints.length; i++) {
                    // Draw data point
                    g2.fillOval(xPoints[i] - 3, yPoints[i] - 3, 6, 6);

                    // Add amount label above the point
                    String amountLabel = String.format("¥%.2f", expenseData.get(months.get(i)));
                    int labelWidth = metrics.stringWidth(amountLabel);

                    // Position the label above the point
                    g2.setFont(new Font("Arial", Font.BOLD, 10));
                    g2.setColor(expenseLineColor);
                    g2.drawString(amountLabel, xPoints[i] - labelWidth / 2, yPoints[i] - 10);
                }
            }
        }
    }
}