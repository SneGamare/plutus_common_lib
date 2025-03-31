package com.plutus.kotak.commonlibs.service;

import com.plutus.kotak.commonlibs.avro.BankTransaction;
import com.plutus.kotak.commonlibs.entity.TransactionEntity;
import com.plutus.kotak.commonlibs.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Consumer;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionConsumerService {

    private final TransactionRepository transactionRepository;

    @Bean
    public Consumer<BankTransaction> processTransaction() {
        return transaction -> {
            log.info("Received transaction from Kafka: {}", transaction);
            if (transaction == null) {
                log.error("Received null transaction from Kafka");
                return;
            }
            try {
                saveTransaction(transaction);
                log.info("Transaction processed successfully");
                
                // Verify immediately after saving
                verifyTransaction(transaction.getTransactionId().toString());
            } catch (Exception e) {
                log.error("Failed to process transaction: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to process transaction", e);
            }
        };
    }

    @Transactional
    protected void saveTransaction(BankTransaction transaction) {
        log.info("Starting to save transaction to database");
        if (transaction == null) {
            throw new IllegalArgumentException("Transaction cannot be null");
        }
        try {
            TransactionEntity entity = new TransactionEntity();
            entity.setTransactionId(transaction.getTransactionId().toString());
            entity.setAccountId(transaction.getAccountId().toString());
            entity.setAmount(transaction.getAmount());
            entity.setTransactionType(transaction.getTransactionType().toString());
            entity.setTimestamp(transaction.getTimestamp());
            entity.setStatus(transaction.getStatus().toString());
            
            log.info("Created transaction entity: {}", entity);
            TransactionEntity savedEntity = transactionRepository.save(entity);
            log.info("Successfully saved transaction to database: {}", savedEntity);
            
            // Verify the transaction was saved
            verifyTransaction(savedEntity.getTransactionId());
        } catch (Exception e) {
            log.error("Error saving transaction to database: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save transaction to database", e);
        }
    }

    private void verifyTransaction(String transactionId) {
        log.info("Verifying transaction with ID: {}", transactionId);
        if (transactionId == null || transactionId.trim().isEmpty()) {
            throw new IllegalArgumentException("Transaction ID cannot be null or empty");
        }
        TransactionEntity retrievedEntity = transactionRepository.findById(transactionId)
            .orElseThrow(() -> {
                log.error("Transaction not found in database with ID: {}", transactionId);
                return new RuntimeException("Transaction not found after saving");
            });
        log.info("Successfully verified transaction in database: {}", retrievedEntity);
    }
} 