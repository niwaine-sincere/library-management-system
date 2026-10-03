package com.librarymanagement.repository;

import com.librarymanagement.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanRepository
        extends JpaRepository<Loan, Long> {


    List<Loan>
    findByUserIdOrderByIdDesc(
            Long userId
    );


    List<Loan>
    findAllByOrderByIdDesc();


    long countByStatus(
            String status
    );


    boolean
    existsByUserIdAndBookIdAndStatus(
            Long userId,
            Long bookId,
            String status
    );
}