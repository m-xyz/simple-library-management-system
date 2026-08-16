package com.libmanagementsys.vestas_proj.dto;

import com.libmanagementsys.vestas_proj.model.Book;

public record WaitingListEntryDto(
        Book book,
        int position) {
}