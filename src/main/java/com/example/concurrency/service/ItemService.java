package com.example.concurrency.service;

import com.example.concurrency.domain.Item;
import com.example.concurrency.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
//@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

//    @Transactional
    public synchronized void decrease(Long id, Long quantity){
        Item item = itemRepository.findById(id).orElseThrow();
        item.decrease(quantity);
    }

}
