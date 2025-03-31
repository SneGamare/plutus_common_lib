package com.plutus.kotak.commonlibs.serializer;

import com.plutus.kotak.commonlibs.avro.BankTransaction;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.kafka.common.serialization.Deserializer;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@Component
public class BankTransactionAvroDeserializer implements Deserializer<BankTransaction> {
    @Override
    public BankTransaction deserialize(String topic, byte[] data) {
        if (data == null) {
            return null;
        }
        try {
            DatumReader<BankTransaction> reader = new SpecificDatumReader<>(BankTransaction.class);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
            return reader.read(null, DecoderFactory.get().binaryDecoder(inputStream, null));
        } catch (IOException e) {
            throw new RuntimeException("Error deserializing BankTransaction", e);
        }
    }

    @Override
    public void close() {
        // No resources to close
    }
} 