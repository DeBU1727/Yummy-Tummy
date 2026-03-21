package com.yummytummy.backend.service;

import com.yummytummy.backend.entity.Order;
import com.yummytummy.backend.entity.OrderItem;
import com.yummytummy.backend.entity.MenuItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

// Brevo SDK imports
import sibApi.TransactionalEmailsApi;
import sibModel.SendSmtpEmail;
import sibModel.SendSmtpEmailSender;
import sibModel.SendSmtpEmailTo;
import sendinblue.ApiClient;
import sendinblue.ApiException;
import sendinblue.Configuration;
import sendinblue.auth.ApiKeyAuth;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Locale;

@Service
public class EmailService {

    @Value("${brevo.api.key}")
    private String apiKey;

    @Value("${brevo.sender.email}")
    private String senderEmail;

    @Value("${brevo.sender.name}")
    private String senderName;

    // Helper for currency formatting
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    public void sendOtpEmail(String toEmail, String otpCode) {
        String cleanKey = (apiKey != null) ? apiKey.replaceAll("\s+", "") : "";
        
        System.out.println("--- BREVO SDK ATTEMPT 2 ---");
        System.out.println("RECIPIENT: " + toEmail);

        try {
            // 1. Setup Client
            ApiClient defaultClient = Configuration.getDefaultApiClient();
            ApiKeyAuth auth = (ApiKeyAuth) defaultClient.getAuthentication("api-key");
            auth.setApiKey(cleanKey);

            // 2. Initialize API with the client (CRITICAL FIX)
            TransactionalEmailsApi apiInstance = new TransactionalEmailsApi(defaultClient);

            // 3. Sender
            SendSmtpEmailSender sender = new SendSmtpEmailSender();
            sender.setEmail(senderEmail.trim());
            sender.setName(senderName != null ? senderName : "Yummy Tummy");

            // 4. Recipient
            SendSmtpEmailTo receiver = new SendSmtpEmailTo();
            receiver.setEmail(toEmail.trim());

            // 5. Build Email
            SendSmtpEmail sendSmtpEmail = new SendSmtpEmail();
            sendSmtpEmail.setSender(sender);
            sendSmtpEmail.setTo(Collections.singletonList(receiver));
            sendSmtpEmail.setSubject("Your OTP: " + otpCode);
            sendSmtpEmail.setHtmlContent("Your verification code is: <b>" + otpCode + "</b>");

            // 6. Send
            apiInstance.sendTransacEmail(sendSmtpEmail);
            System.out.println("RESULT: Email sent successfully!");

        } catch (ApiException e) {
            // Catch the specific SDK exception to see the body
            String errorMsg = "Brevo API Error: " + e.getCode() + " - " + e.getResponseBody();
            System.err.println("RESULT: " + errorMsg);
            throw new RuntimeException(errorMsg);
        } catch (Exception e) {
            System.err.println("RESULT: General Error - " + e.getMessage());
            throw new RuntimeException("Email Error: " + e.getMessage());
        }
    }

    public void sendOrderConfirmationEmail(Order order) {
        String cleanKey = (apiKey != null) ? apiKey.replaceAll("\s+", "") : "";

        if (order.getUser() == null || order.getUser().getEmail() == null) {
            System.err.println("RESULT: Cannot send order confirmation email. User or user email is missing for Order ID: " + order.getId());
            return;
        }

        String toEmail = order.getUser().getEmail().trim();
        String customerName = order.getCustomerName() != null ? order.getCustomerName() : order.getUser().getFullName();

        System.out.println("--- BREVO SDK ---");
        System.out.println("Sending Order Confirmation to: " + toEmail + " for Order ID: " + order.getId());

        try {
            ApiClient defaultClient = Configuration.getDefaultApiClient();
            ApiKeyAuth auth = (ApiKeyAuth) defaultClient.getAuthentication("api-key");
            auth.setApiKey(cleanKey);

            TransactionalEmailsApi apiInstance = new TransactionalEmailsApi(defaultClient);

            SendSmtpEmailSender sender = new SendSmtpEmailSender();
            sender.setEmail(senderEmail.trim());
            sender.setName(senderName != null ? senderName : "Yummy Tummy");

            SendSmtpEmailTo receiver = new SendSmtpEmailTo();
            receiver.setEmail(toEmail);
            receiver.setName(customerName);

            String subject = "Congratulations! Your Yummy Tummy Order #" + order.getId() + " has been placed successfully.";
            String htmlContent = buildOrderConfirmationHtml(order, customerName);

            SendSmtpEmail sendSmtpEmail = new SendSmtpEmail();
            sendSmtpEmail.setSender(sender);
            sendSmtpEmail.setTo(Collections.singletonList(receiver));
            sendSmtpEmail.setSubject(subject);
            sendSmtpEmail.setHtmlContent(htmlContent);

            apiInstance.sendTransacEmail(sendSmtpEmail);
            System.out.println("RESULT: Order Confirmation Email sent successfully for Order ID: " + order.getId());

        } catch (ApiException e) {
            String errorMsg = "Brevo API Error for Order ID " + order.getId() + ": " + e.getCode() + " - " + e.getResponseBody();
            System.err.println("RESULT: " + errorMsg);
            throw new RuntimeException(errorMsg);
        } catch (Exception e) {
            System.err.println("RESULT: General Error for Order ID " + order.getId() + ": " + e.getMessage());
            throw new RuntimeException("Email Error: " + e.getMessage());
        }
    }

    public void sendOrderRejectedEmail(Order order) {
        String cleanKey = (apiKey != null) ? apiKey.replaceAll("\s+", "") : "";

        if (order.getUser() == null || order.getUser().getEmail() == null) {
            System.err.println("RESULT: Cannot send order rejected email. User or user email is missing for Order ID: " + order.getId());
            return;
        }

        String toEmail = order.getUser().getEmail().trim();
        String customerName = order.getCustomerName() != null ? order.getCustomerName() : order.getUser().getFullName();

        System.out.println("--- BREVO SDK ---");
        System.out.println("Sending Order Rejection to: " + toEmail + " for Order ID: " + order.getId());

        try {
            ApiClient defaultClient = Configuration.getDefaultApiClient();
            ApiKeyAuth auth = (ApiKeyAuth) defaultClient.getAuthentication("api-key");
            auth.setApiKey(cleanKey);

            TransactionalEmailsApi apiInstance = new TransactionalEmailsApi(defaultClient);

            SendSmtpEmailSender sender = new SendSmtpEmailSender();
            sender.setEmail(senderEmail.trim());
            sender.setName(senderName != null ? senderName : "Yummy Tummy");

            SendSmtpEmailTo receiver = new SendSmtpEmailTo();
            receiver.setEmail(toEmail);
            receiver.setName(customerName);

            String subject = "Your Yummy Tummy Order #" + order.getId() + " has been Rejected.";
            String htmlContent = buildOrderRejectedHtml(order, customerName);

            SendSmtpEmail sendSmtpEmail = new SendSmtpEmail();
            sendSmtpEmail.setSender(sender);
            sendSmtpEmail.setTo(Collections.singletonList(receiver));
            sendSmtpEmail.setSubject(subject);
            sendSmtpEmail.setHtmlContent(htmlContent);

            apiInstance.sendTransacEmail(sendSmtpEmail);
            System.out.println("RESULT: Order Rejection Email sent successfully for Order ID: " + order.getId());

        } catch (ApiException e) {
            String errorMsg = "Brevo API Error for Order ID " + order.getId() + ": " + e.getCode() + " - " + e.getResponseBody();
            System.err.println("RESULT: " + errorMsg);
            throw new RuntimeException(errorMsg);
        } catch (Exception e) {
            System.err.println("RESULT: General Error for Order ID " + order.getId() + ": " + e.getMessage());
            throw new RuntimeException("Email Error: " + e.getMessage());
        }
    }

    public void sendOrderCancellationEmail(Order order) {
        String cleanKey = (apiKey != null) ? apiKey.replaceAll("\s+", "") : "";

        if (order.getUser() == null || order.getUser().getEmail() == null) {
            System.err.println("RESULT: Cannot send order cancellation email. User or user email is missing for Order ID: " + order.getId());
            return;
        }

        String toEmail = order.getUser().getEmail().trim();
        String customerName = order.getCustomerName() != null ? order.getCustomerName() : order.getUser().getFullName();

        System.out.println("--- BREVO SDK ---");
        System.out.println("Sending Order Cancellation to: " + toEmail + " for Order ID: " + order.getId());

        try {
            ApiClient defaultClient = Configuration.getDefaultApiClient();
            ApiKeyAuth auth = (ApiKeyAuth) defaultClient.getAuthentication("api-key");
            auth.setApiKey(cleanKey);

            TransactionalEmailsApi apiInstance = new TransactionalEmailsApi(defaultClient);

            SendSmtpEmailSender sender = new SendSmtpEmailSender();
            sender.setEmail(senderEmail.trim());
            sender.setName(senderName != null ? senderName : "Yummy Tummy");

            SendSmtpEmailTo receiver = new SendSmtpEmailTo();
            receiver.setEmail(toEmail);
            receiver.setName(customerName);

            String subject = "Your Yummy Tummy Order #" + order.getId() + " has been Cancelled.";
            String htmlContent = buildOrderCancellationHtml(order, customerName);

            SendSmtpEmail sendSmtpEmail = new SendSmtpEmail();
            sendSmtpEmail.setSender(sender);
            sendSmtpEmail.setTo(Collections.singletonList(receiver));
            sendSmtpEmail.setSubject(subject);
            sendSmtpEmail.setHtmlContent(htmlContent);

            apiInstance.sendTransacEmail(sendSmtpEmail);
            System.out.println("RESULT: Order Cancellation Email sent successfully for Order ID: " + order.getId());

        } catch (ApiException e) {
            String errorMsg = "Brevo API Error for Order ID " + order.getId() + ": " + e.getCode() + " - " + e.getResponseBody();
            System.err.println("RESULT: " + errorMsg);
            throw new RuntimeException(errorMsg);
        } catch (Exception e) {
            System.err.println("RESULT: General Error for Order ID " + order.getId() + ": " + e.getMessage());
            throw new RuntimeException("Email Error: " + e.getMessage());
        }
    }


    private String buildOrderConfirmationHtml(Order order, String customerName) {
        StringBuilder htmlBuilder = new StringBuilder();
        htmlBuilder.append("<!DOCTYPE html>")
                   .append("<html lang='en'>")
                   .append("<head>")
                   .append("    <meta charset='UTF-8'>")
                   .append("    <meta name='viewport' content='width=device-width, initial-scale=1.0'>")
                   .append("    <title>Yummy Tummy Order Confirmation</title>")
                   .append("    <style>")
                   .append("        body { font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; margin: 0; padding: 0; background-color: #f4f4f4; color: #333; }")
                   .append("        table { width: 100%; border-collapse: collapse; }")
                   .append("        td, th { padding: 8px; text-align: left; border-bottom: 1px solid #ddd; }")
                   .append("        th { background-color: #f8f8f8; }")
                   .append("        .container { max-width: 600px; margin: 20px auto; background-color: #ffffff; padding: 20px; border-radius: 8px; box-shadow: 0 0 10px rgba(0, 0, 0, 0.1); }")
                   .append("        .header { background-color: #eb4d4b; color: #ffffff; padding: 15px 20px; border-radius: 8px 8px 0 0; text-align: center; }")
                   .append("        .header h1 { margin: 0; font-size: 24px; }")
                   .append("        .content { padding: 20px; }")
                   .append("        .footer { text-align: center; padding: 20px; font-size: 12px; color: #777; }")
                   .append("        .button { display: inline-block; background-color: #f0932b; color: #ffffff; padding: 10px 20px; text-decoration: none; border-radius: 5px; margin-top: 20px; }")
                   .append("        .highlight { color: #eb4d4b; font-weight: bold; }")
                   .append("        .total-row td { border-top: 2px solid #eb4d4b; font-weight: bold; }")
                   .append("    </style>")
                   .append("</head>")
                   .append("<body>")
                   .append("    <div class='container'>")
                   .append("        <div class='header'>")
                   .append("            <h1>Yummy Tummy Order Confirmation</h1>")
                   .append("        </div>")
                   .append("        <div class='content'>")
                   .append("            <p>Hi ").append(customerName).append(",</p>")
                   .append("            <p>Thank you for your order! Your order has been placed successfully.</p>")
                   .append("            <p><strong>Order Details:</strong></p>")
                   .append("            <table>")
                   .append("                <thead>")
                   .append("                    <tr>")
                   .append("                        <th>Item</th>")
                   .append("                        <th>Quantity</th>")
                   .append("                        <th>Price</th>")
                   .append("                        <th>Total</th>")
                   .append("                    </tr>")
                   .append("                </thead>")
                   .append("                <tbody>");

        for (OrderItem item : order.getOrderItems()) {
            MenuItem menuItem = item.getMenuItem();
            htmlBuilder.append("                    <tr>")
                       .append("                        <td>").append(menuItem != null ? menuItem.getName() : "N/A").append("</td>")
                       .append("                        <td>").append(item.getQuantity()).append("</td>")
                       .append("                        <td>").append(CURRENCY_FORMAT.format(item.getPrice())).append("</td>")
                       .append("                        <td>").append(CURRENCY_FORMAT.format(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))).append("</td>")
                       .append("                    </tr>");
        }

        htmlBuilder.append("                </tbody>")
                   .append("                <tfoot>")
                   .append("                    <tr><td colspan='3' style='text-align: right;'>Subtotal:</td><td>").append(CURRENCY_FORMAT.format(order.getSubtotal())).append("</td></tr>");
        
        // Assuming GST is 18% as per frontend, if it's dynamic, this needs to be fetched
        htmlBuilder.append("                    <tr><td colspan='3' style='text-align: right;'>GST (18%):</td><td>").append(CURRENCY_FORMAT.format(order.getGstAmount())).append("</td></tr>");

        if (order.getCouponCode() != null && !order.getCouponCode().isEmpty() && order.getDiscountAmount() != null && order.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
            htmlBuilder.append("                    <tr><td colspan='3' style='text-align: right;'>Coupon (").append(order.getCouponCode()).append("):</td><td>-").append(CURRENCY_FORMAT.format(order.getDiscountAmount())).append("</td></tr>");
        }

        htmlBuilder.append("                    <tr class='total-row'><td colspan='3' style='text-align: right;'>Total Payable:</td><td>").append(CURRENCY_FORMAT.format(order.getTotalPrice())).append("</td></tr>");
        htmlBuilder.append("                </tfoot>")
                   .append("            </table>")
                   .append("            <p><strong>Payment Method:</strong> ").append(order.getPaymentMethod().name().replace("_", " ")).append("</p>")
                   .append("            <p><strong>Order Type:</strong> ").append(order.getOrderType().name().replace("_", " ")).append("</p>");

        if (order.getOrderType() == Order.OrderType.DELIVERY) {
            htmlBuilder.append("            <p><strong>Delivery Address:</strong> ").append(order.getDeliveryAddress()).append("</p>")
                       .append("            <p><strong>Contact Number:</strong> ").append(order.getContactNumber()).append("</p>");
        }
        
        htmlBuilder.append("            <p>You will receive further updates on your order status. You can track your order status in your Yummy Tummy account.</p>")
                   .append("            <p>Thank you for choosing Yummy Tummy!</p>")
                   .append("        </div>")
                   .append("        <div class='footer'>")
                   .append("            <p>&copy; ").append(LocalDateTime.now().getYear()).append(" Yummy Tummy. All rights reserved.</p>")
                   .append("        </div>")
                   .append("    </div>")
                   .append("</body>")
                   .append("</html>");

        return htmlBuilder.toString();
    }

    private String buildOrderRejectedHtml(Order order, String customerName) {
        StringBuilder htmlBuilder = new StringBuilder();
        htmlBuilder.append("<!DOCTYPE html>")
                   .append("<html lang='en'>")
                   .append("<head>")
                   .append("    <meta charset='UTF-8'>")
                   .append("    <meta name='viewport' content='width=device-width, initial-scale=1.0'>")
                   .append("    <title>Yummy Tummy Order Rejected</title>")
                   .append("    <style>")
                   .append("        body { font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; margin: 0; padding: 0; background-color: #f4f4f4; color: #333; }")
                   .append("        table { width: 100%; border-collapse: collapse; }")
                   .append("        td, th { padding: 8px; text-align: left; border-bottom: 1px solid #ddd; }")
                   .append("        th { background-color: #f8f8f8; }")
                   .append("        .container { max-width: 600px; margin: 20px auto; background-color: #ffffff; padding: 20px; border-radius: 8px; box-shadow: 0 0 10px rgba(0, 0, 0, 0.1); }")
                   .append("        .header { background-color: #eb4d4b; color: #ffffff; padding: 15px 20px; border-radius: 8px 8px 0 0; text-align: center; }")
                   .append("        .header h1 { margin: 0; font-size: 24px; }")
                   .append("        .content { padding: 20px; }")
                   .append("        .footer { text-align: center; padding: 20px; font-size: 12px; color: #777; }")
                   .append("        .button { display: inline-block; background-color: #f0932b; color: #ffffff; padding: 10px 20px; text-decoration: none; border-radius: 5px; margin-top: 20px; }")
                   .append("        .highlight { color: #eb4d4b; font-weight: bold; }")
                   .append("    </style>")
                   .append("</head>")
                   .append("<body>")
                   .append("    <div class='container'>")
                   .append("        <div class='header'>")
                   .append("            <h1>Yummy Tummy Order Rejected</h1>")
                   .append("        </div>")
                   .append("        <div class='content'>")
                   .append("            <p>Hi ").append(customerName).append(",</p>")
                   .append("            <p>We regret to inform you that your Yummy Tummy Order <span class='highlight'>#").append(order.getId()).append("</span> has been rejected.</p>")
                   .append("            <p><strong>Order Details:</strong></p>")
                   .append("            <table>")
                   .append("                <thead>")
                   .append("                    <tr>")
                   .append("                        <th>Item</th>")
                   .append("                        <th>Quantity</th>")
                   .append("                        <th>Price</th>")
                   .append("                        <th>Total</th>")
                   .append("                    </tr>")
                   .append("                </thead>")
                   .append("                <tbody>");

        for (OrderItem item : order.getOrderItems()) {
            MenuItem menuItem = item.getMenuItem();
            htmlBuilder.append("                    <tr>")
                       .append("                        <td>").append(menuItem != null ? menuItem.getName() : "N/A").append("</td>")
                       .append("                        <td>").append(item.getQuantity()).append("</td>")
                       .append("                        <td>").append(CURRENCY_FORMAT.format(item.getPrice())).append("</td>")
                       .append("                        <td>").append(CURRENCY_FORMAT.format(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))).append("</td>")
                       .append("                    </tr>");
        }

        htmlBuilder.append("                </tbody>")
                   .append("            </table>")
                   .append("            <p><strong>Payment Method:</strong> ").append(order.getPaymentMethod().name().replace("_", " ")).append("</p>")
                   .append("            <p><strong>Total Payable:</strong> ").append(CURRENCY_FORMAT.format(order.getTotalPrice())).append("</p>");

        // Conditional refund message
        if (order.getPaymentMethod() == Order.PaymentMethod.CARD || order.getPaymentMethod() == Order.PaymentMethod.ONLINE_PAYMENT) {
            htmlBuilder.append("            <p>Your refund has been initiated and will be reflected in your original payment method within 5–7 business days.</p>");
        }
        
        htmlBuilder.append("            <p>We apologize for any inconvenience this may cause. Please contact our support team if you have any questions.</p>")
                   .append("            <p>Thank you for choosing Yummy Tummy!</p>")
                   .append("        </div>")
                   .append("        <div class='footer'>")
                   .append("            <p>&copy; ").append(LocalDateTime.now().getYear()).append(" Yummy Tummy. All rights reserved.</p>")
                   .append("        </div>")
                   .append("    </div>")
                   .append("</body>")
                   .append("</html>");

        return htmlBuilder.toString();
    }

    private String buildOrderCancellationHtml(Order order, String customerName) {
        StringBuilder htmlBuilder = new StringBuilder();
        htmlBuilder.append("<!DOCTYPE html>")
                   .append("<html lang='en'>")
                   .append("<head>")
                   .append("    <meta charset='UTF-8'>")
                   .append("    <meta name='viewport' content='width=device-width, initial-scale=1.0'>")
                   .append("    <title>Yummy Tummy Order Cancelled</title>")
                   .append("    <style>")
                   .append("        body { font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; margin: 0; padding: 0; background-color: #f4f4f4; color: #333; }")
                   .append("        table { width: 100%; border-collapse: collapse; }")
                   .append("        td, th { padding: 8px; text-align: left; border-bottom: 1px solid #ddd; }")
                   .append("        th { background-color: #f8f8f8; }")
                   .append("        .container { max-width: 600px; margin: 20px auto; background-color: #ffffff; padding: 20px; border-radius: 8px; box-shadow: 0 0 10px rgba(0, 0, 0, 0.1); }")
                   .append("        .header { background-color: #eb4d4b; color: #ffffff; padding: 15px 20px; border-radius: 8px 8px 0 0; text-align: center; }")
                   .append("        .header h1 { margin: 0; font-size: 24px; }")
                   .append("        .content { padding: 20px; }")
                   .append("        .footer { text-align: center; padding: 20px; font-size: 12px; color: #777; }")
                   .append("        .button { display: inline-block; background-color: #f0932b; color: #ffffff; padding: 10px 20px; text-decoration: none; border-radius: 5px; margin-top: 20px; }")
                   .append("        .highlight { color: #eb4d4b; font-weight: bold; }")
                   .append("    </style>")
                   .append("</head>")
                   .append("<body>")
                   .append("    <div class='container'>")
                   .append("        <div class='header'>")
                   .append("            <h1>Yummy Tummy Order Cancelled</h1>")
                   .append("        </div>")
                   .append("        <div class='content'>")
                   .append("            <p>Hi ").append(customerName).append(",</p>")
                   .append("            <p>Your Yummy Tummy Order <span class='highlight'>#").append(order.getId()).append("</span> has been successfully cancelled.</p>")
                   .append("            <p><strong>Order Details:</strong></p>")
                   .append("            <table>")
                   .append("                <thead>")
                   .append("                    <tr>")
                   .append("                        <th>Item</th>")
                   .append("                        <th>Quantity</th>")
                   .append("                        <th>Price</th>")
                   .append("                        <th>Total</th>")
                   .append("                    </tr>")
                   .append("                </thead>")
                   .append("                <tbody>");

        for (OrderItem item : order.getOrderItems()) {
            MenuItem menuItem = item.getMenuItem();
            htmlBuilder.append("                    <tr>")
                       .append("                        <td>").append(menuItem != null ? menuItem.getName() : "N/A").append("</td>")
                       .append("                        <td>").append(item.getQuantity()).append("</td>")
                       .append("                        <td>").append(CURRENCY_FORMAT.format(item.getPrice())).append("</td>")
                       .append("                        <td>").append(CURRENCY_FORMAT.format(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))).append("</td>")
                       .append("                    </tr>");
        }

        htmlBuilder.append("                </tbody>")
                   .append("            </table>")
                   .append("            <p><strong>Payment Method:</strong> ").append(order.getPaymentMethod().name().replace("_", " ")).append("</p>")
                   .append("            <p><strong>Total Payable:</strong> ").append(CURRENCY_FORMAT.format(order.getTotalPrice())).append("</p>");

        // Conditional refund message
        if (order.getPaymentMethod() == Order.PaymentMethod.CARD || order.getPaymentMethod() == Order.PaymentMethod.ONLINE_PAYMENT) {
            htmlBuilder.append("            <p>Your refund has been initiated and will be reflected in your original payment method within 5–7 business days.</p>");
        }
        
        htmlBuilder.append("            <p>We apologize for any inconvenience this may cause. Please contact our support team if you have any questions.</p>")
                   .append("            <p>Thank you for choosing Yummy Tummy!</p>")
                   .append("        </div>")
                   .append("        <div class='footer'>")
                   .append("            <p>&copy; ").append(LocalDateTime.now().getYear()).append(" Yummy Tummy. All rights reserved.</p>")
                   .append("        </div>")
                   .append("    </div>")
                   .append("</body>")
                   .append("</html>");

        return htmlBuilder.toString();
    }
}