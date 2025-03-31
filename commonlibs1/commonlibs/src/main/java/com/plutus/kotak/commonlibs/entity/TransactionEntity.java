package com.plutus.kotak.commonlibs.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "transactions")
@Data
public class TransactionEntity {
    @Id
    private String transactionId;
    private String accountId;
    private double amount;
    private String transactionType;
    private long timestamp;
    private String status;
} 