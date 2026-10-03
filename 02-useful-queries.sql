USE library_management;

-- Show all tables
SHOW TABLES;


-- Show all users
SELECT
    id,
    full_name,
    email,
    role
FROM users;


-- Show all books
SELECT
    id,
    title,
    author,
    isbn,
    category,
    total_copies,
    available_copies
FROM books;


-- Show all loans
SELECT
    id,
    user_id,
    book_id,
    borrowed_at,
    due_date,
    returned_at,
    status,
    fine
FROM loans;


-- Show loans with member and book names
SELECT
    l.id,
    u.full_name AS member,
    u.email,
    b.title AS book,
    b.author,
    l.borrowed_at,
    l.due_date,
    l.returned_at,
    l.status,
    l.fine
FROM loans l
JOIN users u
    ON l.user_id = u.id
JOIN books b
    ON l.book_id = b.id
ORDER BY l.id DESC;


-- Search for Java books
SELECT *
FROM books
WHERE title LIKE '%java%'
   OR author LIKE '%java%'
   OR isbn LIKE '%java%';


-- Show currently borrowed books
SELECT
    u.full_name,
    b.title,
    l.borrowed_at,
    l.due_date
FROM loans l
JOIN users u
    ON l.user_id = u.id
JOIN books b
    ON l.book_id = b.id
WHERE l.status = 'BORROWED';


-- Show overdue books
SELECT
    u.full_name,
    b.title,
    l.due_date,
    DATEDIFF(CURDATE(), l.due_date) AS overdue_days
FROM loans l
JOIN users u
    ON l.user_id = u.id
JOIN books b
    ON l.book_id = b.id
WHERE l.status = 'BORROWED'
AND l.due_date < CURDATE();