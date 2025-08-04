package com.team3.otboo.storage;

import com.team3.otboo.storage.dto.BinaryContentCreateRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@Component
public class BinaryContentFactory {
    public Optional<BinaryContentCreateRequest> resolveProfileRequest(MultipartFile profileFile) {
        if (profileFile.isEmpty()) {
            return Optional.empty();
        } else {
            try {
                BinaryContentCreateRequest binaryContentCreateRequest = new BinaryContentCreateRequest(
                        profileFile.getOriginalFilename(),
                        profileFile.getContentType(),
                        profileFile.getBytes()
                );
                return Optional.of(binaryContentCreateRequest);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
