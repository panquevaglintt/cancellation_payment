package com.orange.api.paymentmanagement.service.impl;

import com.orange.api.paymentmanagement.db.entities.FixedValuesDB;
import com.orange.api.paymentmanagement.db.entities.PaymentRegistryDB;
import com.orange.api.paymentmanagement.db.repository.FixedValuesRepository;
import com.orange.api.paymentmanagement.db.repository.PaymentRegistryRepository;
import com.orange.api.paymentmanagement.feign.client.IntegrationServiceWSPspClient;
import com.orange.api.paymentmanagement.web.model.Payment;
import com.orange.api.paymentmanagement.web.model.integrationservicewspsp.RequestCancellationPaymentLink;
import com.orange.api.paymentmanagement.web.model.integrationservicewspsp.ResponseCancellationPaymentLink;
import com.orange.openapiosp.boot.errorhandler.exception.OpenApiBadRequestException;
import com.orange.openapiosp.boot.errorhandler.exception.OpenApiNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.util.Collections;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PaymentLinkCancellationServiceImplTest {

    @Mock
    private FixedValuesRepository fixedValuesRepository;

    @Mock
    private PaymentRegistryRepository paymentRegistryRepository;

    @Mock
    private IntegrationServiceWSPspClient integrationServiceWSPspClient;

    @Mock
    private ResponseCancellationPaymentLink responseCancellationPaymentLink;

    @InjectMocks
    private PaymentLinkCancellationServiceImpl paymentLinkCancellationService;


    private Payment payment;
    private PaymentRegistryDB paymentRegistryDB;
    private FixedValuesDB fixedValuesDB;

    @BeforeEach
    public void setUp() {
        payment = new Payment();
        paymentRegistryDB = new PaymentRegistryDB();
        paymentRegistryDB.setTransactiontype("transactionType");
        paymentRegistryDB.setChannel("app");

        fixedValuesDB = new FixedValuesDB();
    }

    @Test
    public void testPaymentLinkCancellation_Success() {
        when(paymentRegistryRepository.findByOriginOpnumber(anyString())).thenReturn(paymentRegistryDB);
        when(fixedValuesRepository.findByTransactiontypeAndApp(anyString(), anyString()))
                .thenReturn(Collections.singletonList(fixedValuesDB));
        when(integrationServiceWSPspClient.cancellationPaymentLink(any(RequestCancellationPaymentLink.class)))
                .thenReturn(responseCancellationPaymentLink);

        Payment result = paymentLinkCancellationService.paymentLinkCancellation(payment, "numOrigin","zLogin");

        assertNotNull(result);
    }

    @Test
    public void testPaymentLinkCancellation_NoPaymentRegistry() {
        when(paymentRegistryRepository.findByOriginOpnumber(anyString())).thenReturn(null);

        assertThrows(OpenApiNotFoundException.class, () -> {
            paymentLinkCancellationService.paymentLinkCancellation(payment, "numOrigin", "zLogin");
        });
    }

    @Test
    public void testPaymentLinkCancellation_NoFixedValues() {
        when(paymentRegistryRepository.findByOriginOpnumber(anyString())).thenReturn(paymentRegistryDB);
        when(fixedValuesRepository.findByTransactiontypeAndApp(anyString(), anyString()))
                .thenReturn(Collections.emptyList());

        assertThrows(OpenApiBadRequestException.class, () -> {
            paymentLinkCancellationService.paymentLinkCancellation(payment, "numOrigin", "zLogin");
        });
    }


    @Mock
    private MessageSource messageSource; // Simular el MessageSource

    @Test
    public void testPaymentLinkCancellation_TimeOutError() throws InstantiationException, IllegalAccessException {

        when(this.paymentRegistryRepository.findByOriginOpnumber(Mockito.anyString())).thenReturn(paymentRegistryDB);
        when(fixedValuesRepository.findByTransactiontypeAndApp(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(Collections.singletonList(fixedValuesDB));
        when(integrationServiceWSPspClient.cancellationPaymentLink(any(RequestCancellationPaymentLink.class)))
                .thenThrow(new RuntimeException(new TimeoutException("Timeout occurred")));

        assertThrows(Exception.class, () -> {
             paymentLinkCancellationService.paymentLinkCancellation(payment, "numOrigin", "zLogin");
        });
    }

    @Test
    public void testPaymentLinkCancellation_Error() throws InstantiationException, IllegalAccessException {

        when(this.paymentRegistryRepository.findByOriginOpnumber(Mockito.anyString())).thenReturn(paymentRegistryDB);
        when(fixedValuesRepository.findByTransactiontypeAndApp(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(Collections.singletonList(fixedValuesDB));
        when(integrationServiceWSPspClient.cancellationPaymentLink(any(RequestCancellationPaymentLink.class)))
                .thenThrow(new RuntimeException(new Exception("Error occurred")));

        assertThrows(Exception.class, () -> {
            paymentLinkCancellationService.paymentLinkCancellation(payment, "numOrigin", "zLogin");
        });
    }

}