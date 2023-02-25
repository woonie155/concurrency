package com.example.concurrency.facade;

import com.example.concurrency.repository.DBNamedLockRepository;
import com.example.concurrency.service.NamedLockItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NamedLockItemFacade {

    private final DBNamedLockRepository dbNamedLockRepository;
    private final NamedLockItemService namedLockItemService;

    @Transactional
    public void decrease(Long id, Long quantity){
        try {
            dbNamedLockRepository.getLock(id.toString());
            namedLockItemService.decrease(id, quantity);
        } finally {
            dbNamedLockRepository.releaseLock(id.toString());
        }
    }

}
