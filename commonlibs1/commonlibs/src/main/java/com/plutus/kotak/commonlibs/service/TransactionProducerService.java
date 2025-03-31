package com.plutus.kotak.commonlibs.service;

import com.plutus.kotak.commonlibs.avro.BankTransaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import java.util.function.Supplier;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionProducerService {

    private final Sinks.Many<BankTransaction> sink = Sinks.many().multicast().onBackpressureBuffer();

    @Bean
    public Supplier<Flux<BankTransaction>> sendTransaction() {
        return () -> {
            log.info("Setting up transaction supplier");
            return sink.asFlux()
                .doOnNext(t -> log.info("Transaction being sent to Kafka: {}", t))
                .doOnError(e -> log.error("Error in transaction supplier: {}", e.getMessage(), e));
        };
    }

    public void send(BankTransaction transaction) {
        log.info("Attempting to send transaction to Kafka: {}", transaction);
        try {
            if (transaction == null) {
                throw new IllegalArgumentException("Transaction cannot be null");
            }
            if (transaction.getTransactionId() == null) {
                throw new IllegalArgumentException("Transaction ID cannot be null");
            }
            
            sink.tryEmitNext(transaction)
                .orThrow();
            log.info("Transaction successfully emitted to sink");
        } catch (Exception e) {
            log.error("Failed to emit transaction to sink: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send transaction to Kafka", e);
        }
    }
} 