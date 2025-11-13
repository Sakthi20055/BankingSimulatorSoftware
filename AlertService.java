package com.bank.simulator.service;

import com.bank.simulator.model.Account;

import java.util.Properties;
import java.util.Random;
import javax.mail.*;
import javax.mail.internet.*;

public class AlertService {

    private static final String FROM_EMAIL = "sakthipanimalarit@gmail.com";
    private static final String FROM_PASSWORD = "duih ihun sqfq amgd";

    // 🔔 Send low balance alert
    public void sendLowBalanceAlert(Account account) {
        double balance = account.getBalance();

        // Console alert
        System.out.println("⚠️ ALERT: Low Balance for Account ID " + account.getAccountId());
        System.out.println("📉 Current Balance: ₹" + balance);
        System.out.println("An alert has been sent to: " + account.getEmail());

        // Email alert
        String subject = "⚠️ Low Balance Alert - Banking Simulator";
        String body = "Dear " + account.getOwnerName() + ",\n\n"
                + "Your account (ID: " + account.getAccountId() + ") has a low balance of ₹" + balance + ".\n"
                + "Please deposit funds soon to avoid service interruptions.\n\n"
                + "Thank you,\nBanking Simulator Team";

        sendEmail(account.getEmail(), subject, body);
    }

    // 🔑 Generate OTP and send via email
    public String sendOtp(Account account) {
        // Generate a random 6-digit OTP
        Random rand = new Random();
        String otp = String.format("%06d", rand.nextInt(1000000));

        // Email content
        String subject = "🔒 OTP for Withdrawal - Banking Simulator";
        String body = "Dear " + account.getOwnerName() + ",\n\n"
                + "Your OTP for withdrawal is: " + otp + "\n"
                + "Do not share this OTP with anyone.\n\n"
                + "Banking Simulator Team";

        // Send email
        sendEmail(account.getEmail(), subject, body);

        // Return OTP to be verified by TransactionProcessor
        return otp;
    }

    // ✉️ Send email helper
    private void sendEmail(String toEmail, String subject, String body) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, FROM_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            message.setText(body);
            Transport.send(message);

            System.out.println("📧 Email sent successfully to " + toEmail);

        } catch (MessagingException e) {
            System.err.println("❌ Failed to send email: " + e.getMessage());
        }
    }
}
