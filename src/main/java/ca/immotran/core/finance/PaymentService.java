package ca.immotran.core.finance;

import ca.immotran.core.finance.dto.CreatePaymentRequest;
import ca.immotran.core.finance.dto.PaymentResponse;
import ca.immotran.core.finance.dto.RecordPaymentRequest;
import ca.immotran.core.lease.Lease;
import ca.immotran.core.lease.LeaseService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository repository;
    private final LeaseService leaseService;

    public PaymentService(PaymentRepository repository, LeaseService leaseService) {
        this.repository = repository;
        this.leaseService = leaseService;
    }

    public PaymentResponse create(UUID unitId, UUID leaseId, CreatePaymentRequest request) {
        Lease lease = leaseService.getEntityById(unitId, leaseId);
        Payment saved = repository.save(new Payment(lease, request.dueDate(), request.amountDue()));
        return PaymentResponse.from(saved);
    }

    public PaymentResponse recordPayment(UUID leaseId, UUID paymentId, RecordPaymentRequest request) {
        Payment payment = getEntityByIdAndLease(paymentId, leaseId);
        payment.recordPayment(request.amountPaid(), request.paidAt() != null ? request.paidAt() : Instant.now());
        return PaymentResponse.from(repository.save(payment));
    }

    public PaymentResponse getById(UUID leaseId, UUID paymentId) {
        return PaymentResponse.from(getEntityByIdAndLease(paymentId, leaseId));
    }

    public List<PaymentResponse> listByLease(UUID leaseId) {
        return repository.findByLeaseId(leaseId).stream()
                .map(PaymentResponse::from)
                .toList();
    }

    private Payment getEntityByIdAndLease(UUID paymentId, UUID leaseId) {
        Payment payment = repository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));

        if (!payment.getLease().getId().equals(leaseId)) {
            throw new PaymentNotFoundException(paymentId);
        }

        return payment;
    }
}
