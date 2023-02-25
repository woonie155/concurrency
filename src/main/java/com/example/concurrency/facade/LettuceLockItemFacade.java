package com.example.concurrency.facade;

import com.example.concurrency.repository.RedisLockRepository;
import com.example.concurrency.service.RedisLockItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LettuceLockItemFacade {

    private final RedisLockRepository redisLockRepository;
    private final RedisLockItemService redisLockItemService;

    public void decrease(Long key, Long quantity) throws InterruptedException {
        while(!redisLockRepository.lock(key)) {
            Thread.sleep(1000); //redis 부하 줄이기
        }

        try {
            redisLockItemService.decrease(key, quantity);
        }finally {
            redisLockRepository.unlock(key);
        }
    }
}
