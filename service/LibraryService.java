package com.librarymanagement.service;

import com.librarymanagement.entity.Book;
import com.librarymanagement.entity.Loan;
import com.librarymanagement.entity.User;

import com.librarymanagement.repository.BookRepository;
import com.librarymanagement.repository.LoanRepository;
import com.librarymanagement.repository.UserRepository;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class LibraryService {

    private final UserRepository userRepository;

    private final BookRepository bookRepository;

    private final LoanRepository loanRepository;

    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();


    public LibraryService(
            UserRepository userRepository,
            BookRepository bookRepository,
            LoanRepository loanRepository
    ) {

        this.userRepository = userRepository;

        this.bookRepository = bookRepository;

        this.loanRepository = loanRepository;
    }


    /*
     * REGISTER MEMBER
     */
    public User registerMember(
            String name,
            String email,
            String password
    ) {

        if (
                name == null ||
                name.isBlank() ||

                email == null ||
                email.isBlank() ||

                password == null ||
                password.length() < 6
        ) {

            throw new IllegalArgumentException(
                    "Name, email and a password of at least 6 characters are required."
            );
        }


        if (
                userRepository
                        .findByEmailIgnoreCase(email)
                        .isPresent()
        ) {

            throw new IllegalArgumentException(
                    "An account with that email already exists."
            );
        }


        User user = new User(

                name.trim(),

                email.trim().toLowerCase(),

                passwordEncoder.encode(password),

                "MEMBER"
        );


        return userRepository.save(user);
    }


    /*
     * LOGIN
     */
    public User login(
            String email,
            String password
    ) {

        User user =
                userRepository
                        .findByEmailIgnoreCase(email)
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Invalid email or password."
                                )
                        );


        if (
                !passwordEncoder.matches(
                        password,
                        user.getPassword()
                )
        ) {

            throw new IllegalArgumentException(
                    "Invalid email or password."
            );
        }


        return user;
    }


    /*
     * SEARCH BOOKS
     */
    public List<Book> searchBooks(
            String q
    ) {

        if (
                q == null ||
                q.isBlank()
        ) {

            return bookRepository.findAll();
        }


        return bookRepository
                .findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCaseOrIsbnContainingIgnoreCase(
                        q,
                        q,
                        q
                );
    }


    /*
     * ADD BOOK
     */
    public Book addBook(
            Book book
    ) {

        if (
                book.getTitle() == null ||
                book.getTitle().isBlank()
        ) {

            throw new IllegalArgumentException(
                    "Title is required."
            );
        }


        if (
                book.getAuthor() == null ||
                book.getAuthor().isBlank()
        ) {

            throw new IllegalArgumentException(
                    "Author is required."
            );
        }


        if (
                book.getTotalCopies() == null ||
                book.getTotalCopies() < 1
        ) {

            throw new IllegalArgumentException(
                    "Total copies must be at least 1."
            );
        }


        book.setAvailableCopies(
                book.getTotalCopies()
        );


        return bookRepository.save(book);
    }


    /*
     * BORROW BOOK
     */
    @Transactional
    public LoanResponse borrowBook(
            Long userId,
            Long bookId
    ) {

        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Member not found."
                                )
                        );


        Book book =
                bookRepository
                        .findById(bookId)
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Book not found."
                                )
                        );


        if (
                book.getAvailableCopies() <= 0
        ) {

            throw new IllegalArgumentException(
                    "No available copy of this book."
            );
        }


        /*
         * Prevent same member from borrowing
         * the same book twice simultaneously.
         */
        if (
                loanRepository
                        .existsByUserIdAndBookIdAndStatus(
                                userId,
                                bookId,
                                "BORROWED"
                        )
        ) {

            throw new IllegalArgumentException(
                    "This member already has this book."
            );
        }


        /*
         * Reduce available copies.
         */
        book.setAvailableCopies(
                book.getAvailableCopies() - 1
        );

        bookRepository.save(book);


        /*
         * Create loan.
         */
        Loan loan = new Loan();

        loan.setUser(user);

        loan.setBook(book);

        loan.setBorrowedAt(
                LocalDateTime.now()
        );

        /*
         * Loan period = 14 days.
         */
        loan.setDueDate(
                LocalDate.now().plusDays(14)
        );

        loan.setStatus("BORROWED");

        loan.setFine(0.0);


        Loan savedLoan =
                loanRepository.save(loan);


        return LoanResponse.from(
                savedLoan
        );
    }


    /*
     * RETURN BOOK
     */
    @Transactional
    public LoanResponse returnBook(
            Long loanId,
            Long requesterId,
            boolean admin
    ) {

        Loan loan =
                loanRepository
                        .findById(loanId)
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Loan not found."
                                )
                        );


        /*
         * A member can only return
         * their own book.
         */
        if (
                !admin &&
                !loan.getUser()
                        .getId()
                        .equals(requesterId)
        ) {

            throw new IllegalArgumentException(
                    "You can only return your own loans."
            );
        }


        if (
                !"BORROWED".equals(
                        loan.getStatus()
                )
        ) {

            throw new IllegalArgumentException(
                    "This loan has already been returned."
            );
        }


        LocalDate today =
                LocalDate.now();


        long overdueDays =
                Math.max(
                        0,
                        today.toEpochDay()
                                - loan.getDueDate().toEpochDay()
                );


        /*
         * UGX 1,000 per overdue day.
         */
        double fine =
                overdueDays * 1000.0;


        loan.setFine(fine);


        loan.setReturnedAt(
                LocalDateTime.now()
        );


        loan.setStatus(
                "RETURNED"
        );


        /*
         * Increase available copies.
         */
        Book book =
                loan.getBook();


        book.setAvailableCopies(
                book.getAvailableCopies() + 1
        );


        bookRepository.save(book);


        Loan savedLoan =
                loanRepository.save(loan);


        return LoanResponse.from(
                savedLoan
        );
    }


    /*
     * ALL LOANS
     */
    @Transactional(readOnly = true)
    public List<LoanResponse> allLoans() {

        return loanRepository
                .findAllByOrderByIdDesc()
                .stream()
                .map(LoanResponse::from)
                .toList();
    }


    /*
     * MEMBER LOANS
     */
    @Transactional(readOnly = true)
    public List<LoanResponse> memberLoans(
            Long userId
    ) {

        return loanRepository
                .findByUserIdOrderByIdDesc(userId)
                .stream()
                .map(LoanResponse::from)
                .toList();
    }


    /*
     * FIND USER
     */
    public User userRepositoryFind(
            Long id
    ) {

        return userRepository
                .findById(id)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "User not found."
                        )
                );
    }


    /*
     * MEMBERS
     */
    public List<User> members() {

        return userRepository
                .findAll()
                .stream()
                .filter(
                        user ->
                                "MEMBER"
                                        .equals(user.getRole())
                )
                .toList();
    }


    /*
     * DASHBOARD
     */
    public long countBooks() {

        return bookRepository.count();
    }


    public long countMembers() {

        return userRepository
                .countByRole("MEMBER");
    }


    public long countActiveLoans() {

        return loanRepository
                .countByStatus("BORROWED");
    }


    public long countAvailableCopies() {

        return bookRepository
                .findAll()
                .stream()
                .mapToLong(
                        Book::getAvailableCopies
                )
                .sum();
    }
}