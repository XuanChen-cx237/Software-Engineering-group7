package com.project.service;

import com.project.model.Transcation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TranscationServiceTest {

    private TranscationService transactionService;

    @Mock
    private TransactionObserver observer;

    @BeforeEach
    void setUp() {
        transactionService = new TranscationService();
        observer = mock(TransactionObserver.class);
    }

    @Test
    void shouldAddTransactionAndAssignId() {
        Transcation transaction = new Transcation(100.0, true, "Salary", "Monthly salary");

        transactionService.addTransaction(transaction);

        assertEquals(1, transaction.getId());
        assertEquals(1, transactionService.getAllTransactions().size());
    }

    @Test
    void shouldCalculateTotalIncome() {
        transactionService.addTransaction(new Transcation(1000.0, true, "Salary", ""));
        transactionService.addTransaction(new Transcation(500.0, true, "Bonus", ""));
        transactionService.addTransaction(new Transcation(200.0, false, "Food", ""));

        assertEquals(1500.0, transactionService.getTotalIncome());
    }

    @Test
    void shouldCalculateTotalExpense() {
        transactionService.addTransaction(new Transcation(1000.0, true, "Salary", ""));
        transactionService.addTransaction(new Transcation(200.0, false, "Food", ""));
        transactionService.addTransaction(new Transcation(100.0, false, "Gas", ""));

        assertEquals(300.0, transactionService.getTotalExpense());
    }

    @Test
    void shouldCalculateNetBalance() {
        transactionService.addTransaction(new Transcation(1000.0, true, "Salary", ""));
        transactionService.addTransaction(new Transcation(300.0, false, "Food", ""));

        assertEquals(700.0, transactionService.getNetBalance());
    }

    @Test
    void shouldNotifyObserversWhenTransactionAdded() {
        transactionService.addObserver(observer);

        transactionService.addTransaction(new Transcation(100.0, true, "Test", ""));

        verify(observer, times(1)).onTransactionDataChanged();
    }
}