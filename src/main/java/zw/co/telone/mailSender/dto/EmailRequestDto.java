package zw.co.telone.mailSender.dto;

import jakarta.validation.constraints.NotNull;
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
public class EmailRequestDto {

    @NotNull
    private String recipient;

    @NotNull
    private String subject;

    @NotNull
    private String messageBody;

    private String attachment;

    private EmailDetails.EmailStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime lastAttemptAt;

    private Integer attempts;
}
