package com.orange.api.paymentmanagement.service.impl;

import com.orange.api.paymentmanagement.db.entities.FixedValuesDB;
import com.orange.api.paymentmanagement.db.entities.PaymentRegistryDB;
import com.orange.api.paymentmanagement.db.repository.FixedValuesRepository;
import com.orange.api.paymentmanagement.db.repository.PaymentRegistryRepository;
import com.orange.api.paymentmanagement.errors.CommonServiceErrors;
import com.orange.api.paymentmanagement.errors.PaymentmanagementErrors;
import com.orange.api.paymentmanagement.feign.client.IntegrationServiceWSPspClient;
import com.orange.api.paymentmanagement.mappers.PaymentCancellationMapper;
import com.orange.api.paymentmanagement.service.PaymentLinkCancellationService;
import com.orange.api.paymentmanagement.utils.Constants;
import com.orange.api.paymentmanagement.web.model.Payment;
import com.orange.api.paymentmanagement.web.model.integrationservicewspsp.RequestCancellationPaymentLink;
import com.orange.openapiosp.boot.errorhandler.exception.OpenApiBadRequestException;
import com.orange.openapiosp.boot.errorhandler.exception.OpenApiCustomException;
import com.orange.openapiosp.boot.errorhandler.exception.OpenApiNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeoutException;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentLinkCancellationServiceImpl implements PaymentLinkCancellationService {

    private final FixedValuesRepository fixedValuesRepository;
    private final PaymentRegistryRepository paymentRegistryRepository;
    private final IntegrationServiceWSPspClient integrationServiceWSPspClient;

    @Override
    public Payment paymentLinkCancellation(Payment payment, String numOrigin, String zLogin) {
        payment.setId(numOrigin);
        PaymentRegistryDB paymentRegistryDB = this.paymentRegistryRepository.findByOriginOpnumber(numOrigin);
        if (ObjectUtils.isEmpty(paymentRegistryDB)) {
            log.error("No se ha encontrado ningun registro en BBDD, flujo payment Link Cancellation");
            throw new OpenApiNotFoundException(PaymentmanagementErrors.ERROR_REGISTER_NOTFOUND.toString());
        }

        String transactionType = paymentRegistryDB.getTransactiontype();
        String app = paymentRegistryDB.getChannel();

        FixedValuesDB fixedValuesDB = Optional.ofNullable(this.fixedValuesRepository.findByTransactiontypeAndApp(transactionType, app))
                .filter(list -> !list.isEmpty())
                .flatMap(list -> list.stream().findFirst())
                .orElseThrow(() -> new OpenApiBadRequestException(PaymentmanagementErrors.INVALID_APP.toString()));
        RequestCancellationPaymentLink requestCancellationPaymentLink =
                PaymentCancellationMapper.mapPaymentToRequestCancellationPsp(payment, fixedValuesDB, paymentRegistryDB, zLogin);
        try {
            log.info("Llamada al integrationwspsp paymentLinkCancellation {}", requestCancellationPaymentLink.toString());
            var response = integrationServiceWSPspClient.cancellationPaymentLink(requestCancellationPaymentLink);
            log.info("Respuesta del integrationwspsp paymentLinkCancellation: {}", response.toString());
            PaymentCancellationMapper.mapPaymentCancellationLinkRegistryOk(paymentRegistryDB, requestCancellationPaymentLink);
        } catch (Exception e) {
            evaluateErrorPaymentLink(paymentRegistryDB, requestCancellationPaymentLink, e);

        }
        this.paymentRegistryRepository.save(paymentRegistryDB);

        return payment;
    }

    private void evaluateErrorPaymentLink(PaymentRegistryDB paymentRegistryDB,
                                          RequestCancellationPaymentLink requestCancellationPaymentLink, Exception e) {
        if (e instanceof TimeoutException || e.getCause() instanceof TimeoutException) {
            PaymentCancellationMapper.mapPaymentCancellationLinkRegistryError(paymentRegistryDB,
                    requestCancellationPaymentLink,
                    Constants.PAYMENT_LINK_CANCELLATION_ERROR, Constants.PAYMENT_LINK_CANCELLATION_TIME_OUT_ERROR);
            log.info("La llamada para cancelar payment link con número operación origen:".concat(" ")
                    .concat(requestCancellationPaymentLink.getNumOpOrigin())
                    .concat("ha fallado por Timeout"));
            throw new OpenApiCustomException(HttpStatus.GATEWAY_TIMEOUT,
                    CommonServiceErrors.SERVICE_UNAVAILABLE.toString(), e.getMessage());

        } else {
            PaymentCancellationMapper.mapPaymentCancellationLinkRegistryError(paymentRegistryDB,
                    requestCancellationPaymentLink,
                    Constants.PAYMENT_LINK_CANCELLATION_ERROR, Constants.PAYMENT_LINK_CANCELLATION_ERROR_DESC);
            log.info("La llamada para cancelar payment link con número operación origen:"
                    .concat(" ")
                    .concat(requestCancellationPaymentLink.getNumOpOrigin())
                    .concat(" ")
                    .concat("falló"));
            throw new OpenApiCustomException(HttpStatus.INTERNAL_SERVER_ERROR,
                    CommonServiceErrors.SERVICE_ERROR_CALL.toString(), e.getMessage());
        }
    }
}
