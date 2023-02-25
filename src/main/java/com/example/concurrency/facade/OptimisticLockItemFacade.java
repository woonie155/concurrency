package com.example.concurrency.facade;

import com.example.concurrency.service.OptimisticLockItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OptimisticLockItemFacade {

    private final OptimisticLockItemService optimisticLockItemService;

    public void decrease(Long id, Long quantity) throws InterruptedException {
        while (true){
            try {
                optimisticLockItemService.decrease(id, quantity);
                break;
            }catch (Exception e){
                Thread.sleep(50);
            }
        }
    }
}
