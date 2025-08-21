package com.team3.otboo.event;

import com.team3.otboo.domain.clothing.repository.ClothingRepository;
import com.team3.otboo.storage.ImageStorage;
import com.team3.otboo.storage.dto.ImageMaybeOrphanedEvent;
import com.team3.otboo.storage.repository.BinaryContentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
class ImageCleanupListener {
    private final ClothingRepository clothingRepository;
    private final ImageStorage imageStorage;
    private final BinaryContentRepository binaryContentRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(ImageMaybeOrphanedEvent e) {
        // 혹시 다른 의상이 같은 이미지를 참조하고 있으면 지우지 않음
        long refs = clothingRepository.countByImageId(e.imageId());
        if (refs == 0) {
            imageStorage.delete(e.imageId());      // S3에서 객체 삭제
            binaryContentRepository.deleteById(e.imageId());
        }
    }
}