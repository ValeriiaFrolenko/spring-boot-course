package vfrolenko.pr2.service.strategy;

import vfrolenko.pr2.entity.Book;

public interface BookPricingStrategy {
    String name();
    double calculatePrice(Book book);
}