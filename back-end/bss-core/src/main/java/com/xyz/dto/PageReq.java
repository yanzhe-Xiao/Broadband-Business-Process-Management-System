package com.xyz.dto;

import lombok.Data;

@Data
public class PageReq {
    private Integer current = 1;
    private Integer size = 10;
    private String keyword;
    private String status;
}