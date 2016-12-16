package travelsafe.paypal;

import com.paypal.api.payments.*;
import com.paypal.base.rest.PayPalRESTException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by aloha on 23-Nov-16.
 */
@Service
public class PayPalService {

    /**
     * When user clicks to buy a product on our website
     * @param orderCost how much user is charged
     * @param description description of payment
     * @param orderId id of an object that represents item (basket, or in our case it is TravelInsurance)
     * @param orderShippingCost cost of shipping
     * @return Link object - look for 'approval_url' - that is url where user should be redirected so he/she can
     *         confirm or cancel buying product - link is generated by PayPal
     * */
    public Links createPayment(long orderId, double orderCost, double orderShippingCost, double totalCost, String description){

        // Set payment details
        Details details = new Details();
        details.setShipping(String.valueOf(orderShippingCost));
        details.setSubtotal(String.valueOf(orderCost));
        details.setTax("0");

        // Payment amount
        Amount amount = new Amount();
        amount.setCurrency("USD");
        // Total must be equal to sum of shipping, tax and subtotal.
        amount.setTotal(String.valueOf(totalCost));
        amount.setDetails(details);

        // Transaction information
        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setDescription(description);

        //------------------------------------
        // Items TODO - home, car as items maybe?
        /*
        Item item = new Item();
        item.setName("Ground Coffee 40 oz").setQuantity("1").setCurrency("USD").setPrice("5");
        ItemList itemList = new ItemList();
        List<Item> items = new ArrayList<Item>();
        items.add(item);
        itemList.setItems(items);

        transaction.setItemList(itemList);
        */
        //------------------------------------

        // Add transaction to a list
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(transaction);

        // Add payer details
        Payer payer = new Payer();
        payer.setPaymentMethod("paypal");

        // Add payment details
        Payment payment = new Payment();
        payment.setIntent("sale");
        payment.setPayer(payer);
        payment.setTransactions(transactions);

        // Add redirect URLs
        RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setCancelUrl(PayPalConfig.CANCEL_URL+"/"+orderId);
        redirectUrls.setReturnUrl(PayPalConfig.RETURN_URL+"/"+orderId);
        payment.setRedirectUrls(redirectUrls);

        // Create payment
        System.out.println(">>> CREATING PAYMENT...");
        Payment createdPayment;
        try {
            createdPayment = payment.create(PayPalContext.context);
            //System.out.println(createdPayment.toJSON());
            Iterator links = createdPayment.getLinks().iterator();
            while (links.hasNext()) {
                Links link = (Links) links.next();
                if (link.getRel().equalsIgnoreCase("approval_url")) {
                    // REDIRECT USER TO link.getHref()
                    System.out.println(">>> CREATING PAYMENT STATUS: SUCCESS!");
                    System.out.println(link.toJSON());
                    //https://github.com/paypal/PayPal-Java-SDK/blob/master/rest-api-sample/src/main/java/com/paypal/api/payments/servlet/PaymentWithPayPalServlet.java
                    return link;
                }
            }
        } catch (PayPalRESTException e) {
            System.err.println(e.getDetails());
            System.out.println(">>> CREATING PAYMENT STATUS: ERROR");
        }

        return null;
    }

    /**
     * When user confirms payment
     * @param PayerID payer id generated by PayPal
     * @param paymentId payment id generated by PayPal
     * @return boolean status of executing payment
     * */
    public boolean executePayment(String PayerID, String paymentId){

        boolean retVal=false;

        /*
        req.getParameter("PayerID")
        req.getParameter("paymentId")
        */

        Payment payment = new Payment();
        payment.setId(paymentId);

        PaymentExecution paymentExecution = new PaymentExecution();
        paymentExecution.setPayerId(PayerID);

        try {
            Payment createdPayment = payment.execute(PayPalContext.context, paymentExecution);
            System.out.println(">>> SUCCESSFUL PAYMENT");
            System.out.println(createdPayment.toJSON());
            return true;

        } catch (PayPalRESTException e) {
            System.out.println(">>> PAYMENT ERROR");
            System.err.println(e.getDetails());
        }

        return false;
    }

    public void getPayPalPaymentInfo(String paymentId){
        try {
            Payment payment = Payment.get(PayPalContext.context, paymentId);
            //TODO - payment info
            //get stuff here
        } catch (PayPalRESTException e) {
            e.printStackTrace();
        }
    }

    public void createCreditCardPayment(){
        //TODO - credit card payment
    }

}
