package com.celauro.chat.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.celauro.chat.entity.Message;
import com.celauro.chat.entity.User;

public interface MessageRepository extends JpaRepository<Message, Long>{
    List<Message> findLimitMessagesByOrderByTimestampDesc(PageRequest pageable);
    List<Message> findMessageBySenderUsernameOrderByTimestampDesc(String username, PageRequest pageable);
    List<Message> findMessageByTextContainingIgnoreCaseOrderByTimestampDesc(String text, PageRequest pageable);
    List<Message> findMessageBySenderUsernameAndTextContainingIgnoreCaseOrderByTimestampDesc(String username, String text, PageRequest pageable);
    int countMessageBySenderUsername(String username);
    List<Message> findAllByOrderByTimestampDesc();
    List<Message> findBySenderOrderByTimestampDesc(User user);
    Optional<Message> findById(long id);

    // ========================
    // Custom query
    // ========================
    @Query(value = "select sleep(5);", nativeQuery = true)
    void sleep();

    @Query( value = """
    SELECT m FROM Message m
    WHERE
    (m.sender.username = :u1 AND m.receiver.username = :u2)
    OR
    (m.sender.username = :u2 AND m.receiver.username = :u1)
    ORDER BY m.timestamp DESC
    """)
    List<Message> findConversation(String u1, String u2);

}
