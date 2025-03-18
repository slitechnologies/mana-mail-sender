package zw.co.telone.mailSender.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import org.springframework.web.bind.annotation.RequestBody;
import zw.co.telone.mailSender.model.EmailDetails;
import zw.co.telone.mailSender.service.EmailService;

@RestController
@RequestMapping("api/v1")
public class EmailController {

    @Autowired
    private EmailService emailService;

//    @PostMapping("/send-email")
//    public String sendEmail(@RequestBody EmailDetails emailDetails) {
//        emailService.sendEmail(emailDetails);
//        return "Email sent to " + emailDetails.getRecipient();
//    }

    @PostMapping("/mail/send")
    public ResponseEntity<EmailDetails> sendEmail(@RequestBody EmailDetails emailDetails) {
        EmailDetails sentEmail = emailService.sendEmail(emailDetails);
        return ResponseEntity.ok(sentEmail);
    }
}
