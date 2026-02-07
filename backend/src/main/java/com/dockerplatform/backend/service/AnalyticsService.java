package com.dockerplatform.backend.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.dockerplatform.backend.DockerPlatformBackendApplication;
import com.dockerplatform.backend.dto.LogSearchQuery;
import com.dockerplatform.backend.dto.LogSearchResponse;
import com.dockerplatform.backend.models.LogEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(DockerPlatformBackendApplication.class);
    @Autowired
    private ElasticsearchClient elasticsearchClient;

    private Query parseCustomQuery(String customQuery) {
        // "(level:ERROR OR level:WARNING) AND message:\"database\""

        return Query.of(q -> q
            .queryString(qs -> qs
                .query(customQuery)
                .fields("message", "logger_name")
                .defaultOperator(Operator.And)
            )
        );
    }

    public LogSearchResponse searchLogs(LogSearchQuery searchQuery) {
        try {
            BoolQuery.Builder boolQuery = new BoolQuery.Builder();

            // text search if query is provided
            if (searchQuery.getQuery() != null && !searchQuery.getQuery().isEmpty()) {
                // convert to Elasticsearch query
                Query textQuery = parseCustomQuery(searchQuery.getQuery());
                boolQuery.must(textQuery);
            }

            // date range filter
            if (searchQuery.getStartDate() != null || searchQuery.getEndDate() != null) {
                Query dateRangeQuery = Query.of(q -> q
                        .range(r -> r
                                .date(d -> {
                                    d.field("@timestamp");
                                    if (searchQuery.getStartDate() != null) {
                                        d.gte(searchQuery.getStartDate());
                                    }
                                    if (searchQuery.getEndDate() != null) {
                                        d.lte(searchQuery.getEndDate());
                                    }
                                    return d;
                                })
                        )
                );
                boolQuery.filter(dateRangeQuery);
            }

            // log level filter
            if (searchQuery.getLevels() != null && !searchQuery.getLevels().isEmpty()) {
                Query levelQuery = Query.of(q -> q
                        .terms(t -> t
                                .field("level.keyword")
                                .terms(tt -> tt
                                        .value(searchQuery.getLevels().stream()
                                                .map(FieldValue::of)
                                                .toList()
                                        )
                                )
                        )
                );
                //log.info("query: ");
                //log.info(String.valueOf(levelQuery));
                boolQuery.filter(levelQuery);
            }

            // search request
            SearchRequest searchRequest = SearchRequest.of(s -> s
                    .index("docker-platform-logs-*")  // index pattern from logstash.conf
                    .query(q -> q.bool(boolQuery.build()))
                    .size(searchQuery.getSize())
                    .from(searchQuery.getFrom())
                    .sort(sort -> sort.field(f -> f.field("@timestamp").order(SortOrder.Desc)))
            );

            SearchResponse<LogEntry> searchResponse = elasticsearchClient.search(
                    searchRequest,
                    LogEntry.class
            );

            // response
            List<LogEntry> hits = searchResponse.hits().hits().stream()
                    .map(Hit::source)
                    .collect(Collectors.toList());

            LogSearchResponse response = new LogSearchResponse();
            response.setHits(hits);
            response.setTotal(searchResponse.hits().total().value());
            response.setTook(searchResponse.took());

            return response;

        } catch (IOException e) {
            throw new RuntimeException("Failed to search logs", e);
        }
    }

    public byte[] exportLogs(LogSearchQuery query) {
        query.setSize(10000); // max export size
        LogSearchResponse response = searchLogs(query);

        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsBytes(response.getHits());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to export logs", e);
        }
    }
}
