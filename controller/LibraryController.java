package com.librarymanagement.controller;

import com.librarymanagement.entity.Book;
import com.librarymanagement.entity.User;

import com.librarymanagement.service.LibraryService;
import com.librarymanagement.service.LoanResponse;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class LibraryController {

    private final LibraryService service;


    public LibraryController(
            LibraryService service
    ) {

        this.service = service;
    }


    /*
     * ADMIN CHECK
     */
    private void requireAdmin(
            HttpSession session
    ) {

        AuthController.requireLogin(
                session
        );


        if (
                !"ADMIN".equals(
                        session.getAttribute("role")
                )
        ) {

            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "Administrator access required."
            );
        }
    }


    /*
     * SEARCH BOOKS
     */
    @GetMapping("/books")
    public List<Book> books(
            @RequestParam(
                    defaultValue = ""
            )
            String q
    ) {

        return service.searchBooks(q);
    }


    /*
     * ADD BOOK
     */
    @PostMapping("/books")
    @ResponseStatus(
            HttpStatus.CREATED
    )
    public Book addBook(
            @RequestBody Book book,
            HttpSession session
    ) {

        requireAdmin(session);


        return service.addBook(
                book
        );
    }


    /*
     * MEMBERS
     */
    @GetMapping("/members")
    public List<User> members(
            HttpSession session
    ) {

        requireAdmin(session);


        return service.members();
    }


    /*
     * BORROW REQUEST
     */
    record BorrowRequest(
            Long userId,
            Long bookId
    ) {
    }


    /*
     * BORROW BOOK
     */
    @PostMapping("/loans/borrow")
    @ResponseStatus(
            HttpStatus.CREATED
    )
    public LoanResponse borrow(
            @RequestBody BorrowRequest request,
            HttpSession session
    ) {

        Long loggedIn =
                AuthController.requireLogin(
                        session
                );


        Long userId;


        if (
                "ADMIN".equals(
                        session.getAttribute("role")
                )
        ) {

            userId = request.userId();

        } else {

            userId = loggedIn;
        }


        if (
                userId == null ||
                request.bookId() == null
        ) {

            throw new IllegalArgumentException(
                    "Member and book are required."
            );
        }


        return service.borrowBook(
                userId,
                request.bookId()
        );
    }


    /*
     * RETURN BOOK
     */
    @PostMapping("/loans/{id}/return")
    public LoanResponse returnBook(
            @PathVariable Long id,
            HttpSession session
    ) {

        Long requester =
                AuthController.requireLogin(
                        session
                );


        boolean admin =
                "ADMIN".equals(
                        session.getAttribute("role")
                );


        return service.returnBook(
                id,
                requester,
                admin
        );
    }


    /*
     * GET LOANS
     */
    @GetMapping("/loans")
    public List<LoanResponse> loans(
            HttpSession session
    ) {

        Long userId =
                AuthController.requireLogin(
                        session
                );


        if (
                "ADMIN".equals(
                        session.getAttribute("role")
                )
        ) {

            return service.allLoans();
        }


        return service.memberLoans(
                userId
        );
    }


    /*
     * DASHBOARD
     */
    @GetMapping("/dashboard")
    public Map<String, Long> dashboard(
            HttpSession session
    ) {

        AuthController.requireLogin(
                session
        );


        return Map.of(

                "books",
                service.countBooks(),

                "members",
                service.countMembers(),

                "activeLoans",
                service.countActiveLoans(),

                "availableCopies",
                service.countAvailableCopies()
        );
    }
}
