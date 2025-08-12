package com.team3.otboo.domain.clothing.service;

import com.team3.otboo.domain.clothing.dto.ClothesDto;
import com.team3.otboo.domain.clothing.dto.request.ClothesCreateRequest;
import com.team3.otboo.domain.clothing.dto.request.ClothesUpdateRequest;
import com.team3.otboo.domain.clothing.dto.response.ClothingDtoCursorResponse;
import com.team3.otboo.domain.clothing.entity.Clothing;
import com.team3.otboo.domain.user.entity.User;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.web.multipart.MultipartFile;

public interface ClothingService {

  List<Clothing> getClothesByOwner(User user);

  ClothesDto registerClothing(User user, ClothesCreateRequest request, MultipartFile image);

  ClothingDtoCursorResponse getClothesByCursor(
          UUID ownerId,
          String cursor,
          UUID idAfter,
          int limit,
          String typeEqual,
          Sort.Direction direction
  );

  void deleteClothing(UUID clothesId);

  ClothesDto updateClothing(UUID id, ClothesUpdateRequest req, MultipartFile image);
}
