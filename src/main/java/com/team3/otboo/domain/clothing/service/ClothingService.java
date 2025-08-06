package com.team3.otboo.domain.clothing.service;

import com.team3.otboo.domain.clothing.dto.ClothingDto;
import com.team3.otboo.domain.clothing.dto.request.ClothingCreateRequest;
import com.team3.otboo.domain.clothing.dto.request.ClothingUpdateRequest;
import com.team3.otboo.domain.clothing.dto.response.ClothingDtoCursorResponse;
import com.team3.otboo.domain.clothing.entity.Clothing;
import com.team3.otboo.domain.user.entity.User;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.web.multipart.MultipartFile;

public interface ClothingService {

  List<Clothing> getClothesByOwner(User user);

  ClothingDto registerClothing(User user, ClothingCreateRequest request, MultipartFile image);

  ClothingDtoCursorResponse getClothesByCursor(
          UUID ownerId,
          String cursor,
          UUID idAfter,
          int limit,
          String typeEqual,
          Sort.Direction direction
  );

  void deleteClothing(UUID clothesId);

  ClothingDto updateClothing(UUID id, ClothingUpdateRequest req, MultipartFile image);
}
