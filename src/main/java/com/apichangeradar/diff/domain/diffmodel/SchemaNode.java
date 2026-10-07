package com.apichangeradar.diff.domain.diffmodel;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class SchemaNode {
    private String type;
    private Map<String, Field> fields = new LinkedHashMap<>();
    private SchemaNode items;
}

