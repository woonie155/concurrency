package com.example.concurrency.facade;

import com.example.concurrency.service.RedisLockItemService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedissonLockItemFacade {

    private final RedissonClient redissonClient;
    private final RedisLockItemService redisLockItemService;

    public void decrease(Long key, Long quantity){
        RLock lock = redissonClient.getLock(key.toString());
        try {
            boolean available = lock.tryLock(5, 1, TimeUnit.SECONDS); //획득시도시간, 락점유시간, 단위
            if(!available){
                System.out.println("lock 획득 실패");
                return;
            }
            redisLockItemService.decrease(key, quantity);
        } catch (InterruptedException e){
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }

}
