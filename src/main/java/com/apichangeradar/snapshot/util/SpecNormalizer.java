package com.apichangeradar.snapshot.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.springframework.stereotype.Component;

@Component
public class SpecNormalizer {
    private final ObjectMapper json = new ObjectMapper();
    private final ObjectMapper yaml = new ObjectMapper(new YAMLFactory());

    public String normalize(String raw) {
        String trimmed = raw==null?"":raw.trim();
        if (trimmed.isEmpty()) return trimmed;
        try {
            if (trimmed.startsWith("{")) {
                Object o = json.readValue(trimmed, Object.class);
                return json.writerWithDefaultPrettyPrinter().writeValueAsString(o);
            } else {
                Object o = yaml.readValue(trimmed, Object.class);
                return json.writerWithDefaultPrettyPrinter().writeValueAsString(o);
            }
        } catch (Exception e) {
            return trimmed;
        }
    }
}

