package com.project.util;

import com.project.model.Transcation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * CSV Import Utility
 */
public class CSVImporter {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * Import transactions from a CSV file
     * Expected CSV format: Date,Amount,Type,Category,Description
     *
     * @param filePath Path to the CSV file
     * @return List of imported transactions
     * @throws IOException If there's an error reading the file
     * @throws ParseException If there's an error parsing the CSV data
     */
    public List<Transcation> importTransactions(String filePath) throws IOException, ParseException {
        List<Transcation> transactions = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isHeader = true;

            while ((line = reader.readLine()) != null) {
                // Skip header row
                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                // Parse CSV line
                String[] values = parseCSVLine(line);

                if (values.length >= 5) {
                    Transcation transaction = new Transcation();

                    // Parse date (format: yyyy-MM-dd HH:mm:ss)
                    try {
                        transaction.setDate(DATE_FORMAT.parse(values[0]));
                    } catch (ParseException e) {
                        // If date format is incorrect, use current date
                        transaction.setDate(new Date());
                    }

                    // Parse amount
                    try {
                        transaction.setAmount(Double.parseDouble(values[1]));
                    } catch (NumberFormatException e) {
                        throw new ParseException("Invalid amount value: " + values[1], 0);
                    }

                    // Parse type (income/expense)
                    String type = values[2].trim().toLowerCase();
                    transaction.setIncome(type.equals("income"));

                    // Parse category
                    transaction.setCategory(values[3].trim());

                    // Parse description
                    transaction.setDescription(values[4].trim());

                    transactions.add(transaction);
                }
            }
        }

        return transactions;
    }

    /**
     * 解析CSV行，处理可能包含逗号的引号值
     *
     * @param line 要解析的CSV行
     * @return CSV行中的值数组
     */
    private String[] parseCSVLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder currentValue = new StringBuilder();
        boolean inQuotes = false;

        for (char c : line.toCharArray()) {
            if (c == '\"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(currentValue.toString());
                currentValue = new StringBuilder();
            } else {
                currentValue.append(c);
            }
        }

        result.add(currentValue.toString());
        return result.toArray(new String[0]);
    }
}