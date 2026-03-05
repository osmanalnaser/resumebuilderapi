package in.osmanalnaser.resumebuilderapi.repository;

import in.osmanalnaser.resumebuilderapi.document.Payment;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends MongoRepository<Payment, String> {

    Optional<Payment> findByPaypalOrderId(String paypalOrderId);

    Optional<Payment> findByPaypalPaymentId(String paypalPaymentId);

    List<Payment> findByUserIdOrderByCreatedAtDesc(String userId);

    List<Payment> findByStatus(String status);
}
