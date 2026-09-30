package in.anumati.platform.document.analysis;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "anumati.document-analysis")
public record DocumentAnalysisProperties(int commandTimeoutSeconds, int maxPdfPages) {
    public DocumentAnalysisProperties {
        if (commandTimeoutSeconds <= 0) throw new IllegalArgumentException("commandTimeoutSeconds must be positive");
        if (maxPdfPages <= 0) throw new IllegalArgumentException("maxPdfPages must be positive");
    }
}
