package com.team3.otboo.domain.clothing.service;

import com.team3.otboo.domain.clothing.entity.Clothing;
import com.team3.otboo.domain.user.entity.User;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ClothingServiceImpl implements ClothingService{

  @Override
  public List<Clothing> getClothesByOwner(User user) {
    return List.of();
  }
}
