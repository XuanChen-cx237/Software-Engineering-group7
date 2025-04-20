package com.project.service;

/**
 * Interface for observing transaction data changes
 */
public interface TransactionObserver {
    /**
     * Called when transaction data has changed
     */
    void onTransactionDataChanged();
}