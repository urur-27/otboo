package com.team3.otboo.fixture;

import com.team3.otboo.storage.entity.BinaryContent;
import com.team3.otboo.storage.entity.BinaryContentUploadStatus;
import java.util.UUID;

public final class BinaryContentFixture {
    private BinaryContentFixture() {}

    public static BinaryContent successWithUrl() throws Exception {
        UUID id = UUID.randomUUID();
        String url = "http://cdn.example.com/" + id;
        BinaryContent bin = new BinaryContent("file.jpg", 3L, "image/jpeg", BinaryContentUploadStatus.SUCCESS);
        setId(bin, id);
        bin.updateImageUrl(url);
        return bin;
    }

    public static void setId(Object entity, UUID id) throws Exception {
        var idField = entity.getClass().getSuperclass().getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(entity, id);
    }
}

