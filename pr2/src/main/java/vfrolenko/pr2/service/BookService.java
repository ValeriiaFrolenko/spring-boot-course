package vfrolenko.pr2.service;

import vfrolenko.pr2.dto.book.CreateBookRequest;
import vfrolenko.pr2.dto.book.UpdateBookRequest;
import vfrolenko.pr2.dto.book.UpdateBookStatusRequest;
import vfrolenko.pr2.entity.Book;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookService {
    List<Book> findAll();
    Optional<Book> findById(UUID id);
    Book create(CreateBookRequest request);
    Book update(UUID id, UpdateBookRequest request);
    void delete(UUID id);
    double calculatePrice(UUID id, String strategyName);
    Book updateStatus(UUID id, UpdateBookStatusRequest request);
}