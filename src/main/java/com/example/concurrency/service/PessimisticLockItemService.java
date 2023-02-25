package com.example.concurrency.service;

import com.example.concurrency.domain.Item;
import com.example.concurrency.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PessimisticLockItemService {

    private final ItemRepository itemRepository;

    @Transactional
    public void decrease(Long id, Long quantity) throws InterruptedException {
        Item item = itemRepository.findByIdWithPessimisticLock(id);
        item.decrease(quantity);
        Thread.sleep(7000);
    }
}
