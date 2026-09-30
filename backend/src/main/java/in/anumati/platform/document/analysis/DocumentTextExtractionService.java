package in.anumati.platform.document.analysis;

import in.anumati.platform.document.Document;
import in.anumati.platform.storage.ObjectStorageService;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class DocumentTextExtractionService {
    private final ObjectStorageService storage;
    private final DocumentAnalysisProperties properties;

    public DocumentTextExtractionService(ObjectStorageService storage, DocumentAnalysisProperties properties) { this.storage = storage; this.properties = properties; }

    public String extract(Document document) {
        try (InputStream input = storage.download(document.getObjectKey())) {
            Path temp = Files.createTempFile("anumati-doc-", extension(document.getOriginalFilename()));
            Files.copy(input, temp, StandardCopyOption.REPLACE_EXISTING);
            try {
                return switch (document.getContentType()) {
                    case "image/png", "image/jpeg" -> runTesseract(temp, false);
                    case "application/pdf" -> runPdfOcr(temp);
                    case "text/plain" -> Files.readString(temp, StandardCharsets.UTF_8);
                    case "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> extractDocx(temp);
                    default -> "";
                };
            } finally {
                Files.deleteIfExists(temp);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Document extraction failed", e);
        }
    }

    private String runPdfOcr(Path pdf) throws IOException {
        Path dir = Files.createTempDirectory("anumati-pdf-pages-");
        try {
            Path prefix = dir.resolve("page");
            run(List.of("pdftoppm", "-f", "1", "-l", String.valueOf(properties.maxPdfPages()), "-jpeg", "-r", "160", pdf.toString(), prefix.toString()), properties.commandTimeoutSeconds());
            StringBuilder text = new StringBuilder();
            try (var files = Files.list(dir)) {
                for (Path image : files.filter(p -> p.getFileName().toString().endsWith(".jpg")).sorted().toList()) {
                    text.append(runTesseract(image, false)).append('\n');
                }
            }
            return text.toString();
        } finally {
            try (var files = Files.list(dir)) {
                for (Path path : files.toList()) Files.deleteIfExists(path);
            }
            Files.deleteIfExists(dir);
        }
    }

    private String runTesseract(Path image, boolean preserveInterword) throws IOException {
        return run(List.of("tesseract", image.toString(), "stdout", "--psm", "6"), properties.commandTimeoutSeconds());
    }

    private String extractDocx(Path docx) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(docx))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if ("word/document.xml".equals(entry.getName())) {
                    String xml = new String(zis.readAllBytes(), StandardCharsets.UTF_8);
                    return xml.replaceAll("</w:p>", "\\n").replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").trim();
                }
            }
        }
        return "";
    }

    private String run(List<String> command, long timeoutSeconds) throws IOException {
        Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
        String output;
        try (InputStream stream = process.getInputStream()) {
            output = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
        try {
            if (!process.waitFor(timeoutSeconds, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                throw new IllegalStateException("Document extraction timed out");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Document extraction interrupted", e);
        }
        if (process.exitValue() != 0) throw new IllegalStateException("Document extraction command failed: " + output);
        return output.trim();
    }

    private static String extension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot > 0 ? filename.substring(dot) : ".bin";
    }
}
