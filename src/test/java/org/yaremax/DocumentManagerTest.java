package org.yaremax;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.yaremax.DocumentManager.Author;
import org.yaremax.DocumentManager.Document;
import org.yaremax.DocumentManager.SearchRequest;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DocumentManagerTest {

    private DocumentManager documentManager;
    private Instant now;

    @BeforeEach
    void setUp() {
        documentManager = new DocumentManager();
        now = Instant.now();
    }

    @Test
    void shouldSave_NewDocument_WithGeneratedId() {
        Document document = Document.builder()
                .title("Test Title")
                .content("Test Content")
                .author(Author.builder().id("1").name("Author").build())
                .created(now)
                .build();

        Document savedDocument = documentManager.save(document);

        assertNotNull(savedDocument.getId());
        assertEquals("Test Title", savedDocument.getTitle());
    }

    @Test
    void shouldUpdate_ExistingDocument_WithSameId() {
        Document originalDocument = Document.builder()
                .title("Original Title")
                .content("Original Content")
                .author(Author.builder().id("1").name("Author").build())
                .created(now)
                .build();

        String documentId = documentManager.save(originalDocument).getId();

        Document updatedDocument = Document.builder()
                .id(documentId)
                .title("Updated Title")
                .content("Updated Content")
                .author(Author.builder().id("1").name("Author").build())
                .created(now)
                .build();

        documentManager.save(updatedDocument);

        Optional<Document> retrievedDocument = documentManager.findById(documentId);
        assertTrue(retrievedDocument.isPresent());
        assertEquals("Updated Title", retrievedDocument.get().getTitle());
    }

    @Test
    void shouldFind_DocumentById_WhenExists() {
        Document document = Document.builder()
                .title("Find Me")
                .content("Some Content")
                .author(Author.builder().id("1").name("Author").build())
                .created(now)
                .build();

        String documentId = documentManager.save(document).getId();
        Optional<Document> foundDocument = documentManager.findById(documentId);

        assertTrue(foundDocument.isPresent());
        assertEquals("Find Me", foundDocument.get().getTitle());
    }

    @Test
    void shouldSearch_DocumentsByTitlePrefix() {
        documentManager.save(Document.builder()
                .title("Alpha Document")
                .content("Content A")
                .author(Author.builder().id("1").name("Author A").build())
                .created(now)
                .build());

        documentManager.save(Document.builder()
                .title("Beta Document")
                .content("Content B")
                .author(Author.builder().id("2").name("Author B").build())
                .created(now)
                .build());

        SearchRequest request = SearchRequest.builder()
                .titlePrefixes(List.of("Alpha"))
                .build();

        List<Document> results = documentManager.search(request);
        assertEquals(1, results.size());
        assertEquals("Alpha Document", results.get(0).getTitle());
    }

    @Test
    void shouldSearch_DocumentsByContent() {
        documentManager.save(Document.builder()
                .title("Document 1")
                .content("Hello World")
                .author(Author.builder().id("1").name("Author A").build())
                .created(now)
                .build());

        documentManager.save(Document.builder()
                .title("Document 2")
                .content("Another Content")
                .author(Author.builder().id("2").name("Author B").build())
                .created(now)
                .build());

        SearchRequest request = SearchRequest.builder()
                .containsContents(List.of("Hello"))
                .build();

        List<Document> results = documentManager.search(request);
        assertEquals(1, results.size());
        assertEquals("Document 1", results.get(0).getTitle());
    }

    @Test
    void shouldSearch_DocumentsByAuthorId() {
        documentManager.save(Document.builder()
                .title("Document 1")
                .content("Content 1")
                .author(Author.builder().id("1").name("Author A").build())
                .created(now)
                .build());

        documentManager.save(Document.builder()
                .title("Document 2")
                .content("Content 2")
                .author(Author.builder().id("2").name("Author B").build())
                .created(now)
                .build());

        SearchRequest request = SearchRequest.builder()
                .authorIds(List.of("1"))
                .build();

        List<Document> results = documentManager.search(request);
        assertEquals(1, results.size());
        assertEquals("Document 1", results.get(0).getTitle());
    }

    @Test
    void shouldSearch_DocumentsByCreatedDateRange() {
        documentManager.save(Document.builder()
                .title("Old Document")
                .content("Content 1")
                .author(Author.builder().id("1").name("Author A").build())
                .created(now.minusSeconds(120))
                .build());

        documentManager.save(Document.builder()
                .title("New Document")
                .content("Content 2")
                .author(Author.builder().id("2").name("Author B").build())
                .created(now)
                .build());

        SearchRequest request = SearchRequest.builder()
                .createdFrom(now.minusSeconds(60))
                .createdTo(now.plusSeconds(60))
                .build();

        List<Document> results = documentManager.search(request);
        assertEquals(1, results.size());
        assertEquals("New Document", results.get(0).getTitle());
    }

    @Test
    void shouldReturn_AllDocuments_WhenSearchRequestIsEmpty() {
        documentManager.save(Document.builder()
                .title("Document 1")
                .content("Content 1")
                .author(Author.builder().id("1").name("Author A").build())
                .created(now)
                .build());

        SearchRequest request = SearchRequest.builder().build();
        List<Document> results = documentManager.search(request);
        assertEquals(1, results.size());
    }

    @Test
    void shouldReturn_AllDocuments_WhenSearchRequestIsNull() {
        documentManager.save(Document.builder()
                .title("Document 1")
                .content("Content 1")
                .author(Author.builder().id("1").name("Author A").build())
                .created(now)
                .build());

        List<Document> results = documentManager.search(null);
        assertEquals(1, results.size());
    }
}