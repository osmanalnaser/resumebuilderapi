package in.osmanalnaser.resumebuilderapi.repository;

import in.osmanalnaser.resumebuilderapi.document.Payment;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PaymentRepository extends MongoRepository<Payment, String> {
}
