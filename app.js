let currentUser = null;

let books = [];



/*
====================================================
GET HTML ELEMENT
====================================================
*/

const $ = id =>
    document.getElementById(id);



/*
====================================================
TOAST MESSAGE
====================================================
*/

function toast(
    message,
    error = false
) {

    const element =
        document.createElement("div");


    element.className =
        "toast" +
        (
            error
                ? " error"
                : ""
        );


    element.textContent =
        message;


    $("toast")
        .appendChild(element);


    setTimeout(
        () => element.remove(),
        3500
    );
}



/*
====================================================
API FUNCTION
====================================================
*/

async function api(
    url,
    options = {}
) {

    const response =
        await fetch(
            url,
            {

                headers: {

                    "Content-Type":
                        "application/json",

                    ...(options.headers || {})

                },

                ...options

            }
        );


    const data =
        await response
            .json()
            .catch(
                () => ({})
            );


    if (!response.ok) {

        throw new Error(
            data.error ||
            "Request failed"
        );
    }


    return data;
}



/*
====================================================
START APPLICATION
====================================================
*/

async function start() {

    try {

        currentUser =
            await api(
                "/api/auth/me"
            );


        showApp();

    } catch (error) {

        showLogin();

    }
}



/*
====================================================
SHOW LOGIN
====================================================
*/

function showLogin() {

    $("loginPage")
        .classList
        .remove("hidden");


    $("appPage")
        .classList
        .add("hidden");
}



/*
====================================================
SHOW APPLICATION
====================================================
*/

async function showApp() {

    $("loginPage")
        .classList
        .add("hidden");


    $("appPage")
        .classList
        .remove("hidden");


    $("welcome")
        .textContent =
        `${currentUser.fullName} (${currentUser.role})`;


    /*
     * Only administrators
     * see Administration.
     */

    $("adminTabButton")
        .classList
        .toggle(
            "hidden",
            currentUser.role !== "ADMIN"
        );


    await loadDashboard();

    await loadBooks();

    await loadLoans();


    if (
        currentUser.role === "ADMIN"
    ) {

        await loadAdminData();

    }
}



/*
====================================================
LOAD DASHBOARD
====================================================
*/

async function loadDashboard() {

    const data =
        await api(
            "/api/dashboard"
        );


    $("statBooks")
        .textContent =
        data.books;


    $("statMembers")
        .textContent =
        data.members;


    $("statLoans")
        .textContent =
        data.activeLoans;


    $("statAvailable")
        .textContent =
        data.availableCopies;
}



/*
====================================================
LOAD BOOKS
====================================================
*/

async function loadBooks(
    q = ""
) {

    books =
        await api(
            "/api/books?q=" +
            encodeURIComponent(q)
        );


    $("booksTable")
        .innerHTML =
        books
            .map(
                book => `

<tr>

    <td>
        ${book.id}
    </td>


    <td>
        ${escapeHtml(book.title)}
    </td>


    <td>
        ${escapeHtml(book.author)}
    </td>


    <td>
        ${escapeHtml(
            book.isbn || ""
        )}
    </td>


    <td>
        ${escapeHtml(
            book.category || ""
        )}
    </td>


    <td>
        ${book.totalCopies}
    </td>


    <td>
        ${book.availableCopies}
    </td>


    <td>

        ${
            book.availableCopies > 0

            ?

            `<button
                onclick="borrowFromSearch(${book.id})"
            >
                Borrow
            </button>`

            :

            `<span>
                Unavailable
            </span>`
        }

    </td>

</tr>

`
            )
            .join("");


    if (
        currentUser?.role === "ADMIN"
    ) {

        fillBookSelect();

    }
}



/*
====================================================
BORROW FROM SEARCH
====================================================
*/

async function borrowFromSearch(
    bookId
) {

    /*
     * Administrator is sent
     * to administration page.
     */

    if (
        currentUser.role === "ADMIN"
    ) {

        document
            .querySelector(
                '[data-tab="adminTab"]'
            )
            .click();


        $("bookSelect")
            .value =
            bookId;


        return;
    }


    try {

        await api(
            "/api/loans/borrow",
            {

                method: "POST",

                body: JSON.stringify({

                    userId:
                        currentUser.id,

                    bookId:
                        bookId

                })

            }
        );


        toast(
            "Book borrowed successfully."
        );


        await refreshAll();


    } catch (error) {

        toast(
            error.message,
            true
        );
    }
}



/*
====================================================
LOAD LOANS
====================================================
*/

async function loadLoans() {

    const loans =
        await api(
            "/api/loans"
        );


    /*
     * MEMBER LOANS
     */

    $("loansTable")
        .innerHTML =

        loans
            .map(
                loan => `

<tr>

    <td>
        ${loan.id}
    </td>


    <td>
        ${escapeHtml(
            loan.bookTitle
        )}
    </td>


    <td>
        ${formatDate(
            loan.borrowedAt
        )}
    </td>


    <td>
        ${loan.dueDate}
    </td>


    <td>
        ${
            loan.returnedAt
                ? formatDate(
                    loan.returnedAt
                )
                : "-"
        }
    </td>


    <td>
        ${loan.status}
    </td>


    <td>
        ${Number(
            loan.fine
        ).toLocaleString()}
    </td>


    <td>

        ${
            loan.status === "BORROWED"

            ?

            `<button
                onclick="returnLoan(${loan.id})"
            >
                Return
            </button>`

            :

            "-"
        }

    </td>

</tr>

`
            )
            .join("")

        ||

        `
        <tr>
            <td colspan="8">
                No loans found.
            </td>
        </tr>
        `;


    /*
     * ADMIN LOANS
     */

    if (
        currentUser.role === "ADMIN"
    ) {

        $("allLoansTable")
            .innerHTML =

            loans
                .map(
                    loan => `

<tr>

    <td>
        ${loan.id}
    </td>


    <td>
        ${escapeHtml(
            loan.memberName
        )}
    </td>


    <td>
        ${escapeHtml(
            loan.bookTitle
        )}
    </td>


    <td>
        ${formatDate(
            loan.borrowedAt
        )}
    </td>


    <td>
        ${loan.dueDate}
    </td>


    <td>
        ${loan.status}
    </td>


    <td>
        ${Number(
            loan.fine
        ).toLocaleString()}
    </td>


    <td>

        ${
            loan.status === "BORROWED"

            ?

            `<button
                onclick="returnLoan(${loan.id})"
            >
                Return
            </button>`

            :

            "-"
        }

    </td>

</tr>

`
                )
                .join("");
    }
}



/*
====================================================
RETURN BOOK
====================================================
*/

async function returnLoan(
    id
) {

    try {

        await api(

            `/api/loans/${id}/return`,

            {
                method: "POST"
            }

        );


        toast(
            "Book returned successfully."
        );


        await refreshAll();


    } catch (error) {

        toast(
            error.message,
            true
        );
    }
}



/*
====================================================
LOAD ADMIN DATA
====================================================
*/

async function loadAdminData() {

    const members =
        await api(
            "/api/members"
        );


    $("memberSelect")
        .innerHTML =

        members
            .map(
                member => `

<option value="${member.id}">

    ${escapeHtml(
        member.fullName
    )}

    —

    ${escapeHtml(
        member.email
    )}

</option>

`
            )
            .join("");


    fillBookSelect();
}



/*
====================================================
FILL BOOK SELECT
====================================================
*/

function fillBookSelect() {

    $("bookSelect")
        .innerHTML =

        books

            .filter(
                book =>
                    book.availableCopies > 0
            )

            .map(
                book => `

<option
    value="${book.id}"
>

    ${escapeHtml(
        book.title
    )}

    (
        ${book.availableCopies}
        available
    )

</option>

`
            )

            .join("");
}



/*
====================================================
REFRESH EVERYTHING
====================================================
*/

async function refreshAll() {

    await loadDashboard();


    await loadBooks(
        $("searchBox").value
    );


    await loadLoans();


    if (
        currentUser.role === "ADMIN"
    ) {

        await loadAdminData();

    }
}



/*
====================================================
DATE FORMAT
====================================================
*/

function formatDate(
    value
) {

    return value
        ? new Date(value)
            .toLocaleString()

        : "-";
}



/*
====================================================
SECURITY:
ESCAPE HTML
====================================================
*/

function escapeHtml(
    value
) {

    return String(
        value ?? ""
    )
        .replace(
            /[&<>'"]/g,

            character => ({

                "&":
                    "&amp;",

                "<":
                    "&lt;",

                ">":
                    "&gt;",

                "'":
                    "&#39;",

                '"':
                    "&quot;"

            }[character])
        );
}



/*
====================================================
LOGIN FORM
====================================================
*/

$("loginForm")
    .addEventListener(
        "submit",

        async event => {

            event.preventDefault();


            try {

                currentUser =
                    await api(

                        "/api/auth/login",

                        {

                            method: "POST",

                            body:
                                JSON.stringify({

                                    email:
                                        $("loginEmail")
                                            .value,

                                    password:
                                        $("loginPassword")
                                            .value

                                })

                        }
                    );


                event.target.reset();


                showApp();


                toast(
                    "Login successful."
                );


            } catch (error) {

                toast(
                    error.message,
                    true
                );
            }
        }
    );



/*
====================================================
REGISTRATION FORM
====================================================
*/

$("registerForm")
    .addEventListener(
        "submit",

        async event => {

            event.preventDefault();


            try {

                await api(

                    "/api/auth/register",

                    {

                        method: "POST",

                        body:
                            JSON.stringify({

                                fullName:
                                    $("regName")
                                        .value,

                                email:
                                    $("regEmail")
                                        .value,

                                password:
                                    $("regPassword")
                                        .value

                            })

                    }
                );


                event.target.reset();


                toast(
                    "Registration successful. You can now log in."
                );


            } catch (error) {

                toast(
                    error.message,
                    true
                );
            }
        }
    );



/*
====================================================
LOGOUT
====================================================
*/

$("logoutBtn")
    .addEventListener(
        "click",

        async () => {

            await api(
                "/api/auth/logout",
                {
                    method: "POST"
                }
            );


            currentUser = null;


            showLogin();

        }
    );



/*
====================================================
SEARCH BUTTON
====================================================
*/

$("searchBtn")
    .addEventListener(
        "click",

        () =>
            loadBooks(
                $("searchBox").value
            )
    );



/*
====================================================
SEARCH WITH ENTER
====================================================
*/

$("searchBox")
    .addEventListener(
        "keydown",

        event => {

            if (
                event.key === "Enter"
            ) {

                loadBooks(
                    event.target.value
                );
            }
        }
    );



/*
====================================================
ADD BOOK
====================================================
*/

$("bookForm")
    .addEventListener(
        "submit",

        async event => {

            event.preventDefault();


            try {

                await api(

                    "/api/books",

                    {

                        method: "POST",

                        body:
                            JSON.stringify({

                                title:
                                    $("bookTitle")
                                        .value,

                                author:
                                    $("bookAuthor")
                                        .value,

                                isbn:
                                    $("bookIsbn")
                                        .value,

                                category:
                                    $("bookCategory")
                                        .value,

                                totalCopies:
                                    Number(
                                        $("bookCopies")
                                            .value
                                    )

                            })

                    }
                );


                event.target.reset();


                $("bookCopies")
                    .value = 1;


                toast(
                    "Book added successfully."
                );


                await refreshAll();


            } catch (error) {

                toast(
                    error.message,
                    true
                );
            }
        }
    );



/*
====================================================
ADMIN BORROW BOOK
====================================================
*/

$("borrowForm")
    .addEventListener(
        "submit",

        async event => {

            event.preventDefault();


            try {

                await api(

                    "/api/loans/borrow",

                    {

                        method: "POST",

                        body:
                            JSON.stringify({

                                userId:
                                    Number(
                                        $("memberSelect")
                                            .value
                                    ),

                                bookId:
                                    Number(
                                        $("bookSelect")
                                            .value
                                    )

                            })

                    }
                );


                toast(
                    "Book borrowed."
                );


                await refreshAll();


            } catch (error) {

                toast(
                    error.message,
                    true
                );
            }
        }
    );



/*
====================================================
TAB NAVIGATION
====================================================
*/

document
    .querySelectorAll(
        ".tabs button"
    )
    .forEach(
        button => {

            button.addEventListener(
                "click",

                () => {

                    /*
                     * Remove active
                     * from all buttons.
                     */

                    document
                        .querySelectorAll(
                            ".tabs button"
                        )
                        .forEach(
                            item =>
                                item.classList
                                    .remove(
                                        "active"
                                    )
                        );


                    /*
                     * Hide all sections.
                     */

                    document
                        .querySelectorAll(
                            ".tab-content"
                        )
                        .forEach(
                            section =>
                                section.classList
                                    .add(
                                        "hidden"
                                    )
                        );


                    /*
                     * Activate clicked button.
                     */

                    button.classList
                        .add(
                            "active"
                        );


                    /*
                     * Show selected section.
                     */

                    $(
                        button.dataset.tab
                    )
                        .classList
                        .remove(
                            "hidden"
                        );
                }
            );
        }
    );



/*
====================================================
START
====================================================
*/

start();