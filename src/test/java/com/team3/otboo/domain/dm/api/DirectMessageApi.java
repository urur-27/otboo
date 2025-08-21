package com.team3.otboo.domain.dm.api;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.team3.otboo.domain.dm.dto.DirectMessageDto;
import com.team3.otboo.domain.user.user_details.CustomUserDetails;
import java.util.List;
import java.util.UUID;
import lombok.Data;
import org.hibernate.query.SortDirection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.client.RestClient;

@SpringBootTest
@AutoConfigureMockMvc
public class DirectMessageApi {

	RestClient restClient = RestClient.create("http://localhost:8080");

	@Autowired
	MockMvc mockMvc;

	private ObjectMapper objectMapper;

	@BeforeEach
	void setUp() {
		objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
	}

	@Test
	void getDirectMessages() throws Exception {
		// DataInitializer 에서 data 생성해서 실제 데이터베이스 user 테이블에 있는 userId를 넣어야함
		UUID currentUserId = UUID.fromString("3accb62a-acbc-492a-b077-9e7d964f2e19");
		UUID targetUserId = UUID.fromString("33bbc8a8-aaec-41d9-aa3e-5e8c9f3d453a");

		CustomUserDetails mockUserDetails = mock(CustomUserDetails.class);
		when(mockUserDetails.getId()).thenReturn(currentUserId);
		when(mockUserDetails.getUsername()).thenReturn("testUser");

		// 첫번째 페이지 가져오기 .
		MvcResult result = mockMvc.perform(get("/api/direct-messages")
				.param("userId", targetUserId.toString())
				.param("limit", "10")
				.with(user(mockUserDetails)))
			.andExpect(status().isOk())
			.andReturn();

		String responseBody = result.getResponse().getContentAsString();
		DirectMessageDtoCursorResponse response = objectMapper.readValue(
			responseBody, DirectMessageDtoCursorResponse.class
		);

		List<DirectMessageDto> data = response.getData();
		System.out.println("[First Page]");
		for (DirectMessageDto messageDto : data) {
			System.out.println("%s : content %s"
				.formatted(messageDto.sender().name(), messageDto.content()));
		}

		DirectMessageDto lastElement = response.data.getLast();

		String nextCursor = lastElement.createdAt().toString();
		UUID nextIdAfter = lastElement.id();

		System.out.println("nextCursor: " + nextCursor);
		System.out.println("nextIdAfter: " + nextIdAfter);

		// 두번째 페이지 가져오기 .
		MvcResult result2 = mockMvc.perform(get("/api/direct-messages")
				.param("userId", targetUserId.toString())
				.param("limit", "10")
				.param("cursor", nextCursor)
				.param("idAfter", nextIdAfter.toString())
				.with(user(mockUserDetails)))
			.andExpect(status().isOk())
			.andReturn();

		String nextPageResponse = result2.getResponse().getContentAsString();
		DirectMessageDtoCursorResponse response2 = objectMapper.readValue(
			nextPageResponse, DirectMessageDtoCursorResponse.class
		);

		System.out.println("[Next Page]");
		List<DirectMessageDto> data2 = response2.data;
		for (DirectMessageDto messageDto : data2) {
			System.out.println("%s : content %s"
				.formatted(messageDto.sender().name(), messageDto.content()));
		}
	}

	@Data
	public static class DirectMessageDtoCursorResponse {

		private List<DirectMessageDto> data;
		private String nextCursor;
		private UUID nextIdAfter;
		private boolean hasNext;
		private int totalCount; // 두 사용자 간의 전체 DM 개수
		private String sortBy;
		private SortDirection sortDirection;
	}
}
