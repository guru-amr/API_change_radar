package com.apichangeradar.diff.domain.diffmodel;

import lombok.Data;

@Data
public class Field {
    private String name;
    private String type;
    private boolean required;
}

