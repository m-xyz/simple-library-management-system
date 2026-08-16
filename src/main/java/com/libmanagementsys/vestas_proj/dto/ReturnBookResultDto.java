package com.libmanagementsys.vestas_proj.dto;

import java.math.BigDecimal;

public record ReturnBookResultDto(
        String title,
        String isbn,
        BigDecimal fine) {
}
