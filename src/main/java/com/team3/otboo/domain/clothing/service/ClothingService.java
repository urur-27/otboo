package com.team3.otboo.domain.clothing.service;

import com.team3.otboo.domain.clothing.dto.ClothingDto;
import com.team3.otboo.domain.clothing.dto.request.ClothingCreateRequest;
import com.team3.otboo.domain.clothing.entity.Clothing;
import com.team3.otboo.domain.user.entity.User;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface ClothingService {

  List<Clothing> getClothesByOwner(User user);

  ClothingDto registerClothing(User user, ClothingCreateRequest request, MultipartFile image);
}
