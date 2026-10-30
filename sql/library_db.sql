CREATE DATABASE IF NOT EXISTS library_db;
USE library_db;


DROP TABLE IF EXISTS transactions;
DROP TABLE IF EXISTS members;
DROP TABLE IF EXISTS books;


CREATE TABLE books (
    book_id            INT           PRIMARY KEY,
    title              VARCHAR(150)  NOT NULL,
    author             VARCHAR(100)  NOT NULL,
    category           VARCHAR(50)   NOT NULL,
    quantity           INT           NOT NULL,
    available_quantity INT           NOT NULL,
    CONSTRAINT chk_quantity  CHECK (quantity >= 0),
    CONSTRAINT chk_available CHECK (available_quantity >= 0 AND available_quantity <= quantity)
);

CREATE TABLE members (
    member_id INT           PRIMARY KEY,
    name      VARCHAR(100)  NOT NULL,
    email     VARCHAR(100)  NOT NULL UNIQUE,
    contact   VARCHAR(15)   NOT NULL
);


CREATE TABLE transactions (
    transaction_id INT AUTO_INCREMENT PRIMARY KEY,
    book_id        INT         NOT NULL,
    member_id      INT         NOT NULL,
    issue_date     DATE        NOT NULL,
    due_date       DATE        NOT NULL,
    return_date    DATE        NULL,
    status         VARCHAR(10) NOT NULL DEFAULT 'ISSUED',
    CONSTRAINT fk_txn_book   FOREIGN KEY (book_id)   REFERENCES books(book_id)     ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_txn_member FOREIGN KEY (member_id) REFERENCES members(member_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_status    CHECK (status IN ('ISSUED', 'RETURNED')),
    CONSTRAINT chk_due_date  CHECK (due_date >= issue_date),
    CONSTRAINT chk_return    CHECK (return_date IS NULL OR return_date >= issue_date)
);


INSERT INTO books (book_id, title, author, category, quantity, available_quantity) VALUES
(101, 'Let Us C',                    'Yashavant Kanetkar',   'Programming',      5, 4),
(102, 'Java: The Complete Reference','Herbert Schildt',      'Programming',      4, 4),
(103, 'Database System Concepts',    'Abraham Silberschatz', 'Database',         3, 3),
(104, 'Operating System Concepts',   'Abraham Silberschatz', 'Computer Science', 3, 3),
(105, 'Data Structures Using C',     'Reema Thareja',        'Programming',      4, 4),
(106, 'Wings of Fire',               'A.P.J. Abdul Kalam',   'Biography',        2, 2),
(107, 'The Alchemist',               'Paulo Coelho',         'Fiction',          2, 2),
(108, 'Discrete Mathematics',        'Kenneth Rosen',        'Mathematics',      1, 0);

INSERT INTO members (member_id, name, email, contact) VALUES
(1, 'Amit Sharma',  'amit.sharma@example.com',  '9876543210'),
(2, 'Priya Verma',  'priya.verma@example.com',  '9123456780'),
(3, 'Rahul Patil',  'rahul.patil@example.com',  '9988776655'),
(4, 'Sneha Joshi',  'sneha.joshi@example.com',  '9090909090'),
(5, 'Vikram Singh', 'vikram.singh@example.com', '8899001122');


INSERT INTO transactions (book_id, member_id, issue_date, due_date, return_date, status) VALUES
(101, 1, CURDATE() - INTERVAL 20 DAY, CURDATE() - INTERVAL 6  DAY, NULL,                        'ISSUED'),
(108, 2, CURDATE() - INTERVAL 3  DAY, CURDATE() + INTERVAL 11 DAY, NULL,                        'ISSUED'),
(102, 3, CURDATE() - INTERVAL 25 DAY, CURDATE() - INTERVAL 11 DAY, CURDATE() - INTERVAL 10 DAY, 'RETURNED');
