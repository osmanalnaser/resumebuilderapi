package in.osmanalnaser.resumebuilderapi.service;

import in.osmanalnaser.resumebuilderapi.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

import static in.osmanalnaser.resumebuilderapi.util.AppConstants.PREMIUM;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;

    @PostMapping("/creat-order")
    public ResponseEntity<?> creatOrder(@RequestBody Map<String, String> request,
                                        Authentication authentication){
        //validate the request
        String planType = request.get("planType");
        if (!PREMIUM.equalsIgnoreCase(planType)){
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid plan type"));
        }

        //Step 1: Call the service method

        //Step 2: Prepare the response object

        //Step 3: return the response
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(@RequestBody Map<String, String> request) {
        return null;
    }

    @GetMapping("/history")
    public ResponseEntity<?> getPaymentHistory(Authentication authentication){
        return null;
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<?> getOrderDetails(@PathVariable String orderId) {
        return null;
    }
}
