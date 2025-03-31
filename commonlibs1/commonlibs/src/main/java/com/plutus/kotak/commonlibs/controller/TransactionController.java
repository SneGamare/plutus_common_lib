package com.plutus.kotak.commonlibs.controller;

import com.plutus.kotak.commonlibs.avro.BankTransaction;
import com.plutus.kotak.commonlibs.entity.TransactionEntity;
import com.plutus.kotak.commonlibs.repository.TransactionRepository;
import com.plutus.kotak.commonlibs.service.TransactionProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionProducerService producerService;
    private final TransactionRepository transactionRepository;

    @PostMapping
    @Transactional
    public ResponseEntity<String> createTransaction(@RequestBody BankTransaction transaction) {
        log.info("Received transaction request: {}", transaction);
        try {
            // Check if transaction already exists
            if (transactionRepository.existsById(transaction.getTransactionId().toString())) {
                log.warn("Transaction with ID {} already exists", transaction.getTransactionId());
                return ResponseEntity.badRequest().body("Transaction already exists");
            }

            // Create and save transaction entity
            TransactionEntity entity = new TransactionEntity();
            entity.setTransactionId(transaction.getTransactionId().toString());
            entity.setAccountId(transaction.getAccountId().toString());
            entity.setAmount(transaction.getAmount());
            entity.setTransactionType(transaction.getTransactionType().toString());
            entity.setTimestamp(transaction.getTimestamp());
            entity.setStatus(transaction.getStatus().toString());
            
            log.info("Saving transaction to database: {}", entity);
            TransactionEntity savedEntity = transactionRepository.save(entity);
            log.info("Successfully saved transaction to database: {}", savedEntity);

            // Verify the transaction was saved
            TransactionEntity verifiedEntity = transactionRepository.findById(savedEntity.getTransactionId())
                .orElseThrow(() -> new RuntimeException("Transaction not found after saving"));
            log.info("Verified transaction in database: {}", verifiedEntity);

            return ResponseEntity.ok("Transaction saved successfully with ID: " + savedEntity.getTransactionId());
        } catch (Exception e) {
            log.error("Error saving transaction: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error saving transaction: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<TransactionEntity>> getAllTransactions() {
        log.info("Fetching all transactions from database");
        try {
            List<TransactionEntity> transactions = transactionRepository.findAll();
            log.info("Found {} transactions in database", transactions.size());
            if (transactions.isEmpty()) {
                log.warn("No transactions found in database");
            } else {
                transactions.forEach(t -> log.info("Retrieved transaction: {}", t));
            }
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            log.error("Error fetching transactions: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
} 