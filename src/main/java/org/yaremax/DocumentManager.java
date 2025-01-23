package org.yaremax;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * For implement this task focus on clear code, and make this solution as simple readable as possible
 * Don't worry about performance, concurrency, etc
 * You can use in Memory collection for sore data
 * <p>
 * Please, don't change class name, and signature for methods save, search, findById
 * Implementations should be in a single class
 * This class could be auto tested
 */
public class DocumentManager {
    private final Map<String, Document> documentStorage = new HashMap<>();

    /**
     * Implementation of this method should upsert the document to your storage
     * And generate unique id if it does not exist, don't change [created] field
     *
     * @param document - document content and author data
     * @return saved document
     */
    public Document save(Document document) {
        if (document.getId() == null || !documentStorage.containsKey(document.getId())) {
            document.setId(UUID.randomUUID().toString());
        }
        documentStorage.put(document.getId(), document);
        return document;
    }

    /**
     * Implementation this method should find documents which match with request
     *
     * @param request - search request, each field could be null
     * @return list matched documents
     */
    public List<Document> search(SearchRequest request) {
        return documentStorage.values().stream()
                .filter(document -> isMatchingDocument(document, request))
                .toList();
    }

    private boolean isMatchingDocument(Document document, SearchRequest request) {
        return request == null
                || (matchTitlePrefixes(document.getTitle(), request.getTitlePrefixes())
                && matchContainsContents(document.getContent(), request.getContainsContents())
                && matchAuthorIds(document.getAuthor().getId(), request.getAuthorIds())
                && matchCreatedFrom(document.getCreated(), request.getCreatedFrom())
                && matchCreatedTo(document.getCreated(), request.getCreatedTo()));
    }

    private boolean matchTitlePrefixes(String value, List<String> prefixes) {
        return prefixes == null || prefixes.isEmpty() ||
                prefixes.stream().anyMatch(prefix -> value != null && value.startsWith(prefix));
    }

    private boolean matchContainsContents(String value, List<String> contents) {
        return contents == null || contents.isEmpty() ||
                contents.stream().anyMatch(content -> value != null && value.contains(content));
    }

    private boolean matchAuthorIds(String documentAuthorId, List<String> authorIds) {
        return authorIds == null || authorIds.isEmpty() ||
                authorIds.contains(documentAuthorId);
    }

    private boolean matchCreatedFrom(Instant documentCreated, Instant from) {
        return from == null || (documentCreated != null && documentCreated.isAfter(from));
    }

    private boolean matchCreatedTo(Instant documentCreated, Instant to) {
        return to == null || (documentCreated != null && documentCreated.isBefore(to));
    }

    /**
     * Implementation this method should find document by id
     *
     * @param id - document id
     * @return optional document
     */
    public Optional<Document> findById(String id) {
        return Optional.ofNullable(documentStorage.get(id));
    }

    @Data
    @Builder
    public static class SearchRequest {
        private List<String> titlePrefixes;
        private List<String> containsContents;
        private List<String> authorIds;
        private Instant createdFrom;
        private Instant createdTo;
    }

    @Data
    @Builder
    public static class Document {
        private String id;
        private String title;
        private String content;
        private Author author;
        private Instant created;
    }

    @Data
    @Builder
    public static class Author {
        private String id;
        private String name;
    }
}