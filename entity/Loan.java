package com.librarymanagement.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "loans")
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    /*
     * Many loans can belong to one user.
     */
    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    @JsonIgnore
    private User user;


    /*
     * Many loans can belong to one book.
     */
    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "book_id",
            nullable = false
    )
    @JsonIgnore
    private Book book;


    @Column(nullable = false)
    private LocalDateTime borrowedAt;


    @Column(nullable = false)
    private LocalDate dueDate;


    private LocalDateTime returnedAt;


    @Column(
            nullable = false,
            length = 20
    )
    private String status = "BORROWED";


    @Column(nullable = false)
    private Double fine = 0.0;


    public Loan() {
    }


    public Long getId() {
        return id;
    }


    public User getUser() {
        return user;
    }


    public void setUser(User user) {
        this.user = user;
    }


    public Book getBook() {
        return book;
    }


    public void setBook(Book book) {
        this.book = book;
    }


    public LocalDateTime getBorrowedAt() {
        return borrowedAt;
    }


    public void setBorrowedAt(
            LocalDateTime borrowedAt
    ) {

        this.borrowedAt = borrowedAt;
    }


    public LocalDate getDueDate() {
        return dueDate;
    }


    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }


    public LocalDateTime getReturnedAt() {
        return returnedAt;
    }


    public void setReturnedAt(
            LocalDateTime returnedAt
    ) {

        this.returnedAt = returnedAt;
    }


    public String getStatus() {
        return status;
    }


    public void setStatus(String status) {
        this.status = status;
    }


    public Double getFine() {
        return fine;
    }


    public void setFine(Double fine) {
        this.fine = fine;
    }


    public Long getUserId() {

        return user != null
                ? user.getId()
                : null;
    }


    public String getMemberName() {

        return user != null
                ? user.getFullName()
                : null;
    }


    public Long getBookId() {

        return book != null
                ? book.getId()
                : null;
    }


    public String getBookTitle() {

        return book != null
                ? book.getTitle()
                : null;
    }
}