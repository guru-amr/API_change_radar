package com.apichangeradar.diff.domain.diffmodel;

import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
public class Operation {
    private String method;
    private String path;
    private List<Param> parameters = new ArrayList<>();
    private SchemaNode requestBodySchema;
    private SchemaNode responseSchema;
}

