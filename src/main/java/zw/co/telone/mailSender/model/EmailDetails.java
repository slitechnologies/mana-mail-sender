package zw.co.telone.mailSender.model;

import jakarta.persistence.*;
import lombok.*;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "email_details")
public class EmailDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

    @NotNull
    @Email
    @Column(nullable = false)
    private String recipient;

    @NotNull
    @Column(nullable = false)
    private String subject;

    @NotNull
    @Column(length = 500, nullable = false)
    private String messageBody;

    @Lob
    private String attachment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmailStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime lastAttemptAt;

    @Column
    private Integer attempts;

    // Add version for optimistic locking
    @Version
    @JsonIgnore // Ignore version in JSON to prevent serialization issues
    private Long version;

    // Enum to track email status
    public enum EmailStatus {
        PENDING,   // Initial state
        SENDING,   // Currently attempting to send
        SENT,      // Successfully sent
        FAILED     // Permanent failure
    }

    // Utility method to initialize default values
    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = EmailStatus.PENDING;
        }
        if (this.attempts == null) {
            this.attempts = 0;
        }
    }
}