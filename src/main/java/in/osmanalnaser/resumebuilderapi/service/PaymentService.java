package in.osmanalnaser.resumebuilderapi.service;

import in.osmanalnaser.resumebuilderapi.document.Payment;
import in.osmanalnaser.resumebuilderapi.document.User;
import in.osmanalnaser.resumebuilderapi.dto.AuthResponse;
import in.osmanalnaser.resumebuilderapi.repository.PaymentRepository;
import in.osmanalnaser.resumebuilderapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.paypal.api.payments.Payer;

import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static in.osmanalnaser.resumebuilderapi.util.AppConstants.PREMIUM;


@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final AuthService authService;
    private final UserRepository userRepository;
    @Value("${paypal.client-id}")
    private String paypalKeyId;
    @Value("${paypal.client-secret}")
    private String paypalKeySecret;

    public Payment createOrder(Object principal, String planType) throws Exception {
        AuthResponse authResponse = authService.getProfile(principal);

        // Step 1: Initialize PayPal client
        APIContext apiContext = new APIContext(paypalKeyId, paypalKeySecret, "sandbox");

        // Step 2: Prepare order
        Amount amount = new Amount();
        amount.setCurrency("EUR");
        amount.setTotal("10.00");

        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setDescription(planType + " Plan");

        List<Transaction> transactions = new ArrayList<>();
        transactions.add(transaction);

        com.paypal.api.payments.Payment paypalPayment = new com.paypal.api.payments.Payment();
        paypalPayment.setIntent("sale");
        Payer payer = new Payer();
        payer.setPaymentMethod("paypal");
        paypalPayment.setPayer(payer);
        paypalPayment.setTransactions(transactions);

        RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setReturnUrl("http://localhost:5173/payment/success");
        redirectUrls.setCancelUrl("http://localhost:5173/payment/cancel");
        paypalPayment.setRedirectUrls(redirectUrls);

        // Step 3: Create order
        com.paypal.api.payments.Payment createdPayment = paypalPayment.create(apiContext);

        // Step 4: Save to database
        Payment newPayment = Payment.builder()
                .userId(authResponse.getId())
                .paypalOrderId(createdPayment.getId())
                .amount(10)
                .currency("EUR")
                .planType(planType)
                .status("created")
                .receipt(PREMIUM + "_" + UUID.randomUUID().toString().substring(0, 8))
                .build();

        return paymentRepository.save(newPayment);
    }

    public boolean verifyPayment(String paypalOrderId, String paypalPaymentId, String paypalSignature) {
        try {
            // Step 1: Initialize PayPal client
            APIContext apiContext = new APIContext(paypalKeyId, paypalKeySecret, "sandbox");

            // Step 2: Fetch the payment from PayPal and check status
            com.paypal.api.payments.Payment paypalPayment = com.paypal.api.payments.Payment.get(apiContext, paypalPaymentId);

            if ("approved".equals(paypalPayment.getState())) {
                // Step 3: Update payment in database
                Payment payment = paymentRepository.findByPaypalOrderId(paypalOrderId)
                        .orElseThrow(() -> new RuntimeException("Payment not found"));
                payment.setPaypalPaymentId(paypalPaymentId);
                payment.setPaypalSignature(paypalSignature);
                payment.setStatus("paid");
                paymentRepository.save(payment);

                // Step 4: Upgrade user subscription
                upgradeUserSubscription(payment.getUserId(), payment.getPlanType());
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("Error verifying the payment", e);
            return false;
        }
    }

    private void upgradeUserSubscription(String userId, String planType) {
        User existingUser = userRepository.findById(userId)  // ← nur userId
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        existingUser.setSubscriptionPlan(planType);
        userRepository.save(existingUser);
        log.info("User {} upgraded to {}", userId, planType);
    }

    public List<Payment> getUserPayment(Object principal) {
        //Step 1: Get the current profile
        AuthResponse authResponse = authService.getProfile(principal);

        //Step 2: Call the repo finder method
        return paymentRepository.findByUserIdOrderByCreatedAtDesc(authResponse.getId());

    }

    public Payment getPaymentDetails(String orderId) {
        //Step 1: Call the repo finder method
        return paymentRepository.findByPaypalOrderId(orderId)
                .orElseThrow(()-> new RuntimeException("Payment not found"));
    }
}
