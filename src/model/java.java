/*
 * model/java.java  –  LibraryMS Model Classes
 * ─────────────────────────────────────────────────────────────────────────
 * Contains all POJO (Plain Old Java Object) model classes used throughout
 * the application.  Each class is a simple data-holder with getters/setters.
 *
 * Classes defined here (referenced as model.java.XXX):
 *   • Member        – a registered library member
 *   • Book          – a book in the library catalogue
 *   • Borrowing     – a single borrow/return record
 *   • WishlistItem  – one entry in a member's wishlist
 *   • Review        – a member review of a book
 * ─────────────────────────────────────────────────────────────────────────
 */
package model;

import java.time.LocalDate;

/**
 * Outer container class — keeps the filename 'java.java' so existing imports
 * such as  import model.java.Member;  continue to resolve correctly.
 */
public class java {

    // ══════════════════════════════════════════════════════════════════════
    //  Member
    // ══════════════════════════════════════════════════════════════════════
    public static class Member {

        private int       memberId;
        private String    firstName;
        private String    lastName;
        private String    email;
        private String    passwordHash;
        private String    role;           // "member" | "admin"
        private String    membershipId;
        private String    membershipType; // "student" | "faculty" | "public"
        private String    phoneNumber;
        private LocalDate joinDate;
        private boolean   active = true;

        public Member() {}

        // ── Getters ──────────────────────────────────────────────────────
        public int       getMemberId()       { return memberId; }
        public String    getFirstName()      { return firstName; }
        public String    getLastName()       { return lastName; }
        public String    getEmail()          { return email; }
        public String    getPasswordHash()   { return passwordHash; }
        public String    getRole()           { return role; }
        public String    getMembershipId()   { return membershipId; }
        public String    getMembershipType() { return membershipType; }
        public String    getPhoneNumber()    { return phoneNumber; }
        public LocalDate getJoinDate()       { return joinDate; }
        public boolean   isActive()          { return active; }

        /** Convenience method: "FirstName LastName" */
        public String getFullName() {
            String f = firstName == null ? "" : firstName.trim();
            String l = lastName  == null ? "" : lastName.trim();
            return (f + " " + l).trim();
        }

        // ── Setters ──────────────────────────────────────────────────────
        public void setMemberId(int memberId)             { this.memberId = memberId; }
        public void setFirstName(String firstName)        { this.firstName = firstName; }
        public void setLastName(String lastName)          { this.lastName = lastName; }
        public void setEmail(String email)                { this.email = email; }
        public void setPasswordHash(String passwordHash)  { this.passwordHash = passwordHash; }
        public void setRole(String role)                  { this.role = role; }
        public void setMembershipId(String membershipId)  { this.membershipId = membershipId; }
        public void setMembershipType(String type)        { this.membershipType = type; }
        public void setPhoneNumber(String phoneNumber)    { this.phoneNumber = phoneNumber; }
        public void setJoinDate(LocalDate joinDate)       { this.joinDate = joinDate; }
        public void setActive(boolean active)             { this.active = active; }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  Book
    // ══════════════════════════════════════════════════════════════════════
    public static class Book {

        private int    bookId;
        private String title;
        private String author;
        private String isbn;
        private String category;
        private String description;
        private int    totalCopies;
        private int    availableCopies;
        private int    pages;
        private String language;
        private String publisher;
        private int    publishYear;
        private double avgRating;
        private int    totalReviews;

        public Book() {}

        // ── Getters ──────────────────────────────────────────────────────
        public int    getBookId()          { return bookId; }
        public String getTitle()           { return title; }
        public String getAuthor()          { return author; }
        public String getIsbn()            { return isbn; }
        public String getCategory()        { return category; }
        public String getDescription()     { return description; }
        public int    getTotalCopies()     { return totalCopies; }
        public int    getAvailableCopies() { return availableCopies; }
        public int    getPages()           { return pages; }
        public String getLanguage()        { return language; }
        public String getPublisher()       { return publisher; }
        public int    getPublishYear()     { return publishYear; }
        public double getAvgRating()       { return avgRating; }
        public int    getTotalReviews()    { return totalReviews; }

        public boolean isAvailable()       { return availableCopies > 0; }

        // ── Setters ──────────────────────────────────────────────────────
        public void setBookId(int bookId)                    { this.bookId = bookId; }
        public void setTitle(String title)                   { this.title = title; }
        public void setAuthor(String author)                 { this.author = author; }
        public void setIsbn(String isbn)                     { this.isbn = isbn; }
        public void setCategory(String category)             { this.category = category; }
        public void setDescription(String description)       { this.description = description; }
        public void setTotalCopies(int totalCopies)          { this.totalCopies = totalCopies; }
        public void setAvailableCopies(int availableCopies)  { this.availableCopies = availableCopies; }
        public void setPages(int pages)                      { this.pages = pages; }
        public void setLanguage(String language)             { this.language = language; }
        public void setPublisher(String publisher)           { this.publisher = publisher; }
        public void setPublishYear(int publishYear)          { this.publishYear = publishYear; }
        public void setAvgRating(double avgRating)           { this.avgRating = avgRating; }
        public void setTotalReviews(int totalReviews)        { this.totalReviews = totalReviews; }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  Borrowing
    // ══════════════════════════════════════════════════════════════════════
    public static class Borrowing {

        private int       borrowId;
        private int       memberId;
        private int       bookId;
        private String    bookTitle;   // joined from books table
        private String    bookAuthor;  // joined from books table
        private LocalDate borrowDate;
        private LocalDate dueDate;
        private LocalDate returnDate;  // null if not yet returned
        private String    status;      // "active" | "overdue" | "returned" | "paid"
        private double    fineAmount;

        public Borrowing() {}

        // ── Getters ──────────────────────────────────────────────────────
        public int       getBorrowId()   { return borrowId; }
        public int       getMemberId()   { return memberId; }
        public int       getBookId()     { return bookId; }
        public String    getBookTitle()  { return bookTitle; }
        public String    getBookAuthor() { return bookAuthor; }
        public LocalDate getBorrowDate() { return borrowDate; }
        public LocalDate getDueDate()    { return dueDate; }
        public LocalDate getReturnDate() { return returnDate; }
        public String    getStatus()     { return status; }
        public double    getFineAmount() { return fineAmount; }

        // ── Setters ──────────────────────────────────────────────────────
        public void setBorrowId(int borrowId)          { this.borrowId = borrowId; }
        public void setMemberId(int memberId)          { this.memberId = memberId; }
        public void setBookId(int bookId)              { this.bookId = bookId; }
        public void setBookTitle(String bookTitle)     { this.bookTitle = bookTitle; }
        public void setBookAuthor(String bookAuthor)   { this.bookAuthor = bookAuthor; }
        public void setBorrowDate(LocalDate borrowDate){ this.borrowDate = borrowDate; }
        public void setDueDate(LocalDate dueDate)      { this.dueDate = dueDate; }
        public void setReturnDate(LocalDate returnDate){ this.returnDate = returnDate; }
        public void setStatus(String status)           { this.status = status; }
        public void setFineAmount(double fineAmount)   { this.fineAmount = fineAmount; }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  WishlistItem
    // ══════════════════════════════════════════════════════════════════════
    public static class WishlistItem {

        private int       wishlistId;
        private int       memberId;
        private int       bookId;
        private String    bookTitle;   // joined from books table
        private String    bookAuthor;  // joined from books table
        private boolean   available;   // availableCopies > 0
        private LocalDate addedDate;

        public WishlistItem() {}

        // ── Getters ──────────────────────────────────────────────────────
        public int       getWishlistId() { return wishlistId; }
        public int       getMemberId()   { return memberId; }
        public int       getBookId()     { return bookId; }
        public String    getBookTitle()  { return bookTitle; }
        public String    getBookAuthor() { return bookAuthor; }
        public boolean   isAvailable()   { return available; }
        public LocalDate getAddedDate()  { return addedDate; }

        // ── Setters ──────────────────────────────────────────────────────
        public void setWishlistId(int wishlistId)      { this.wishlistId = wishlistId; }
        public void setMemberId(int memberId)          { this.memberId = memberId; }
        public void setBookId(int bookId)              { this.bookId = bookId; }
        public void setBookTitle(String bookTitle)     { this.bookTitle = bookTitle; }
        public void setBookAuthor(String bookAuthor)   { this.bookAuthor = bookAuthor; }
        public void setAvailable(boolean available)    { this.available = available; }
        public void setAddedDate(LocalDate addedDate)  { this.addedDate = addedDate; }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  Review
    // ══════════════════════════════════════════════════════════════════════
    public static class Review {

        private int       reviewId;
        private int       memberId;
        private int       bookId;
        private int       rating;
        private String    comment;
        private LocalDate reviewDate;
        private String    memberName;
        private String    bookTitle;

        public Review() {}

        public int       getReviewId()   { return reviewId; }
        public int       getMemberId()   { return memberId; }
        public int       getBookId()     { return bookId; }
        public int       getRating()     { return rating; }
        public String    getComment()    { return comment; }
        public LocalDate getReviewDate() { return reviewDate; }
        public String    getMemberName() { return memberName; }
        public String    getBookTitle()  { return bookTitle; }

        public void setReviewId(int reviewId)          { this.reviewId = reviewId; }
        public void setMemberId(int memberId)          { this.memberId = memberId; }
        public void setBookId(int bookId)              { this.bookId = bookId; }
        public void setRating(int rating)              { this.rating = rating; }
        public void setComment(String comment)         { this.comment = comment; }
        public void setReviewDate(LocalDate reviewDate){ this.reviewDate = reviewDate; }
        public void setMemberName(String memberName)   { this.memberName = memberName; }
        public void setBookTitle(String bookTitle)     { this.bookTitle = bookTitle; }
    }
}