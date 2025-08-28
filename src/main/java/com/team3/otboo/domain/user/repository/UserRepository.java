package com.team3.otboo.domain.user.repository;

import com.team3.otboo.domain.user.entity.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, UUID>, UserRepositoryCustom {

	boolean existsByEmail(String email);

	boolean existsByUsername(String username);

	Optional<User> findByUsername(String username);

	Optional<User> findByEmail(String email);

	/**
	 * 주어진 ID 목록에 해당하는 모든 사용자를 조회합니다.
	 *
	 * @param ids 조회할 사용자 ID 목록
	 * @return 조회된 사용자 엔티티 목록
	 */
	@Query(
		value = "SELECT * FROM users WHERE id IN (:ids)",
		nativeQuery = true)
	List<User> findAllByIdIn(@Param("ids") List<UUID> ids);

}
