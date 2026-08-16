package com.libmanagementsys.vestas_proj.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AppProperties {
    @Value("${loan.late.fee}")
    private String lateFee;

    @Value("${book.borrow.days}")
    private int bookBorrowDays;

    public String getLateReturnFee() {
        return this.lateFee;
    }

    public int getBookBorrowDays() {
        return this.bookBorrowDays;
    }

}
