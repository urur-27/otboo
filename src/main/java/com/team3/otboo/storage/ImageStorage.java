package com.team3.otboo.storage;


import java.io.InputStream;
import java.util.UUID;

public interface ImageStorage {
    void delete(UUID id);

    UUID put(UUID binaryContentId, byte[] bytes);

    InputStream get(UUID binaryContentId);

    String getPatch(UUID binaryContentId, String contentType);

}