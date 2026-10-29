package vfrolenko.pr2.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vfrolenko.pr2.dto.book.BookResponse;
import vfrolenko.pr2.service.BookService;

import java.util.List;
import java.util.function.Function;

@Configuration(proxyBeanMethods = false)
public class FunctionConfig {

    private static final Logger log = LoggerFactory.getLogger(FunctionConfig.class);

    private final BookService bookService;

    public FunctionConfig(BookService bookService) {
        this.bookService = bookService;
    }

    @Bean
    public Function<String, List<BookResponse>> searchBooksFunction() {
        return searchQuery -> {
            log.info("Received external request via Ngrok to search for books: {}", searchQuery);
            return bookService.searchByName(searchQuery).stream()
                    .map(BookResponse::from)
                    .toList();
        };
    }
}