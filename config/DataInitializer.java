package com.librarymanagement.config;

import com.librarymanagement.entity.Book;
import com.librarymanagement.entity.User;

import com.librarymanagement.repository.BookRepository;
import com.librarymanagement.repository.UserRepository;

import org.springframework.boot.CommandLineRunner;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class DataInitializer {


    @Bean
    CommandLineRunner seed(
            UserRepository users,
            BookRepository books
    ) {

        return args -> {

            BCryptPasswordEncoder encoder =
                    new BCryptPasswordEncoder();


            /*
             * CREATE ADMIN
             */
            if (
                    users
                            .findByEmailIgnoreCase(
                                    "admin@library.com"
                            )
                            .isEmpty()
            ) {

                users.save(

                        new User(

                                "System Administrator",

                                "admin@library.com",

                                encoder.encode(
                                        "Admin@123"
                                ),

                                "ADMIN"
                        )
                );
            }


            /*
             * CREATE DEMO MEMBER
             */
            if (
                    users
                            .findByEmailIgnoreCase(
                                    "member@library.com"
                            )
                            .isEmpty()
            ) {

                users.save(

                        new User(

                                "Demo Member",

                                "member@library.com",

                                encoder.encode(
                                        "Member@123"
                                ),

                                "MEMBER"
                        )
                );
            }


            /*
             * CREATE DEMO BOOKS
             */
            if (
                    books.count() == 0
            ) {

                books.save(

                        new Book(
                                "Effective Java",
                                "Joshua Bloch",
                                "9780134685991",
                                "Programming",
                                3
                        )
                );


                books.save(

                        new Book(
                                "Clean Code",
                                "Robert C. Martin",
                                "9780132350884",
                                "Programming",
                                2
                        )
                );


                books.save(

                        new Book(
                                "Database System Concepts",
                                "Abraham Silberschatz",
                                "9780078022159",
                                "Database",
                                2
                        )
                );


                books.save(

                        new Book(
                                "Head First Java",
                                "Kathy Sierra",
                                "9780596009205",
                                "Java",
                                4
                        )
                );
            }
        };
    }
}