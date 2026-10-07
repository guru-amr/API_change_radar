package com.apichangeradar.diff.domain.diffmodel;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class ApiSchema {
    private Map<String, PathItem> paths = new LinkedHashMap<>();
}

