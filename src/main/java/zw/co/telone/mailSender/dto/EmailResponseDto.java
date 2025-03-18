package zw.co.telone.mailSender.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import zw.co.telone.mailSender.model.EmailDetails;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmailResponseDto {

    private String recipient;

    private String subject;

    private String messageBody;

    private String attachment;

    private EmailDetails.EmailStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime lastAttemptAt;

    private Integer attempts;

}
