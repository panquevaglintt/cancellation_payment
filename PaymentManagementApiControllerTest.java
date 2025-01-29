package com.orange.api.paymentmanagement.web.controller;

import com.orange.api.paymentmanagement.service.impl.PaymentManagementServiceImpl;
import com.orange.api.paymentmanagement.utils.Constants;
import com.orange.api.paymentmanagement.utils.TestUtils;
import com.orange.api.paymentmanagement.web.model.*;
import com.orange.openapiosp.boot.security.jwt.service.OpenApiSecurityService;
import com.orange.openapiosp.boot.security.jwt.util.JwtClaim;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@SpringBootTest
@DirtiesContext
@RunWith(SpringRunner.class)
public class PaymentManagementApiControllerTest {

    @Mock
    PaymentManagementServiceImpl paymentManagementServiceImpl;

    @Mock
    OpenApiSecurityService securityService;

    @InjectMocks
    PaymentManagementApiController controller;

    /**
     * This test checks serviceTestFind feature at Service Test Management API
     * controller.
     **/

    @Test
    public void serviceTestCreatePayment_OK() {

        Payment payment = createPayment();
        payment.setType(Constants.E_PAYMENT);
        OffsetDateTime statusDate = OffsetDateTime.parse("2024-07-01T10:11:21+02:00");
        payment.setStatusDate(statusDate);

        Mockito.when(securityService.getByClaimsName(JwtClaim.Z_APP)).thenReturn("zApp");
        Mockito.when(securityService.getByClaimsName(JwtClaim.Z_LOGIN)).thenReturn("zLogin");

        ResponseEntity<Payment> paymentResult = ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                .body(payment);

        Mockito.when(paymentManagementServiceImpl.generationPaymentLink(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(paymentResult);

        ResponseEntity<Payment> responseEntity = controller.paymentCreate(payment, "Bearer 124143");

        assertTrue(responseEntity.getStatusCode().equals(HttpStatus.OK));
    }

    @Test
    public void serviceTestStatusPayment_OK() {

        Payment payment = createPayment();

        ResponseEntity<Payment> paymentResult = ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                .body(payment);

        Mockito.when(paymentManagementServiceImpl.statusPaymentLink(Mockito.any(),Mockito.any())).thenReturn(paymentResult);

        ResponseEntity<Payment> responseEntity = controller.paymentGet("id","E-Payment");

        assertTrue(responseEntity.getStatusCode().equals(HttpStatus.OK));
    }

    @Test
    public void serviceTestUpdatePayment_OK() {

        Payment payment = createPayment();

        Mockito.when(securityService.getByClaimsName(JwtClaim.Z_APP)).thenReturn("zApp");

        ResponseEntity<Payment> paymentResult = ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                .body(payment);

        Mockito.when(paymentManagementServiceImpl.updateStatusPayment(Mockito.any(), Mockito.any()))
                .thenReturn(paymentResult);

        ResponseEntity<Payment> responseEntity = controller.paymentPatch("id", payment);

        assertTrue(responseEntity.getStatusCode().equals(HttpStatus.OK));
    }

    @Test
    public void serviceTestCancellationPayment_OK() {

        Payment payment = createPayment();
        payment.setStatus("Cancelled");

        Mockito.when(securityService.getByClaimsName(JwtClaim.Z_APP)).thenReturn("zApp");

        ResponseEntity<Payment> paymentResult = ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                .body(payment);

        Mockito.when(paymentManagementServiceImpl.paymentLinkAnnulation(Mockito.any(), Mockito.any()))
                .thenReturn(paymentResult);

        ResponseEntity<Payment> responseEntity = controller.paymentPatch("id", payment);

        assertTrue(responseEntity.getStatusCode().equals(HttpStatus.OK));
    }

    @Test
    public void paymentReportCsv_OK() {

        Mockito.when(paymentManagementServiceImpl.paymentReportCsvGenerate(Mockito.any(), Mockito.any()))
                .thenReturn(new ResponseEntity<byte[]>(HttpStatus.OK));

        ResponseEntity<byte[]> fraudReportCsv = controller.paymentReportCsv("12", "21");

        assertEquals(HttpStatus.OK, fraudReportCsv.getStatusCode());
    }

    @Test
    public void cleanDb_OK() {

        doNothing().when(paymentManagementServiceImpl).cleanDb();

        controller.cleanDb();
    }
    
    /**
     * This test checks serviceTestFind feature at Service Test Management API
     * controller.
     **/

    @Test
    public void serviceTestCreatePaymentMarketPlace_OK() {

        Payment payment = createPayment();
        payment.setType(Constants.MP_PAYMENT);
        OffsetDateTime statusDate = OffsetDateTime.parse("2024-11-26T14:30:40Z");
        payment.setStatusDate(statusDate);

        Mockito.when(securityService.getByClaimsName(JwtClaim.Z_APP)).thenReturn("zApp");
        Mockito.when(securityService.getByClaimsName(JwtClaim.Z_LOGIN)).thenReturn("zLogin");

        ResponseEntity<Payment> paymentResult = ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                .body(payment);

        Mockito.when(paymentManagementServiceImpl.generationPaymentMarketPlace(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(paymentResult);

        ResponseEntity<Payment> responseEntity = controller.paymentCreate(payment, "Bearer 124143");

        assertTrue(responseEntity.getStatusCode().equals(HttpStatus.OK));
    }
    
    @Test
    public void serviceTestSearchPaymentMarketPlace_MSISDN_OK() {
    	List<Payment> paymentList = new ArrayList<>();
        Payment payment = createPayment();
        payment.setType(Constants.MP_PAYMENT);
        paymentList.add(payment);

        ResponseEntity<List<Payment>> paymentResult = ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                .body(paymentList);

        Mockito.when(paymentManagementServiceImpl.searchPaymentMarketPlace(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(paymentResult);

        ResponseEntity<List<Payment>> responseEntity = controller.paymentFind(Constants.MP_PAYMENT, "MSISDN", "654678678", null, null, null, null, null, null, null);

        assertTrue(responseEntity.getStatusCode().equals(HttpStatus.OK));
    }
    
    @Test
    public void serviceTestSearchPaymentMarketPlace_Individual_OK() {
    	List<Payment> paymentList = new ArrayList<>();
        Payment payment = createPayment();
        payment.setType(Constants.MP_PAYMENT);
        paymentList.add(payment);

        ResponseEntity<List<Payment>> paymentResult = ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                .body(paymentList);

        Mockito.when(paymentManagementServiceImpl.searchPaymentMarketPlace(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(paymentResult);

        ResponseEntity<List<Payment>> responseEntity = controller.paymentFind(Constants.MP_PAYMENT, null, null, Constants.REFERRED_TYPE_INDIVIDUAL, "45657865X", "NIF", null, null, null, null);

        assertTrue(responseEntity.getStatusCode().equals(HttpStatus.OK));
    }
    
    
    
    @Test
    public void serviceTestSearchPaymentMarketPlace_Organization_OK() {
    	List<Payment> paymentList = new ArrayList<>();
        Payment payment = createPayment();
        payment.setType(Constants.MP_PAYMENT);
        paymentList.add(payment);

        ResponseEntity<List<Payment>> paymentResult = ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                .body(paymentList);

        Mockito.when(paymentManagementServiceImpl.searchPaymentMarketPlace(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(paymentResult);

        ResponseEntity<List<Payment>> responseEntity = controller.paymentFind(Constants.MP_PAYMENT, null, null, Constants.REFERRED_TYPE_ORGANIZATION, "45657865X", "NIF", null, null, null, null);

        assertTrue(responseEntity.getStatusCode().equals(HttpStatus.OK));
    }
    
    @Test
    public void serviceTestReturnedPayment_OK() {

        Payment payment = createPayment();
        payment.setStatus("Returned");
        payment.setType("MP-Payment");

        Mockito.when(securityService.getByClaimsName(JwtClaim.Z_APP)).thenReturn("zApp");

        ResponseEntity<Payment> paymentResult = ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                .body(payment);

        Mockito.when(paymentManagementServiceImpl.paymentReturnMarketPlace(Mockito.any(), Mockito.any()))
                .thenReturn(paymentResult);

        ResponseEntity<Payment> responseEntity = controller.paymentPatch("id", payment);

        assertTrue(responseEntity.getStatusCode().equals(HttpStatus.OK));
    }
    
    @Test
    public void serviceTestTokenPayment_OK() {

        Payment payment = createPayment();
        payment.setType(Constants.TOKEN_PAYMENT);
        PaymentMethodRefOrValue paymentMethod = new PaymentMethodRefOrValue();
        paymentMethod.setId("12345");
        payment.setPaymentMethod(paymentMethod);
        Money totalAmount = new Money();
        totalAmount.setUnit("EUR");
        totalAmount.setValue((float) 50);
        payment.setTotalAmount(totalAmount);
        List<Characteristic> characteristicList = new ArrayList<>();
        
        Characteristic characteristic = null;
        characteristic = new Characteristic();
        characteristic.setName(Constants.PHONENUMBER);
        characteristic.setValue("123456");
        characteristicList.add(characteristic);
        
        characteristic = new Characteristic();
        characteristic.setName(Constants.TRANSACTION);
        characteristic.setValue(Constants.SCHEDULED_TOP_UP);
        characteristicList.add(characteristic);
        
        characteristic = new Characteristic();
        characteristic.setName(Constants.PROCESSFLOW_ID);
        characteristic.setValue("Prueba");
        characteristicList.add(characteristic);
        
        payment.setCharacteristic(characteristicList);

        Mockito.when(securityService.getByClaimsName(JwtClaim.Z_APP)).thenReturn("zApp");
        Mockito.when(securityService.getByClaimsName(JwtClaim.Z_LOGIN)).thenReturn("zLogin");
        Mockito.when(securityService.getByClaimsName(JwtClaim.Z_CALLER_IP)).thenReturn("zCallerIp");

        ResponseEntity<Payment> paymentResult = ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                .body(payment);

        Mockito.when(paymentManagementServiceImpl.generationTokenPayment(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(paymentResult);

        ResponseEntity<Payment> responseEntity = controller.paymentCreate(payment, "Bearer 124143");

        assertTrue(responseEntity.getStatusCode().equals(HttpStatus.OK));
    }

    @Test
    public void cancellationPaymentLinkTest_OK(){
        Payment rqPaymentCancellation = TestUtils.createValidCancellationPaymentLink();

        Payment payment = createPayment();
        payment.setType(Constants.PAYMENT_LINK_TYPE_CANCELLATION);
        ResponseEntity<Payment> paymentResult = ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                .body(payment);
        when(securityService.getByClaimsName(JwtClaim.Z_LOGIN)).thenReturn("zLogin");
        Mockito.doReturn(paymentResult).when(paymentManagementServiceImpl).paymentLinkCancellation(Mockito.eq(rqPaymentCancellation), any(), any());
        ResponseEntity<Payment> responseEntity = controller.paymentPatch("A123AS", rqPaymentCancellation);
        assertTrue(responseEntity.getStatusCode().equals(HttpStatus.OK));

    }

    private Payment createPayment() {
        Payment payment = new Payment();

        payment.setTotalAmount(createTotalAmount());
        payment.setPaymentItem(createPaymentItem());
        payment.setCharacteristic(createCharacteristic());
        payment.setPayer(createPayer());

        return payment;
    }

    private RelatedParty createPayer() {
        RelatedParty payer = new RelatedParty();
        payer.setReferredType(Constants.REFERRED_TYPE_INDIVIDUAL);
        payer.setRole(Constants.CLIENT);
        payer.setIndividualIdentification(createIndividual());
        return payer;
    }

    private List<IndividualIdentification> createIndividual() {
        List<IndividualIdentification> listIndividual = new ArrayList<IndividualIdentification>();

        IndividualIdentification individual = new IndividualIdentification();
        individual.setIdentificationId("id");
        individual.setIdentificationType("NIF");
        listIndividual.add(individual);

        return listIndividual;
    }

    private List<Characteristic> createCharacteristic() {
        List<Characteristic> listCharacteristic = new ArrayList<Characteristic>();

        Characteristic characteristic = new Characteristic();
        characteristic.setName(Constants.PHONENUMBER);
        characteristic.setValue("telefono");
        listCharacteristic.add(characteristic);

        characteristic = new Characteristic();
        characteristic.setName(Constants.VALIDITYTIME);
        characteristic.setValue("8");
        listCharacteristic.add(characteristic);

        characteristic = new Characteristic();
        characteristic.setName(Constants.PAYMENTRETRIES);
        characteristic.setValue("3");
        listCharacteristic.add(characteristic);

        characteristic = new Characteristic();
        characteristic.setName(Constants.TRANSACTION);
        characteristic.setValue("amortization");
        listCharacteristic.add(characteristic);

        return listCharacteristic;
    }

    private List<PaymentItem> createPaymentItem() {
        List<PaymentItem> listPaymentItem = new ArrayList<PaymentItem>();
        PaymentItem paymentItem = new PaymentItem();
        EntityRef item = new EntityRef();
        item.setId("id_del_vap");
        item.setRole(Constants.VAP);
        item.setCharacteristic(createCharacteristicEntityRef());
        paymentItem.setItem(item);
        listPaymentItem.add(paymentItem);
        return listPaymentItem;
    }

    private List<Characteristic> createCharacteristicEntityRef() {
        List<Characteristic> listCharacteristic = new ArrayList<Characteristic>();
        Characteristic characteristic = new Characteristic();
        characteristic.setName(Constants.BSCSCODE);
        characteristic.setValue("codigo_bscs");
        listCharacteristic.add(characteristic);
        return listCharacteristic;
    }

    private Money createTotalAmount() {
        Money money = new Money();
        money.setUnit(Constants.EUR);
        money.setValue(10f);
        return money;
    }

}