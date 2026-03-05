package in.osmanalnaser.resumebuilderapi.controller;

import in.osmanalnaser.resumebuilderapi.document.Payment;
import in.osmanalnaser.resumebuilderapi.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static in.osmanalnaser.resumebuilderapi.util.AppConstants.PREMIUM;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payment")
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create-order")
    public ResponseEntity<?> creatOrder(@RequestBody Map<String, String> request,
                                        Authentication authentication) throws Exception {
        //validate the request
        String planType = request.get("planType");
        if (!PREMIUM.equalsIgnoreCase(planType)){
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid plan type"));
        }

        //Step 1: Call the service method
        Payment payment = paymentService.createOrder(authentication.getPrincipal(), planType);

        //Step 2: Prepare the response object
        Map<String, Object> response = Map.of(
                "orderId", payment.getPaypalOrderId(),
                "amount", payment.getAmount(),
                "currency", payment.getCurrency(),
                "receipt", payment.getReceipt()
        );

        //Step 3: return the response
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(@RequestBody Map<String, String> request) {
        //Step 1: Validate the request
        String paypalOrderId = request.get("paypal_order_id");
        String paypalPaymentId = request.get("paypal_payment_id");
        String paypalSignature = request.get("paypal_signature");

        if (Objects.isNull(paypalOrderId) ||
                Objects.isNull(paypalPaymentId) ||
                Objects.isNull(paypalSignature)){
            return ResponseEntity.badRequest().body(Map.of("message", "Missing required payment parameters"));
        }

        //Step 2: Call the service method
        boolean isValid = paymentService.verifyPayment(paypalOrderId, paypalPaymentId, paypalSignature);

        //Step 3: return the response
        if (isValid){
            return ResponseEntity.ok(Map.of(
                    "message", "Payment verified successfully",
                    "status", "success"
            ));
        } else {
            return ResponseEntity.badRequest().body(Map.of("message", "Payment verification failed"));
        }
    }

    @GetMapping("/history")
    public ResponseEntity<?> getPaymentHistory(Authentication authentication){
        //Step 1: Call the service method
        List<Payment> payments = paymentService.getUserPayment(authentication.getPrincipal());

        //Step 2: return the Response
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<?> getOrderDetails(@PathVariable String orderId) {
        //Step 1: Call the service method
        Payment paymentDetails = paymentService.getPaymentDetails(orderId);

        //Step 2: return response
        return ResponseEntity.ok(paymentDetails);
    }
}


