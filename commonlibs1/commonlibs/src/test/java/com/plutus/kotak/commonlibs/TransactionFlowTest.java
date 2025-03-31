package com.plutus.kotak.commonlibs;

import com.plutus.kotak.commonlibs.avro.BankTransaction;
import com.plutus.kotak.commonlibs.entity.TransactionEntity;
import com.plutus.kotak.commonlibs.model.TransactionDTO;
import com.plutus.kotak.commonlibs.repository.TransactionRepository;
import com.plutus.kotak.commonlibs.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.test.InputDestination;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(TestChannelBinderConfiguration.class)
public class TransactionFlowTest {

    @Autowired
    private InputDestination inputDestination;

    @Autowired
    private OutputDestination outputDestination;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TransactionService transactionService;

    @Test
    public void testTransactionFlow() {
        // Create a test transaction
        TransactionDTO transactionDTO = TransactionDTO.builder()
                .transactionId("TEST-123")
                .accountId("ACC-001")
                .amount(100.50)
                .transactionType("DEPOSIT")
                .timestamp(Instant.now().toEpochMilli())
                .status("COMPLETED")
                .build();

        // Create transaction through service
        TransactionDTO savedTransaction = transactionService.createTransaction(transactionDTO);
        assertNotNull(savedTransaction);
        assertEquals("TEST-123", savedTransaction.getTransactionId());
        assertEquals("ACC-001", savedTransaction.getAccountId());
        assertEquals(100.50, savedTransaction.getAmount());
        assertEquals("DEPOSIT", savedTransaction.getTransactionType());
        assertEquals("COMPLETED", savedTransaction.getStatus());

        // Verify the transaction was saved in the database
        Optional<TransactionEntity> entity = transactionRepository.findById("TEST-123");
        assertTrue(entity.isPresent(), "Transaction should be saved in database");
        
        TransactionEntity savedEntity = entity.get();
        assertEquals("TEST-123", savedEntity.getTransactionId());
        assertEquals("ACC-001", savedEntity.getAccountId());
        assertEquals(100.50, savedEntity.getAmount());
        assertEquals("DEPOSIT", savedEntity.getTransactionType());
        assertEquals("COMPLETED", savedEntity.getStatus());

        // Verify the output message
        Message<byte[]> outputMessage = outputDestination.receive(1000, "transactions");
        assertNotNull(outputMessage, "Output message should be received");
    }

    @Test
    public void testInvalidTransaction() {
        // Create an invalid transaction (with negative amount)
        TransactionDTO invalidTransaction = TransactionDTO.builder()
                .transactionId("TEST-456")
                .accountId("ACC-002")
                .amount(-100.50) // Invalid amount
                .transactionType("WITHDRAWAL")
                .timestamp(Instant.now().toEpochMilli())
                .status("FAILED")
                .build();

        // Create transaction through service
        TransactionDTO savedTransaction = transactionService.createTransaction(invalidTransaction);
        assertNotNull(savedTransaction);
        assertEquals("TEST-456", savedTransaction.getTransactionId());
        assertEquals("ACC-002", savedTransaction.getAccountId());
        assertEquals(-100.50, savedTransaction.getAmount());
        assertEquals("WITHDRAWAL", savedTransaction.getTransactionType());
        assertEquals("FAILED", savedTransaction.getStatus());

        // Verify the transaction was saved in the database
        Optional<TransactionEntity> entity = transactionRepository.findById("TEST-456");
        assertTrue(entity.isPresent(), "Transaction should be saved in database");
        
        TransactionEntity savedEntity = entity.get();
        assertEquals("TEST-456", savedEntity.getTransactionId());
        assertEquals("ACC-002", savedEntity.getAccountId());
        assertEquals(-100.50, savedEntity.getAmount());
        assertEquals("WITHDRAWAL", savedEntity.getTransactionType());
        assertEquals("FAILED", savedEntity.getStatus());

        // Verify the output message
        Message<byte[]> outputMessage = outputDestination.receive(1000, "transactions");
        assertNotNull(outputMessage, "Output message should be received");
    }

    @Test
    public void testDuplicateTransaction() {
        // Create a transaction
        TransactionDTO transactionDTO = TransactionDTO.builder()
                .transactionId("TEST-789")
                .accountId("ACC-003")
                .amount(200.00)
                .transactionType("DEPOSIT")
                .timestamp(Instant.now().toEpochMilli())
                .status("COMPLETED")
                .build();

        // Create transaction through service
        TransactionDTO savedTransaction = transactionService.createTransaction(transactionDTO);
        assertNotNull(savedTransaction);
        assertEquals("TEST-789", savedTransaction.getTransactionId());

        // Try to create the same transaction again
        assertThrows(IllegalArgumentException.class, () -> {
            transactionService.createTransaction(transactionDTO);
        });

        // Verify only one transaction was saved
        Optional<TransactionEntity> entity = transactionRepository.findById("TEST-789");
        assertTrue(entity.isPresent(), "Transaction should be saved in database");
        
        TransactionEntity savedEntity = entity.get();
        assertEquals("TEST-789", savedEntity.getTransactionId());
        assertEquals("ACC-003", savedEntity.getAccountId());
        assertEquals(200.00, savedEntity.getAmount());
        assertEquals("DEPOSIT", savedEntity.getTransactionType());
        assertEquals("COMPLETED", savedEntity.getStatus());
    }
} 