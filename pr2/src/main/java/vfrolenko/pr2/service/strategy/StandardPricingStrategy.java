package vfrolenko.pr2.service.strategy;

import org.springframework.stereotype.Component;
import vfrolenko.pr2.entity.Book;

@Component
public class StandardPricingStrategy implements BookPricingStrategy {

    @Override
    public String name() {
        return "STANDARD";
    }

    @Override
    public double calculatePrice(Book book) {
        return book.price();
    }
}