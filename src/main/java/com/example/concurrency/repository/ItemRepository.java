package com.example.concurrency.repository;

import com.example.concurrency.domain.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.persistence.LockModeType;

public interface ItemRepository extends JpaRepository<Item, Long> {

    @Lock(value = LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Item i where i.id =:id")
    Item findByIdWithPessimisticLock(@Param("id") Long id); //비관적

    @Lock(value = LockModeType.OPTIMISTIC)
    @Query("select i from Item i where i.id =:id")
    Item findByIdWithOptimisticLock(@Param("id") Long id); //낙관적
}
