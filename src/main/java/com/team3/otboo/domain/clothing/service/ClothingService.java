package com.team3.otboo.domain.clothing.service;

import com.team3.otboo.domain.clothing.entity.Clothing;
import com.team3.otboo.domain.user.entity.User;
import java.util.List;

public interface ClothingService {

  List<Clothing> getClothesByOwner(User user);
}
