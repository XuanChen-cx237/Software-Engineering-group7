package com.project.gui;

import com.project.model.Transcation;
import com.project.gui.ChartPanel;
import com.project.service.BudgetService;
import com.project.service.TranscationService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Application main window
 */
public class MainFrame extends JFrame {
    private JPanel contentPane;
    private JPanel leftPanel;
    private JPanel rightPanel;
    private CardLayout cardLayout;

    private TranscationPanel transactionPanel;
    private JPanel chartPanel;
    private JPanel budgetPanel;
    private JPanel aiAnalysisPanel;

    private TranscationService transactionService;
    private BudgetService budgetService;

    public MainFrame() {
        transactionService = new TranscationService();
        budgetService = new BudgetService(transactionService);
        initializeUI();
    }

    /**
     * Initialize the user interface
     */
    private void initializeUI() {
        setTitle("Financial Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 700);
        setLocationRelativeTo(null);

        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        contentPane.setLayout(new BorderLayout(10, 0));
        setContentPane(contentPane);

        // Create left panel with module buttons
        createLeftPanel();

        // Create right panel with card layout for different views
        createRightPanel();

        // Default to show transaction panel
        showPanel("transaction");
    }

    /**
     * Create left panel
     */
    private void createLeftPanel() {
        leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setPreferredSize(new Dimension(200, getHeight()));
        leftPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY));

        // Add title
        JLabel titleLabel = new JLabel("Modules");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));

        // Create module buttons
        JButton transactionBtn = createModuleButton("Transactions", "transaction");
        JButton chartBtn = createModuleButton("Charts", "chart");
        JButton budgetBtn = createModuleButton("Budget", "budget");
        JButton aiAnalysisBtn = createModuleButton("AI Analysis", "aianalysis");

        // Add components to left panel
        leftPanel.add(titleLabel);
        leftPanel.add(transactionBtn);
        leftPanel.add(Box.createVerticalStrut(10));
        leftPanel.add(chartBtn);
        leftPanel.add(Box.createVerticalStrut(10));
        leftPanel.add(budgetBtn);
        leftPanel.add(Box.createVerticalStrut(10));
        leftPanel.add(aiAnalysisBtn);
        leftPanel.add(Box.createVerticalGlue());

        contentPane.add(leftPanel, BorderLayout.WEST);
    }

    /**
     * Create module button
     */
    private JButton createModuleButton(String text, String command) {
        JButton button = new JButton(text);
        button.setMaximumSize(new Dimension(180, 40));
        button.setPreferredSize(new Dimension(180, 40));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setFont(new Font("Arial", Font.PLAIN, 14));
        button.setFocusPainted(false);

        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showPanel(command);
            }
        });

        return button;
    }

    /**
     * Create right panel
     */
    private void createRightPanel() {
        rightPanel = new JPanel();
        cardLayout = new CardLayout();
        rightPanel.setLayout(cardLayout);

        // Initialize each module panel
        transactionPanel = new TranscationPanel(transactionService);
        chartPanel = new ChartPanel(transactionService);
        budgetPanel = new BudgetPanel(budgetService, transactionService);

        // Use AIPanel as the AI Analysis panel
        aiAnalysisPanel = new AIPanel(budgetService, transactionService);

        // Add panels to card layout
        rightPanel.add(transactionPanel, "transaction");
        rightPanel.add(chartPanel, "chart");
        rightPanel.add(budgetPanel, "budget");
        rightPanel.add(aiAnalysisPanel, "aianalysis");

        contentPane.add(rightPanel, BorderLayout.CENTER);
    }

    /**
     * Show specified panel
     */
    private void showPanel(String panelName) {
        cardLayout.show(rightPanel, panelName);
    }
}