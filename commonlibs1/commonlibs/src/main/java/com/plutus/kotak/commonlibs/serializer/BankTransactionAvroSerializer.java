package com.plutus.kotak.commonlibs.serializer;

import com.plutus.kotak.commonlibs.avro.BankTransaction;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.kafka.common.serialization.Serializer;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Component
public class BankTransactionAvroSerializer implements Serializer<BankTransaction> {
    @Override
    public byte[] serialize(String topic, BankTransaction data) {
        if (data == null) {
            return null;
        }
        try {
            DatumWriter<BankTransaction> writer = new SpecificDatumWriter<>(BankTransaction.class);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            writer.write(data, EncoderFactory.get().binaryEncoder(outputStream, null));
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error serializing BankTransaction", e);
        }
    }

    @Override
    public void close() {
        // No resources to close
    }
} 