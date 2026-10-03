package com.librarymanagement.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(
            nullable = false,
            length = 180
    )
    private String title;


    @Column(
            nullable = false,
            length = 120
    )
    private String author;


    @Column(
            unique = true,
            length = 30
    )
    private String isbn;


    @Column(length = 80)
    private String category;


    @Column(nullable = false)
    private Integer totalCopies = 1;


    @Column(nullable = false)
    private Integer availableCopies = 1;


    public Book() {
    }


    public Book(
            String title,
            String author,
            String isbn,
            String category,
            Integer totalCopies
    ) {

        this.title = title;
        this.author = author;
        this.isbn = isbn;

        this.category = category;

        this.totalCopies = totalCopies;

        this.availableCopies = totalCopies;
    }


    public Long getId() {
        return id;
    }


    public String getTitle() {
        return title;
    }


    public void setTitle(String title) {
        this.title = title;
    }


    public String getAuthor() {
        return author;
    }


    public void setAuthor(String author) {
        this.author = author;
    }


    public String getIsbn() {
        return isbn;
    }


    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }


    public String getCategory() {
        return category;
    }


    public void setCategory(String category) {
        this.category = category;
    }


    public Integer getTotalCopies() {
        return totalCopies;
    }


    public void setTotalCopies(Integer totalCopies) {
        this.totalCopies = totalCopies;
    }


    public Integer getAvailableCopies() {
        return availableCopies;
    }


    public void setAvailableCopies(Integer availableCopies) {
        this.availableCopies = availableCopies;
    }
}