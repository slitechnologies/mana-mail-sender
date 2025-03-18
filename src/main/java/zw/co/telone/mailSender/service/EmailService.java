package zw.co.telone.mailSender.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import zw.co.telone.mailSender.repository.EmailRepository;
import zw.co.telone.mailSender.exception.EmailSendingException;
import zw.co.telone.mailSender.model.EmailDetails;


import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings({"unused"})
public class EmailService {
    private final JavaMailSender emailSender;
    private final EmailRepository emailRepository;

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public EmailDetails sendEmail(EmailDetails emailDetails) {
        try {

            EmailDetails managedEmailDetails = prepareEmailDetails(emailDetails);


            return sendEmailWithRetry(managedEmailDetails);

        } catch (ObjectOptimisticLockingFailureException e) {
            log.warn("Concurrent modification detected. Retrying email send.", e);
            throw new EmailSendingException("Concurrent email modification", e);
        } catch (Exception e) {
            log.error("Unexpected error in email sending process", e);
            throw new EmailSendingException("Email sending failed", e);
        }
    }

    private EmailDetails prepareEmailDetails(EmailDetails emailDetails) {

        if (emailDetails.getId() == null || emailDetails.getId() == 0) {
            EmailDetails newEmailDetails = new EmailDetails();
            newEmailDetails.setRecipient(emailDetails.getRecipient());
            newEmailDetails.setSubject(emailDetails.getSubject());
            newEmailDetails.setMessageBody(emailDetails.getMessageBody());
            newEmailDetails.setAttachment(emailDetails.getAttachment());

            newEmailDetails.setCreatedAt(LocalDateTime.now());
            newEmailDetails.setStatus(EmailDetails.EmailStatus.PENDING);
            newEmailDetails.setAttempts(0);


            return emailRepository.save(newEmailDetails);
        }

        return emailRepository.findById(emailDetails.getId())
                .orElseThrow(() -> new EmailSendingException("Email not found with ID: " + emailDetails.getId()));
    }

    private EmailDetails sendEmailWithRetry(EmailDetails emailDetails) {
        try {
            emailDetails.setStatus(EmailDetails.EmailStatus.SENDING);
            emailDetails.setLastAttemptAt(LocalDateTime.now());
            emailDetails.setAttempts(emailDetails.getAttempts() + 1);
            emailDetails = emailRepository.save(emailDetails);


            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(senderEmail);
            message.setTo(emailDetails.getRecipient());
            message.setSubject(emailDetails.getSubject());
            message.setText(emailDetails.getMessageBody());


            emailSender.send(message);


            emailDetails.setStatus(EmailDetails.EmailStatus.SENT);
            emailDetails = emailRepository.save(emailDetails);

            log.info("Email sent successfully to {}", emailDetails.getRecipient());
            return emailDetails;

        } catch (MailException e) {

            return handleEmailSendingException(emailDetails, e);
        }
    }

//    private EmailDetails handleEmailSendingException(EmailDetails emailDetails, MailException e) {
//        log.error("Failed to send email to {}: {}", emailDetails.getRecipient(), e.getMessage());
//
//
//        if (isTemporaryFailure(e) && emailDetails.getAttempts() < 5) {
//            emailDetails.setStatus(EmailDetails.EmailStatus.PENDING);
//        } else {
//            emailDetails.setStatus(EmailDetails.EmailStatus.FAILED);
//        }
//
//        return emailRepository.save(emailDetails);
//    }

    private EmailDetails handleEmailSendingException(EmailDetails emailDetails, MailException e) {
        log.error("Failed to send email to {}: {}", emailDetails.getRecipient(), e.getMessage());

        // Always keep the status as PENDING to allow continuous retry
        emailDetails.setStatus(EmailDetails.EmailStatus.PENDING);

        // Log the specific failure reason
        log.warn("Email sending failed. Recipient: {}, Attempt: {}, Reason: {}",
                emailDetails.getRecipient(),
                emailDetails.getAttempts(),
                e.getMessage());

        return emailRepository.save(emailDetails);
    }

    private boolean isTemporaryFailure(MailException e) {
        String errorMessage = e.getMessage().toLowerCase();
        return errorMessage.contains("timeout") ||
                errorMessage.contains("connection") ||
                errorMessage.contains("server") ||
                errorMessage.contains("temporary");
    }



    @Scheduled(fixedDelay = 20000) // Run every 5 minutes
    @Transactional
    public void retryFailedEmails() {
        List<EmailDetails> emailsToRetry = emailRepository.findEmailsToRetry();

        log.info("Retrying {} emails", emailsToRetry.size());

        for (EmailDetails email : emailsToRetry) {
            try {
                // If email is already sent, skip
                if (email.getStatus() == EmailDetails.EmailStatus.SENT) {
                    continue;
                }

                // Send email
                EmailDetails updatedEmail = sendEmail(email);

                // If email sending failed, log the attempt
                if (updatedEmail.getStatus() != EmailDetails.EmailStatus.SENT) {
                    log.warn("Email retry failed for email ID {}, attempts: {}",
                            email.getId(),
                            updatedEmail.getAttempts());
                }
            } catch (Exception e) {
                log.error("Error during email retry for email ID {}", email.getId(), e);
            }
        }


//    @Scheduled(fixedDelay = 300000)
//    @Transactional
//    public void retryFailedEmails() {
//        List<EmailDetails> emailsToRetry = emailRepository.findEmailsToRetry();
//
//        log.info("Retrying {} emails", emailsToRetry.size());
//
//        for (EmailDetails email : emailsToRetry) {
//            try {
//
//                if (email.getAttempts() >= 5) {
//                    email.setStatus(EmailDetails.EmailStatus.FAILED);
//                    emailRepository.save(email);
//                    continue;
//                }
//
//
//                sendEmail(email);
//            } catch (Exception e) {
//                log.error("Error during email retry for email ID {}", email.getId(), e);
//            }
//        }
    }
}
//    public void sendEmail(EmailDetails emailDetails) {
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setFrom(senderEmail);
//        message.setTo(emailDetails.getRecipient());
//        message.setSubject(emailDetails.getSubject());
//        message.setText(emailDetails.getMessageBody());
//
//        emailSender.send(message);
//    }
//}
