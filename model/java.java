/*
 * java.java  (model package)
 * ──────────────────────────────────────────────────────────────────────
 * Model classes for LibraryMS.
 *
 * All domain objects are defined as public static nested classes inside
 * this file so the project compiles without adding new source files.
 * (The outer class is named "java" to match the existing filename.)
 * ──────────────────────────────────────────────────────────────────────
 */
package model;

import java.time.LocalDate;

public class java {

    // ── Member ────────────────────────────────────────────────────────
    public static class Member {
        private int    memberId;
        private String firstName;
        private String lastName;
        private String email;
        private String passwordHash;
        private String phoneNumber;
        private String membershipType;   // "student" | "faculty" | "public"
        private String membershipId;     // library card number
        private String role;             // "member" | "admin"
        private LocalDate joinDate;

        public Member() {}

        public int getMemberId()                  { return memberId; }
        public void setMemberId(int memberId)     { this.memberId = memberId; }

        public String getFirstName()                     { return firstName; }
        public void   setFirstName(String firstName)     { this.firstName = firstName; }

        public String getLastName()                      { return lastName; }
        public void   setLastName(String lastName)       { this.lastName = lastName; }

        public String getFullName() {
            return (firstName == null ? "" : firstName) + " " +
                   (lastName  == null ? "" : lastName);
        }

        public String getEmail()                         { return email; }
        public void   setEmail(String email)             { this.email = email; }

        public String getPasswordHash()                      { return passwordHash; }
        public void   setPasswordHash(String passwordHash)   { this.passwordHash = passwordHash; }

        public String getPhoneNumber()                       { return phoneNumber; }
        public void   setPhoneNumber(String phoneNumber)     { this.phoneNumber = phoneNumber; }

        public String getMembershipType()                        { return membershipType; }
        public void   setMembershipType(String membershipType)   { this.membershipType = membershipType; }

        public String getMembershipId()                      { return membershipId; }
        public void   setMembershipId(String membershipId)   { this.membershipId = membershipId; }

        public String getRole()               { return role; }
        public void   setRole(String role)    { this.role = role; }

        public LocalDate getJoinDate()                   { return joinDate; }
        public void      setJoinDate(LocalDate joinDate) { this.joinDate = joinDate; }

        @Override
        public String toString() {
            return "Member{id=" + memberId + ", name=" + getFullName() +
                   ", email=" + email + ", role=" + role + "}";
        }
    }

    // ── Book ──────────────────────────────────────────────────────────
    public static class Book {
        private int    bookId;
        private String title;
        private String author;
        private String isbn;
        private String category;
        private int    totalCopies;
        private int    availableCopies;
        private int    pages;
        private String language;
        private String publisher;
        private int    publishYear;
        private double avgRating;
        private int    totalReviews;

        public Book() {}

        public int    getBookId()                      { return bookId; }
        public void   setBookId(int bookId)            { this.bookId = bookId; }

        public String getTitle()                       { return title; }
        public void   setTitle(String title)           { this.title = title; }

        public String getAuthor()                      { return author; }
        public void   setAuthor(String author)         { this.author = author; }

        public String getIsbn()                        { return isbn; }
        public void   setIsbn(String isbn)             { this.isbn = isbn; }

        public String getCategory()                    { return category; }
        public void   setCategory(String category)     { this.category = category; }

        public int    getTotalCopies()                           { return totalCopies; }
        public void   setTotalCopies(int totalCopies)           { this.totalCopies = totalCopies; }

        public int    getAvailableCopies()                       { return availableCopies; }
        public void   setAvailableCopies(int availableCopies)   { this.availableCopies = availableCopies; }

        public boolean isAvailable() { return availableCopies > 0; }

        public int    getPages()                       { return pages; }
        public void   setPages(int pages)              { this.pages = pages; }

        public String getLanguage()                    { return language; }
        public void   setLanguage(String language)     { this.language = language; }

        public String getPublisher()                   { return publisher; }
        public void   setPublisher(String publisher)   { this.publisher = publisher; }

        public int    getPublishYear()                         { return publishYear; }
        public void   setPublishYear(int publishYear)         { this.publishYear = publishYear; }

        public double getAvgRating()                       { return avgRating; }
        public void   setAvgRating(double avgRating)       { this.avgRating = avgRating; }

        public int    getTotalReviews()                        { return totalReviews; }
        public void   setTotalReviews(int totalReviews)       { this.totalReviews = totalReviews; }

        @Override
        public String toString() {
            return "Book{id=" + bookId + ", title=" + title + ", author=" + author +
                   ", available=" + availableCopies + "/" + totalCopies + "}";
        }
    }

    // ── Borrowing ─────────────────────────────────────────────────────
    public static class Borrowing {
        private int      borrowId;
        private int      memberId;
        private int      bookId;
        private String   bookTitle;   // joined from books table (optional)
        private String   bookAuthor;  // joined from books table (optional)
        private LocalDate borrowDate;
        private LocalDate dueDate;
        private LocalDate returnDate;
        private String    status;      // "active" | "overdue" | "returned" | "paid"
        private double    fineAmount;

        public Borrowing() {}

        public int    getBorrowId()                    { return borrowId; }
        public void   setBorrowId(int borrowId)        { this.borrowId = borrowId; }

        public int    getMemberId()                    { return memberId; }
        public void   setMemberId(int memberId)        { this.memberId = memberId; }

        public int    getBookId()                      { return bookId; }
        public void   setBookId(int bookId)            { this.bookId = bookId; }

        public String getBookTitle()                   { return bookTitle; }
        public void   setBookTitle(String bookTitle)   { this.bookTitle = bookTitle; }

        public String getBookAuthor()                  { return bookAuthor; }
        public void   setBookAuthor(String bookAuthor) { this.bookAuthor = bookAuthor; }

        public LocalDate getBorrowDate()                     { return borrowDate; }
        public void      setBorrowDate(LocalDate borrowDate) { this.borrowDate = borrowDate; }

        public LocalDate getDueDate()                  { return dueDate; }
        public void      setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

        public LocalDate getReturnDate()                       { return returnDate; }
        public void      setReturnDate(LocalDate returnDate)   { this.returnDate = returnDate; }

        public String getStatus()               { return status; }
        public void   setStatus(String status)  { this.status = status; }

        public double getFineAmount()                  { return fineAmount; }
        public void   setFineAmount(double fineAmount) { this.fineAmount = fineAmount; }

        @Override
        public String toString() {
            return "Borrowing{borrowId=" + borrowId + ", bookId=" + bookId +
                   ", status=" + status + ", fine=" + fineAmount + "}";
        }
    }

    // ── Review ────────────────────────────────────────────────────────
    public static class Review {
        private int      reviewId;
        private int      memberId;
        private int      bookId;
        private int      rating;        // 1–5
        private String   comment;
        private LocalDate reviewDate;
        private String   memberName;    // display: "Firstname L." (joined)

        public Review() {}

        public int    getReviewId()                    { return reviewId; }
        public void   setReviewId(int reviewId)        { this.reviewId = reviewId; }

        public int    getMemberId()                    { return memberId; }
        public void   setMemberId(int memberId)        { this.memberId = memberId; }

        public int    getBookId()                      { return bookId; }
        public void   setBookId(int bookId)            { this.bookId = bookId; }

        public int    getRating()                      { return rating; }
        public void   setRating(int rating)            { this.rating = rating; }

        public String getComment()                     { return comment; }
        public void   setComment(String comment)       { this.comment = comment; }

        public LocalDate getReviewDate()                       { return reviewDate; }
        public void      setReviewDate(LocalDate reviewDate)   { this.reviewDate = reviewDate; }

        public String getMemberName()                    { return memberName; }
        public void   setMemberName(String memberName)   { this.memberName = memberName; }

        @Override
        public String toString() {
            return "Review{reviewId=" + reviewId + ", rating=" + rating +
                   ", by=" + memberName + "}";
        }
    }

    // ── WishlistItem ──────────────────────────────────────────────────
    public static class WishlistItem {
        private int      wishlistId;
        private int      memberId;
        private int      bookId;
        private String   bookTitle;   // joined from books table (optional)
        private String   bookAuthor;  // joined from books table (optional)
        private boolean  available;   // joined from books table (optional)
        private LocalDate addedDate;

        public WishlistItem() {}

        public int    getWishlistId()                    { return wishlistId; }
        public void   setWishlistId(int wishlistId)      { this.wishlistId = wishlistId; }

        public int    getMemberId()                      { return memberId; }
        public void   setMemberId(int memberId)          { this.memberId = memberId; }

        public int    getBookId()                        { return bookId; }
        public void   setBookId(int bookId)              { this.bookId = bookId; }

        public String getBookTitle()                     { return bookTitle; }
        public void   setBookTitle(String bookTitle)     { this.bookTitle = bookTitle; }

        public String getBookAuthor()                    { return bookAuthor; }
        public void   setBookAuthor(String bookAuthor)   { this.bookAuthor = bookAuthor; }

        public boolean isAvailable()                     { return available; }
        public void    setAvailable(boolean available)   { this.available = available; }

        public LocalDate getAddedDate()                  { return addedDate; }
        public void      setAddedDate(LocalDate d)       { this.addedDate = d; }

        @Override
        public String toString() {
            return "WishlistItem{bookId=" + bookId + ", title=" + bookTitle + "}";
        }
    }
}
