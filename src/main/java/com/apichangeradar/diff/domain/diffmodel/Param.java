package com.apichangeradar.diff.domain.diffmodel;

import lombok.Data;

@Data
public class Param {
    private String name;
    private String in;
    private boolean required;
    private String type;
}

