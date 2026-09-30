package in.anumati.platform.document.analysis;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class DocumentFieldExtractor {
    private static final Pattern PAN = Pattern.compile("\\b[A-Z]{5}[0-9]{4}[A-Z]\\b");
    private static final Pattern GSTIN = Pattern.compile("\\b[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z][1-9A-Z]Z[0-9A-Z]\\b");
    private static final Pattern EMAIL = Pattern.compile("\\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern LABEL = Pattern.compile("(?im)^(business\\s*name|name of business|registered name|district|city|pan|gstin)\\s*[:\\-]\\s*(.+)$");

    public Map<String, String> extract(String text) {
        String normalized = text == null ? "" : text.replace('\u00A0', ' ').replaceAll("[ \\t]+", " ");
        Map<String, String> fields = new LinkedHashMap<>();
        String withoutGstin = GSTIN.matcher(normalized).replaceAll(" ");
        captureFirst(GSTIN, normalized, "GSTIN", fields);
        captureFirst(PAN, withoutGstin, "PAN", fields);
        captureFirst(EMAIL, normalized, "EMAIL", fields);
        Matcher matcher = LABEL.matcher(normalized);
        while (matcher.find()) {
            String key = matcher.group(1).toUpperCase(Locale.ROOT).replaceAll("\\s+", "_");
            if (key.equals("NAME_OF_BUSINESS") || key.equals("REGISTERED_NAME")) key = "BUSINESS_NAME";
            if (key.equals("CITY")) key = "DISTRICT";
            fields.putIfAbsent(key, matcher.group(2).trim());
        }
        return fields;
    }

    private static void captureFirst(Pattern pattern, String text, String name, Map<String, String> fields) {
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) fields.put(name, matcher.group());
    }
}
