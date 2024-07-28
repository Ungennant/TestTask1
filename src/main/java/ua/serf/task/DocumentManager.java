package ua.serf.task;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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

    private final List<Document> storage = new ArrayList<>();

    /**
     * Implementation of this method should upsert the document to your storage
     * And generate unique id if it does not exist
     *
     * @param document - document content and author data
     * @return saved document
     */
    public Document save(Document document) {
        if (document.getId() == null || document.getId().isEmpty()) {
            String newId = generateUniqueId();
            Document newDocument = Document.builder()
                    .id(newId)
                    .title(document.getTitle())
                    .content(document.getContent())
                    .author(document.getAuthor())
                    .created(document.getCreated())
                    .build();
            storage.add(newDocument);
            return newDocument;
        } else {
            for (int i = 0; i < storage.size(); i++) {
                if (storage.get(i).getId().equals(document.getId())) {
                    storage.set(i, document);
                    return document;
                }
            }
            storage.add(document);
            return document;
        }
    }

    /**
     * Implementation this method should find documents which match with request
     *
     * @param request - search request, each field could be null
     * @return list matched documents
     */
    public List<Document> search(SearchRequest request) {
        return storage.stream()
                .filter(doc -> matchesRequest(doc, request))
                .collect(Collectors.toList());
    }
    
    /**
     * Implementation this method should find document by id
     *
     * @param id - document id
     * @return optional document
     */
    public Optional<Document> findById(String id) {
        return storage.stream()
                .filter(doc -> doc.getId().equals(id))
                .findFirst();
    }

    private boolean matchesRequest(Document doc, SearchRequest request) {
        if (request.getTitlePrefixes() != null) {
            boolean matchesTitlePrefix = request.getTitlePrefixes().stream()
                    .anyMatch(prefix -> doc.getTitle() != null && doc.getTitle().startsWith(prefix));
            if (!matchesTitlePrefix) return false;
        }
        if (request.getContainsContents() != null) {
            boolean matchesContent = request.getContainsContents().stream()
                    .anyMatch(content -> doc.getContent() != null && doc.getContent().contains(content));
            if (!matchesContent) return false;
        }
        if (request.getAuthorIds() != null) {
            boolean matchesAuthorId = request.getAuthorIds().contains(doc.getAuthor() != null ? doc.getAuthor().getId() : null);
            if (!matchesAuthorId) return false;
        }
        if (request.getCreatedFrom() != null && doc.getCreated() != null) {
            if (doc.getCreated().isBefore(request.getCreatedFrom())) return false;
        }
        if (request.getCreatedTo() != null && doc.getCreated() != null) {
            if (doc.getCreated().isAfter(request.getCreatedTo())) return false;
        }
        return true;
    }

    private String generateUniqueId() {
        return UUID.randomUUID().toString();
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
