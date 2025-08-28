package com.team3.otboo.domain.feedread.repository;

import com.team3.otboo.domain.feedread.document.FeedDocument;
import java.util.UUID;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface FeedSearchRepository extends ElasticsearchRepository<FeedDocument, UUID> {

}
