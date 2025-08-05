package com.team3.otboo.domain.feed.service;

import com.team3.otboo.domain.clothing.entity.Clothing;
import com.team3.otboo.domain.clothing.repository.ClothingRepository;
import com.team3.otboo.domain.feed.dto.OotdDto;
import com.team3.otboo.domain.feed.entity.Ootd;
import com.team3.otboo.domain.feed.mapper.OotdDtoAssembler;
import com.team3.otboo.domain.feed.repository.OotdRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OotdService {

	private final OotdRepository ootdRepository;

	private final ClothingRepository clothingRepository;
	private final OotdDtoAssembler ootdDtoAssembler;

	@Transactional
	public void create(UUID feedId, List<UUID> clothesIds) {

		List<Clothing> clothesList = clothingRepository.findAllById(clothesIds);

		List<Ootd> ootds = clothesList.stream()
			.map(Clothing::getId)
			.map(clothesId -> Ootd.create(feedId, clothesId))
			.toList();

		ootdRepository.saveAll(ootds);
	}

	@Transactional(readOnly = true)
	public List<OotdDto> getOotdDtos(UUID feedId) {
		// feedId 로 해당하는 clothes 전부 찾고, ootdDto 로 만들어서 반환해주기 (feedAssembler 에서 사용)
		List<UUID> clothesIds = ootdRepository.findClothesIdsByFeedId(feedId);
		List<Clothing> clothesList = clothingRepository.findAllById(clothesIds);

		return ootdDtoAssembler.assemble(clothesList);
	}

	@Transactional
	public void deleteAllByFeedId(UUID feedId) {
		// 해당 feedId 와 연관된 모든 ootd 삭제 요청 .
		ootdRepository.deleteAllByFeedId(feedId);
	}
}
