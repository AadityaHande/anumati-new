package in.anumati.platform;

import in.anumati.platform.document.analysis.DocumentFieldExtractor;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DocumentFieldExtractorTest {
    private final DocumentFieldExtractor extractor = new DocumentFieldExtractor();

    @Test
    void doesNotTreatPanSegmentInsideGstinAsPan() {
        Map<String, String> fields = extractor.extract("GSTIN: 27ABCDE1234F1Z5");
        assertEquals("27ABCDE1234F1Z5", fields.get("GSTIN"));
        assertNull(fields.get("PAN"));
    }

    @Test
    void extractsStandalonePanAndGstin() {
        Map<String, String> fields = extractor.extract("PAN: ABCDE1234F\nGSTIN: 27ABCDE1234F1Z5");
        assertEquals("ABCDE1234F", fields.get("PAN"));
        assertEquals("27ABCDE1234F1Z5", fields.get("GSTIN"));
    }
}
