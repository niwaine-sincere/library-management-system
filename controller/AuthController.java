package com.librarymanagement.controller;

import com.librarymanagement.entity.User;
import com.librarymanagement.service.LibraryService;

import jakarta.servlet.http.HttpSession;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.springframework.http.HttpStatus;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final LibraryService service;


    public AuthController(
            LibraryService service
    ) {

        this.service = service;
    }


    /*
     * LOGIN REQUEST
     */
    record LoginRequest(

            @Email
            @NotBlank
            String email,

            @NotBlank
            String password

    ) {
    }


    /*
     * REGISTRATION REQUEST
     */
    record RegisterRequest(

            @NotBlank
            String fullName,

            @Email
            @NotBlank
            String email,

            @NotBlank
            @Size(min = 6)
            String password

    ) {
    }


    /*
     * LOGIN
     */
    @PostMapping("/login")
    public User login(
            @Valid
            @RequestBody
            LoginRequest request,

            HttpSession session
    ) {

        User user =
                service.login(
                        request.email(),
                        request.password()
                );


        session.setAttribute(
                "userId",
                user.getId()
        );


        session.setAttribute(
                "role",
                user.getRole()
        );


        return user;
    }


    /*
     * REGISTER
     */
    @PostMapping("/register")
    @ResponseStatus(
            HttpStatus.CREATED
    )
    public User register(
            @Valid
            @RequestBody
            RegisterRequest request
    ) {

        return service.registerMember(
                request.fullName(),
                request.email(),
                request.password()
        );
    }


    /*
     * LOGOUT
     */
    @PostMapping("/logout")
    public Map<String, String> logout(
            HttpSession session
    ) {

        session.invalidate();


        return Map.of(
                "message",
                "Logged out successfully."
        );
    }


    /*
     * CURRENT USER
     */
    @GetMapping("/me")
    public User me(
            HttpSession session
    ) {

        Long userId =
                requireLogin(session);


        return service.userRepositoryFind(
                userId
        );
    }


    /*
     * CHECK LOGIN
     */
    static Long requireLogin(
            HttpSession session
    ) {

        Object id =
                session.getAttribute(
                        "userId"
                );


        if (id == null) {

            throw new ApiException(
                    HttpStatus.UNAUTHORIZED,
                    "Please log in."
            );
        }


        return (Long) id;
    }
}