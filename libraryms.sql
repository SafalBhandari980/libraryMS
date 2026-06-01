-- ═════════════════════════════════════════════════════════════════════════
-- Database Creation for LibraryMS
-- Database Name: libraryms   (NOTE: fixed from 'lybraryms' typo)
-- ═════════════════════════════════════════════════════════════════════════

CREATE DATABASE IF NOT EXISTS libraryms;
USE libraryms;

-- ─────────────────────────────────────────────────────────────────────────
-- 1. Members Table
-- ─────────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS members (
    memberId      INT AUTO_INCREMENT PRIMARY KEY,
    firstName     VARCHAR(100) NOT NULL,
    lastName      VARCHAR(100) NOT NULL,
    email         VARCHAR(150) NOT NULL UNIQUE,
    passwordHash  VARCHAR(255) NOT NULL,
    role          ENUM('member', 'admin') DEFAULT 'member',
    membershipId  VARCHAR(50) UNIQUE,
    membershipType ENUM('student', 'faculty', 'public') NOT NULL,
    phoneNumber   VARCHAR(20),
    joinDate      DATE DEFAULT (CURRENT_DATE),
    isActive      TINYINT(1) NOT NULL DEFAULT 1   -- 1 = active, 0 = deactivated
);

-- ─────────────────────────────────────────────────────────────────────────
-- 2. Books Table
-- ─────────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS books (
    bookId          INT AUTO_INCREMENT PRIMARY KEY,
    title           VARCHAR(255) NOT NULL,
    author          VARCHAR(255) NOT NULL,
    isbn            VARCHAR(20) UNIQUE,
    category        VARCHAR(100),
    description     TEXT,                          -- book synopsis / notes
    totalCopies     INT DEFAULT 1,
    availableCopies INT DEFAULT 1,
    pages           INT,
    language        VARCHAR(50),
    publisher       VARCHAR(150),
    publishYear     INT,
    avgRating       DECIMAL(3, 2) DEFAULT 0.00,
    totalReviews    INT DEFAULT 0
);

-- ─────────────────────────────────────────────────────────────────────────
-- 3. Borrowings Table
-- ─────────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS borrowings (
    borrowId    INT AUTO_INCREMENT PRIMARY KEY,
    memberId    INT NOT NULL,
    bookId      INT NOT NULL,
    borrowDate  DATE DEFAULT (CURRENT_DATE),
    dueDate     DATE,
    returnDate  DATE,
    status      ENUM('active', 'overdue', 'returned', 'paid') DEFAULT 'active',
    fineAmount  DECIMAL(10, 2) DEFAULT 0.00,
    FOREIGN KEY (memberId) REFERENCES members(memberId) ON DELETE CASCADE,
    FOREIGN KEY (bookId)   REFERENCES books(bookId)     ON DELETE CASCADE
);

-- ─────────────────────────────────────────────────────────────────────────
-- 4. Wishlist Table
-- ─────────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS wishlist (
    wishlistId  INT AUTO_INCREMENT PRIMARY KEY,
    memberId    INT NOT NULL,
    bookId      INT NOT NULL,
    addedDate   DATE DEFAULT (CURRENT_DATE),
    FOREIGN KEY (memberId) REFERENCES members(memberId) ON DELETE CASCADE,
    FOREIGN KEY (bookId)   REFERENCES books(bookId)     ON DELETE CASCADE,
    UNIQUE KEY unique_wishlist (memberId, bookId)
);

-- ─────────────────────────────────────────────────────────────────────────
-- 5. Reviews Table
-- ─────────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS reviews (
    reviewId    INT AUTO_INCREMENT PRIMARY KEY,
    memberId    INT NOT NULL,
    bookId      INT NOT NULL,
    rating      INT CHECK (rating >= 1 AND rating <= 5),
    comment     TEXT,
    reviewDate  DATE DEFAULT (CURRENT_DATE),
    FOREIGN KEY (memberId) REFERENCES members(memberId) ON DELETE CASCADE,
    FOREIGN KEY (bookId)   REFERENCES books(bookId)     ON DELETE CASCADE
);

-- ─────────────────────────────────────────────────────────────────────────
-- 6. Reservations Table  (was missing — used extensively in dao/data.java)
-- ─────────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS reservations (
    reservationId  INT AUTO_INCREMENT PRIMARY KEY,
    memberId       INT NOT NULL,
    bookId         INT NOT NULL,
    reservedDate   DATE DEFAULT (CURRENT_DATE),
    expiryDate     DATE,
    status         ENUM('pending', 'fulfilled', 'cancelled', 'expired') DEFAULT 'pending',
    FOREIGN KEY (memberId) REFERENCES members(memberId) ON DELETE CASCADE,
    FOREIGN KEY (bookId)   REFERENCES books(bookId)     ON DELETE CASCADE,
    UNIQUE KEY unique_reservation (memberId, bookId, status)
);

-- ─────────────────────────────────────────────────────────────────────────
-- 7. Activity Log Table  (was missing — used in dao/data.java logActivity())
-- ─────────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS activity_log (
    logId       INT AUTO_INCREMENT PRIMARY KEY,
    memberId    INT,           -- NULL for system/admin actions
    memberName  VARCHAR(200),
    action      VARCHAR(100) NOT NULL,
    details     TEXT,
    logTime     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (memberId) REFERENCES members(memberId) ON DELETE SET NULL
);

-- ═════════════════════════════════════════════════════════════════════════
-- Initial Seed Data (for testing — passwords stored as plain text here
--   since PasswordUtils.hash() now uses SHA-256; update hash below if needed)
-- ═════════════════════════════════════════════════════════════════════════

-- Test member (password = "Admin@123" — SHA-256 hash)
INSERT IGNORE INTO members
    (firstName, lastName, email, passwordHash, role, membershipId, membershipType, phoneNumber, joinDate, isActive)
VALUES
    ('Admin', 'User', 'admin@libraryms.com',
     'a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3',  -- SHA-256("Admin@123")
     'admin', 'ADMIN001', 'student', '9800000001', CURDATE(), 1),

    ('Safal', 'Bhandari', 'safal@example.com',
     'a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3',
     'member', 'LIB2004', 'student', '9841021821', '2024-01-01', 1);

-- Sample books
INSERT IGNORE INTO books (title, author, isbn, category, description, totalCopies, availableCopies, pages, publisher, publishYear)
VALUES
    ('Design Patterns', 'Erich Gamma', '978-0201633610', 'Programming',
     'Classic software design patterns used in object-oriented design.', 5, 4, 395, 'Addison-Wesley', 1994),

    ('Clean Code', 'Robert C. Martin', '978-0132350884', 'Programming',
     'A handbook of agile software craftsmanship.', 3, 3, 431, 'Prentice Hall', 2008),

    ('The Pragmatic Programmer', 'Andrew Hunt', '978-0201616224', 'Programming',
     'Your journey to mastery in software development.', 2, 0, 352, 'Addison-Wesley', 1999),

    ('Introduction to Algorithms', 'Thomas H. Cormen', '978-0262033848', 'Computer Science',
     'A comprehensive introduction to modern algorithms.', 4, 4, 1312, 'MIT Press', 2009);

-- Sample borrowing record (Safal has Design Patterns overdue)
INSERT IGNORE INTO borrowings (memberId, bookId, borrowDate, dueDate, status)
SELECT m.memberId, b.bookId, '2024-03-01', '2024-03-15', 'overdue'
FROM members m, books b
WHERE m.email = 'safal@example.com' AND b.isbn = '978-0201633610'
LIMIT 1;

-- Sample wishlist
INSERT IGNORE INTO wishlist (memberId, bookId)
SELECT m.memberId, b.bookId
FROM members m, books b
WHERE m.email = 'safal@example.com' AND b.isbn = '978-0201616224'
LIMIT 1;
