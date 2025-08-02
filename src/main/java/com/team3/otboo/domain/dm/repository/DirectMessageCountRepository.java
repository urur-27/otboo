package com.team3.otboo.domain.dm.repository;

import com.team3.otboo.domain.dm.entity.DirectMessageCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DirectMessageCountRepository extends JpaRepository<DirectMessageCount, String> {

	@Query(
		value = "update direct_message_count set direct_message_count = direct_message_count + 1 where dm_key = :dmKey ",
		nativeQuery = true
	)
	@Modifying
	int increase(@Param("dmKey") String dmKey);

	@Query(
		value = "update direct_message_count set direct_message_count = direct_message_count - 1 where dm_key = :dmKey",
		nativeQuery = true
	)
	@Modifying
	int decrease(@Param("dmKey") String dmKey);
}
