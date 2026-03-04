package org.jabref.logic.util;

import java.util.Map;
import java.util.Optional;

import org.jabref.logic.importer.AuthorListParser;
import org.jabref.logic.importer.ImporterPreferences;
import org.jabref.model.entry.Author;
import org.jabref.model.entry.AuthorList;
import org.jabref.model.entry.BibEntry;
import org.jabref.model.entry.field.StandardField;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExternalLinkCreatorMockAuthorTest {

    private ImporterPreferences mockPreferences;
    private AuthorListParser mockParser;
    private ExternalLinkCreator linkCreator;

    @BeforeEach
    void setUp() {
        mockPreferences = mock(ImporterPreferences.class);
        when(mockPreferences.getSearchEngineUrlTemplates()).thenReturn(Map.of());

        mockParser = mock(AuthorListParser.class);
        linkCreator = new ExternalLinkCreator(mockPreferences, mockParser);
    }

    private BibEntry createEntry(String title, String author) {
        return new BibEntry()
                .withField(StandardField.TITLE, title)
                .withField(StandardField.AUTHOR, author);
    }

    @Test
    void emptyAuthorListProducesNoAuthorParams() {
        when(mockParser.parse(anyString())).thenReturn(AuthorList.of());

        BibEntry entry = createEntry("Quantum Computing", "some author text");
        Optional<String> url = linkCreator.getSemanticScholarSearchURL(entry);

        assertTrue(url.isPresent());
        assertFalse(url.get().contains("author"),
                "URL should have no author parameters when parser returns empty list");
        assertTrue(url.get().contains("q=Quantum%20Computing"));
    }

    @Test
    void threeAuthorsAreIndexedCorrectly() {
        AuthorList threeAuthors = AuthorList.of(
                new Author("Alice", "A.", null, "Smith", null),
                new Author("Bob", "B.", null, "Jones", null),
                new Author("Carol", "C.", null, "Williams", null)
        );
        when(mockParser.parse(anyString())).thenReturn(threeAuthors);

        BibEntry entry = createEntry("Deep Learning", "irrelevant - parser is mocked");
        Optional<String> url = linkCreator.getSemanticScholarSearchURL(entry);

        assertTrue(url.isPresent());
        String urlStr = url.get();
        assertTrue(urlStr.contains("author%5B0%5D=Smith%2C%20A."),
                "URL should contain author[0]=Smith, A. but was: " + urlStr);
        assertTrue(urlStr.contains("author%5B1%5D=Jones%2C%20B."),
                "URL should contain author[1]=Jones, B. but was: " + urlStr);
        assertTrue(urlStr.contains("author%5B2%5D=Williams%2C%20C."),
                "URL should contain author[2]=Williams, C. but was: " + urlStr);
        assertFalse(urlStr.contains("author%5B3%5D"),
                "URL should not contain author[3]");
    }
}
