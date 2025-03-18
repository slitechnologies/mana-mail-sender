package zw.co.telone.mailSender.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import zw.co.telone.mailSender.model.EmailDetails;

import java.util.List;

@Repository
public interface EmailRepository extends JpaRepository<EmailDetails, Long> {
    // Find emails that are pending or have failed and need retry
//    @Query("SELECT e FROM EmailDetails e WHERE e.status IN (zw.co.telone.mailSender.model.EmailDetails.EmailStatus.PENDING, zw.co.telone.mailSender.model.EmailDetails.EmailStatus.FAILED) AND e.attempts < 5")
//    @Query("SELECT e FROM EmailDetails e WHERE e.status = 'PENDING' OR e.status = 'FAILED' AND e.attempts < 5")
    @Query("SELECT e FROM EmailDetails e WHERE e.status = 'PENDING' OR e.status = 'FAILED'")
    List<EmailDetails> findEmailsToRetry();
}