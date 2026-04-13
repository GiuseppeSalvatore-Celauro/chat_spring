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
    List<Message> findAllByOrderByTimestampDesc();
    List<Message> findByUserOrderByTimestampDesc(User user);
    Optional<Message> findById(long id);
    @Query(value = "select sleep(5);", nativeQuery = true)
    void sleep();

}
