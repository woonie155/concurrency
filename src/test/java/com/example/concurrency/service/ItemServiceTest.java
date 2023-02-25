package com.example.concurrency.service;

import com.example.concurrency.domain.Item;
import com.example.concurrency.facade.LettuceLockItemFacade;
import com.example.concurrency.facade.NamedLockItemFacade;
import com.example.concurrency.facade.OptimisticLockItemFacade;
import com.example.concurrency.facade.RedissonLockItemFacade;
import com.example.concurrency.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
class ItemServiceTest {


    @Autowired private ItemService itemService;
    @Autowired private ItemRepository itemRepository;
    @Autowired private PessimisticLockItemService pessimisticLockItemService;
    @Autowired private OptimisticLockItemFacade optimisticLockItemFacade;
    @Autowired private NamedLockItemFacade namedLockItemFacade;
    @Autowired private LettuceLockItemFacade lettuceLockItemFacade;
    @Autowired private RedissonLockItemFacade redissonLockItemFacade;


    @BeforeEach
    public void before(){
        Item item = Item.builder().productId(1L).quantity(100L).build();
        itemRepository.save(item);
    }

    @Test
    public void item_decrease(){
        //given
        itemService.decrease(1L, 1L);

        //when
        Item item = itemRepository.findById(1L).orElseThrow();

        //then
        assertEquals(99, item.getQuantity());
    }

    /**
     * App- 경쟁조건 발생 코드 (race condition)
     *
     * [문제]
     * @Transaction (MySQL: repeatable read)
     * 100개 요청을 멀티 스레드로 요청시 문제점: 많은 스레드가 같은 값을 읽게 된 후에 decrease() 호출하므로 최종 결과는 0이되지 않는다.
     *
     * [해결책]
     * -> Service 메소드에 @Transaction + Synchronized 처리해서 해결한다.
     *    발생문제 1. Transaction 과정은 AOP 프록시이므로, synchronized 는 실제 타겟에만 적용된다.
     *              AOP after(커밋)처리가 완료되기 전에, 다른 세션에서 동일한 값으로 읽게된다.
     *
     * -> @Transaction 빼고, Synchronized 로만 처리해 해결한다.
     *    발생문제 1. 하지만, 서버 스케일 아웃에 의한 경쟁조건은 피할 수 없다. synchronized 는 java API 이므로 해당 서버에서의 요청만 블락시키기 때문이다.
     */
    @Test
    public void APP_Concurrency_Synchronized_Test() throws InterruptedException {
        int threadCount = 100; //100개 요청
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for(int i=0; i<threadCount; i++){
            executorService.submit(()->{ //비동기 처리 API
                try {
                    itemService.decrease(1L,1L);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await(); //threadCount 개수 쓰레드가 처리하기까지 대기
        Item item = itemRepository.findById(1L).orElseThrow();
        assertEquals(0L, item.getQuantity());
    }

    /**
     * DB- 경쟁조건 발생 코드 (race condition)
     * 비관적 락 사용하여 해결
     */
    @Test
    public void DB_Concurrency_PessimisticLock_Test() throws InterruptedException {
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for(int i=0; i<threadCount; i++){
            executorService.submit(()->{ //비동기 처리 API
                try {
                    pessimisticLockItemService.decrease(1L,1L);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await(); //threadCount 개수 쓰레드가 처리하기까지 대기
        Item item = itemRepository.findById(1L).orElseThrow();
        assertEquals(0L, item.getQuantity());
    }


    /**
     * DB- 경쟁조건 발생 코드 (race condition)
     * 낙관적 락 사용하여 해결
     *
     * - 낙관적 락의 경우 업데이트와 재시도로직을 동일한 트랜잭션내 존재하게 되면
     *  MySQL Repeatable read 격리수준에의해 무한루프 발생하니 주의해야한다.
     */
    @Test
    public void DB_Concurrency_OptimisticLock_Test() throws InterruptedException {
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for(int i=0; i<threadCount; i++){
            executorService.submit(()->{ //비동기 처리 API
                try {
                    optimisticLockItemFacade.decrease(1L,1L);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await(); //threadCount 개수 쓰레드가 처리하기까지 대기
        Item item = itemRepository.findById(1L).orElseThrow();
        assertEquals(0L, item.getQuantity());
    }

    /**
     * DB- 경쟁조건 발생 코드 (race condition)
     * named lock 사용하여 해결
     */
    @Test
    public void DB_Concurrency_NamedLock_Test() throws InterruptedException {
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for(int i=0; i<threadCount; i++){
            executorService.submit(()->{ //비동기 처리 API
                try {
                    namedLockItemFacade.decrease(1L,1L);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await(); //threadCount 개수 쓰레드가 처리하기까지 대기
        Item item = itemRepository.findById(1L).orElseThrow();
        assertEquals(0L, item.getQuantity());
    }

    /**
     * redis- 경쟁조건 발생 코드 (race condition)
     * lettuce (Spin Lock)
     */
    @Test
    public void Redis_Concurrency_lettuce_Test() throws InterruptedException {
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for(int i=0; i<threadCount; i++){
            executorService.submit(()->{ //비동기 처리 API
                try {
                    lettuceLockItemFacade.decrease(1L,1L);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await(); //threadCount 개수 쓰레드가 처리하기까지 대기
        Item item = itemRepository.findById(1L).orElseThrow();
        assertEquals(0L, item.getQuantity());
    }

    /**
     * redis- 경쟁조건 발생 코드 (race condition)
     *  redisson (pub-sub)
     */
    @Test
    public void Redis_Concurrency_redisson_Test() throws InterruptedException {
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for(int i=0; i<threadCount; i++){
            executorService.submit(()->{ //비동기 처리 API
                try {
                    redissonLockItemFacade.decrease(1L,1L);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await(); //threadCount 개수 쓰레드가 처리하기까지 대기
        Item item = itemRepository.findById(1L).orElseThrow();
        assertEquals(0L, item.getQuantity());
    }
}