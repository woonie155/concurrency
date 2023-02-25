package com.example.concurrency.service;

import com.example.concurrency.domain.Item;
import com.example.concurrency.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OptimisticLockItemService {

    private final ItemRepository itemRepository;

    @Transactional
    public void decrease(Long id, Long quantity){
        Item item = itemRepository.findByIdWithOptimisticLock(id);
        item.decrease(quantity);
    }
}
