package com.example.concurrency.service;

import com.example.concurrency.domain.Item;
import com.example.concurrency.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NamedLockItemService {

    private final ItemRepository itemRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void decrease(Long id, Long quantity){
        Item item = itemRepository.findById(id).orElseThrow();
        item.decrease(quantity);
    }
}
