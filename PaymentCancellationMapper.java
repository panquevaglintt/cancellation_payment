package com.orange.api.paymentmanagement.mappers;

import com.orange.api.paymentmanagement.db.entities.FixedValuesDB;
import com.orange.api.paymentmanagement.db.entities.PaymentRegistryDB;
import com.orange.api.paymentmanagement.utils.Constants;
import com.orange.api.paymentmanagement.utils.PaymentManagementUtils;
import com.orange.api.paymentmanagement.web.model.Payment;
import com.orange.api.paymentmanagement.web.model.integrationservicewspsp.*;
import lombok.extern.slf4j.Slf4j;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Slf4j
public final class PaymentCancellationMapper {

    private PaymentCancellationMapper() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static RequestCancellationPaymentLink mapPaymentToRequestCancellationPsp(
                                                                                    FixedValuesDB fixedValues, PaymentRegistryDB paymentRegistryDB, String zLogin) {
        RequestCancellationPaymentLink requestCancellationPaymentLink = new RequestCancellationPaymentLink();

        requestCancellationPaymentLink.setClientData(mapClientData(fixedValues, paymentRegistryDB));
        requestCancellationPaymentLink.setAdditionalParams(mapAdditionalParameters(paymentRegistryDB,zLogin));
        requestCancellationPaymentLink.setUser(zLogin);
        requestCancellationPaymentLink.setLanguage(fixedValues.getLanguage());
        requestCancellationPaymentLink.setNumOpOrigin(PaymentManagementUtils.getNumOrigen());
        requestCancellationPaymentLink.setNumOperationNPP(paymentRegistryDB.getPaymentLink());
        return requestCancellationPaymentLink;
    }

    private static DatosCliente mapClientData(FixedValuesDB fixedValues, PaymentRegistryDB paymentRegistryDB) {
        DatosCliente datosCliente = new DatosCliente();

        datosCliente.setCoCliente(fixedValues.getClientCode());
        if (Objects.nonNull(paymentRegistryDB.getCommercialCode())) {
            datosCliente.setCoComercio(paymentRegistryDB.getCommercialCode());
        } else {
            datosCliente.setCoComercio(fixedValues.getCommercialCode());
        }
        datosCliente.setCoTerminal(fixedValues.getTerminalCode());
        datosCliente.setPalabraClave(fixedValues.getKeyword());

        return datosCliente;
    }

    private static ParametrosAdicionales mapAdditionalParameters(PaymentRegistryDB paymentRegistryDB, String zLogin) {
        ParametrosAdicionales parameters = new ParametrosAdicionales();
        List<Entry> listEntry = new ArrayList<>();

        Entry entry = new Entry();
        entry.setKey(Constants.MSISDN);
        entry.setValue(paymentRegistryDB.getPhonenumber());
        listEntry.add(entry);

        entry = new Entry();
        entry.setKey(Constants.DOCUMENTO);
        entry.setValue(paymentRegistryDB.getIdDocument());
        listEntry.add(entry);

        entry = new Entry();
        entry.setKey(Constants.USUARIO);
        entry.setValue(zLogin);
        listEntry.add(entry);

        parameters.setEntry(listEntry);

        return parameters;
    }

    public static void mapPaymentCancellationLinkRegistryOk(PaymentRegistryDB paymentRegistryDB
            , RequestCancellationPaymentLink requestCancellationPaymentLink) {
        paymentRegistryDB.setStatus(Constants.PAYMENT_LINK_CANCELLATION_OK_STATUS);
        paymentRegistryDB.setStatusDescription(Constants.PAYMENT_LINK_CANCELLATION_OK_DESC);
        paymentRegistryDB.setAnnulationDate(mapTimestampCancellationPaymentLink());
        paymentRegistryDB.setAnnulationOpnumber(requestCancellationPaymentLink.getNumOpOrigin());
        paymentRegistryDB.setAnnulationResult(Constants.PAYMENT_LINK_CANCELLATION_OK_STATUS);
        paymentRegistryDB.setAnnulationDescription(Constants.PAYMENT_LINK_CANCELLATION_OK_DESC);
        paymentRegistryDB.setResultDescription(Constants.OK);

    }

    public static void mapPaymentCancellationLinkRegistryError(PaymentRegistryDB paymentRegistryDB,
                                                               RequestCancellationPaymentLink requestCancellationPaymentLink
            , String status, String description) {

        paymentRegistryDB.setStatus(status);
        paymentRegistryDB.setStatusDescription(description);
        paymentRegistryDB.setAnnulationDate(mapTimestampCancellationPaymentLink());
        paymentRegistryDB.setAnnulationOpnumber(requestCancellationPaymentLink.getNumOpOrigin());
        paymentRegistryDB.setAnnulationResult(status);
        paymentRegistryDB.setAnnulationDescription(description);
        paymentRegistryDB.setResultDescription(Constants.KO);
    }

    private static Timestamp mapTimestampCancellationPaymentLink() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.C_YYYY_MM_DD_T_HH_MM_SSXXX);
        Date currentDate = new Date();
        try {
            String formattedDate = dateFormat.format(currentDate);
            Date parsedDate = dateFormat.parse(formattedDate);
            return new Timestamp(parsedDate.getTime());
        } catch (Exception e) {
            log.error("Error al generar el timestamp", e);
            return null;
        }
    }
}
