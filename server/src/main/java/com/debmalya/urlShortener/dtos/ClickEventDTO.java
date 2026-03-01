package com.debmalya.urlShortener.dtos;

import java.time.LocalDate;

import lombok.Data;

@Data
public class ClickEventDTO {
    private LocalDate clickDate;
    private Long count;
}
