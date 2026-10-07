package com.apichangeradar.diff.domain.diffmodel;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class PathItem {
    private String path;
    private Map<String, Operation> operations = new LinkedHashMap<>();
}

