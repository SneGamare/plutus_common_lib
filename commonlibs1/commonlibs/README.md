# Spring Cloud Stream Kafka with Avro Demo

This project demonstrates the usage of Spring Cloud Stream with Kafka, using Avro for serialization/deserialization, and H2 database for storage.

## Prerequisites

- Java 17 or later
- Maven
- Docker and Docker Compose

## Project Structure

- `src/main/avro/bank_transaction.avsc`: Avro schema definition
- `src/main/java/com/example/demo/`: Java source code
  - `config/`: Configuration classes
  - `controller/`: REST controllers
  - `entity/`: JPA entities
  - `repository/`: JPA repositories
  - `service/`: Business logic
- `src/main/resources/application.yml`: Application configuration
- `docker-compose.yml`: Docker Compose configuration for Kafka infrastructure

## Running the Project

1. Start the Kafka infrastructure:
   ```bash
   docker-compose up -d
   ```

2. Build the project:
   ```bash
   mvn clean package
   ```

3. Run the application:
   ```bash
   java -jar target/demo-0.0.1-SNAPSHOT.jar
   ```

4. Access the H2 console:
   - Open http://localhost:8080/h2-console
   - JDBC URL: jdbc:h2:mem:transactiondb
   - Username: sa
   - Password: (empty)

## Testing the Application

1. Send a transaction using curl:
   ```bash
   curl -X POST http://localhost:8080/api/transactions \
   -H "Content-Type: application/json" \
   -d '{
     "transactionId": "123",
     "accountId": "ACC001",
     "amount": 100.50,
     "transactionType": "DEPOSIT",
     "timestamp": 1648656000000,
     "status": "COMPLETED"
   }'
   ```

2. Check the H2 database to verify the transaction was stored:
   ```sql
   SELECT * FROM TRANSACTIONS;
   ```

## Architecture

- The application uses Spring Cloud Stream to integrate with Kafka
- Avro is used for serialization/deserialization of messages
- Messages are stored in H2 in-memory database
- The project includes both producer and consumer components
- Schema Registry is used to manage Avro schemas

## Notes

- The Kafka broker is configured to run on localhost:9092
- Schema Registry is configured to run on localhost:8081
- H2 console is available at http://localhost:8080/h2-console 