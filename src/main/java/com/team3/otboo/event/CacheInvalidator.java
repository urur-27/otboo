package com.team3.otboo.event;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class CacheInvalidator {

    @Caching(evict = {
            @CacheEvict(cacheNames = "clothingById", key = "#e.clothingId()"),
            @CacheEvict(cacheNames = "clothingList", allEntries = true) // 목록 전부 비움(짧은 TTL이라 큰 부담 X)
    })
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onClothingChanged(ClothingChangedEvent e) {}

    @Caching(evict = {
            @CacheEvict(cacheNames = "attrSnapshot", key = "'v1'"),
            @CacheEvict(cacheNames = "clothingList", allEntries = true),
            @CacheEvict(cacheNames = "attrDefsPage", allEntries = true)
    })
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAttributeSchemaChanged(AttributeSchemaChangedEvent e) {}
}
