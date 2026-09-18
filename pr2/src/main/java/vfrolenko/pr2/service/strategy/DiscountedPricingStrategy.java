package vfrolenko.pr2.service.strategy;

import org.springframework.stereotype.Component;
import vfrolenko.pr2.entity.Book;

@Component
public class DiscountedPricingStrategy implements BookPricingStrategy {

    private static final double DISCOUNT_RATE = 0.20;

    @Override
    public String name() {
        return "DISCOUNTED";
    }

    @Override
    public double calculatePrice(Book book) {
        return book.price() * (1 - DISCOUNT_RATE);
    }
}