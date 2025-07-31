package com.team3.otboo.domain.follow.service;

import com.team3.otboo.domain.follow.dto.FollowDto;
import com.team3.otboo.domain.follow.dto.FollowSummaryDto;
import com.team3.otboo.domain.follow.entity.Follow;
import com.team3.otboo.domain.follow.entity.UserFollowerCount;
import com.team3.otboo.domain.follow.entity.UserFollowingCount;
import com.team3.otboo.domain.follow.mapper.FollowMapper;
import com.team3.otboo.domain.follow.repository.FollowRepository;
import com.team3.otboo.domain.follow.repository.UserFollowerCountRepository;
import com.team3.otboo.domain.follow.repository.UserFollowingCountRepository;
import com.team3.otboo.domain.follow.service.request.FollowCreateRequest;
import com.team3.otboo.domain.follow.service.response.FollowListResponse;
import com.team3.otboo.domain.user.dto.UserSummary;
import com.team3.otboo.domain.user.entity.User;
import com.team3.otboo.domain.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.hibernate.query.SortDirection;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FollowService {

	private final UserRepository userRepository;
	private final FollowRepository followRepository;
	private final FollowMapper followMapper;

	private final UserFollowerCountRepository userFollowerCountRepository;
	private final UserFollowingCountRepository userFollowingCountRepository;

	@Transactional
	public FollowDto create(FollowCreateRequest request) {
		User follower = userRepository.findById(request.followerId()).orElseThrow(
			() -> new EntityNotFoundException("해당 user가 존재하지 않습니다.")
		);
		User followee = userRepository.findById(request.followeeId()).orElseThrow(
			() -> new EntityNotFoundException("해당 user가 존재하지 않습니다.")
		);

		// unique index 를 만들어서 동시성 문제 해결 .
		if (followRepository.existsByFollowerIdAndFolloweeId(request.followerId(),
			request.followeeId())) {
			throw new IllegalArgumentException("같은 사람을 두번 팔로우 할 수 없습니다.");
		}

		Follow follow = followRepository.save(
			Follow.create(request.followeeId(), request.followerId()));

		// follower 의 following 카운트 증가 + followee 의 follower 카운트 증가 .
		int followingResult = userFollowingCountRepository.increase(follower.getId());
		if (followingResult == 0) { // 만약에 해당 row 가 없으면 0이 반환됨 . 없으면 객체 만들어서 save
			userFollowingCountRepository.save(
				UserFollowingCount.init(follower.getId(), 1L)
			);
		}

		int followerCount = userFollowerCountRepository.increase(followee.getId());
		if (followerCount == 0) {
			userFollowerCountRepository.save(
				UserFollowerCount.init(followee.getId(), 1L)
			);
		}

		return new FollowDto(
			follow.getId(),
			UserSummary.from(followee),
			UserSummary.from(follower)
		);
	}

	@Transactional
	public void delete(UUID followId) {
		Follow follow = followRepository.findById(followId).orElseThrow(
			() -> new EntityNotFoundException("해당 follow가 존재하지 않습니다.")
		);

		followRepository.deleteById(followId);

		userFollowerCountRepository.decrease(follow.getFolloweeId());
		userFollowingCountRepository.decrease(follow.getFollowerId());
	}

	// 팔로우 하는 사람의 ID 를 주면 follow 하고 있는 사람들의 목록을 준다.
	@Transactional(readOnly = true)
	public FollowListResponse getFollowings(UUID followerId, String cursor, UUID idAfter,
		Integer limit, String nameLike) {

		Long count = userFollowingCountRepository.findById(followerId)
			.map(UserFollowingCount::getFollowingCount)
			.orElse(0L);

		int followingCount = count.intValue();

		// has next 의 판단을 위해 limit + 1 개를 가져옴 .
		// cursor -> Instant, idAfter -> 같은 시간에 만들어졌을 경우 id 로 정렬 ..
		// TODO 인덱스 만들기 .

		Instant lastCreatedAt = null;
		if (cursor != null) {
			lastCreatedAt = Instant.parse(cursor);
		}

		List<Follow> follows = cursor == null || idAfter == null ?
			followRepository.getFollowings(followerId, limit + 1, nameLike) :
			followRepository.getFollowings(followerId, lastCreatedAt, idAfter, limit + 1, nameLike);

		boolean hasNext = follows.size() > limit;
		List<Follow> currentPage = hasNext ? follows.subList(0, limit) : follows;

		List<FollowDto> followDtoList = currentPage.stream()
			.map(followMapper::toDto)
			.toList();

		String nextCursor = null; // 마지막 요소의 createdAt
		UUID nextIdAfter = null; // lastFollowId

		if (hasNext && !follows.isEmpty()) {
			Follow lastElement = follows.get(limit - 1); // 현재 페이지의 마지막 요소
			nextCursor = lastElement.getCreatedAt().toString();
			nextIdAfter = lastElement.getId();
		}

		return new FollowListResponse(followDtoList, nextCursor, nextIdAfter, hasNext,
			followingCount, "createdAt, id", SortDirection.DESCENDING);
	}

	@Transactional(readOnly = true)
	public FollowListResponse getFollowers(UUID followeeId, String cursor,
		UUID idAfter, int limit, String nameLike) {

		Long count = userFollowerCountRepository.findById(followeeId)
			.map(UserFollowerCount::getFollowerCount)
			.orElse(0L);

		int followerCount = count.intValue();

		Instant lastCreatedAt = null;
		if (cursor != null) {
			lastCreatedAt = Instant.parse(cursor);
		}

		List<Follow> follows = cursor == null || idAfter == null ?
			followRepository.getFollowers(followeeId, limit + 1, nameLike) :
			followRepository.getFollowers(followeeId, lastCreatedAt, idAfter, limit + 1, nameLike);

		boolean hasNext = follows.size() > limit;
		List<Follow> currentPage = hasNext ? follows.subList(0, limit) : follows;

		List<FollowDto> followDtoList = currentPage.stream()
			.map(followMapper::toDto)
			.toList();

		String nextCursor = null;
		UUID nextIdAfter = null;
		if (hasNext && !follows.isEmpty()) {
			Follow lastElement = follows.get(limit - 1);
			nextCursor = lastElement.getCreatedAt().toString();
			nextIdAfter = lastElement.getId();
		}

		return new FollowListResponse(followDtoList, nextCursor, nextIdAfter, hasNext,
			followerCount, "createdAt, id", SortDirection.DESCENDING);
	}

	// CustomUserDetail 구현 한 후 구현하기 .
	public FollowSummaryDto getFollowSummary(UUID userId, UUID currentUserId) {

		return null;
	}
}
