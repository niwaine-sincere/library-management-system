package com.librarymanagement.service;

import com.librarymanagement.entity.Loan;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record LoanResponse(

        Long id,

        Long userId,

        String memberName,

        Long bookId,

        String bookTitle,

        LocalDateTime borrowedAt,

        LocalDate dueDate,

        LocalDateTime returnedAt,

        String status,

        Double fine

) {


    public static LoanResponse from(
            Loan loan
    ) {

        return new LoanResponse(

                loan.getId(),

                loan.getUser().getId(),

                loan.getUser().getFullName(),

                loan.getBook().getId(),

                loan.getBook().getTitle(),

                loan.getBorrowedAt(),

                loan.getDueDate(),

                loan.getReturnedAt(),

                loan.getStatus(),

                loan.getFine()
        );
    }
}