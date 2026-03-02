package in.osmanalnaser.resumebuilderapi.repository;

import in.osmanalnaser.resumebuilderapi.document.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByEmail(String email);

    Boolean existsByEmail(String email);

    Optional <User> findByVerificationToken(String verificationToken);


}