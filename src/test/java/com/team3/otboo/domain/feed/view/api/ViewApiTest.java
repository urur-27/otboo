package com.team3.otboo.domain.feed.view.api;


import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

public class ViewApiTest {

	RestClient restClient = RestClient.create("http://localhost:8080");

	@Test
	void viewTest() throws InterruptedException {
		ExecutorService executorService = Executors.newFixedThreadPool(100);
		CountDownLatch latch = new CountDownLatch(10000);

		UUID feedId = UUID.randomUUID();

		for (int i = 0; i < 10000; i++) {
			executorService.submit(() -> {
				try {
					restClient.post()
						.uri("/api/feeds/{feedId}/view", feedId)
						.retrieve()
						.toBodilessEntity();
				} finally {
					latch.countDown();
				}
			});
		}

		latch.await();

		Long count = restClient.get()
			.uri("/api/feeds/{feedId}/view", feedId)
			.retrieve()
			.body(Long.class);

		System.out.println("count = " + count); // 한번 실행할때마다 view 가 100 씩 증가.
	}
}
