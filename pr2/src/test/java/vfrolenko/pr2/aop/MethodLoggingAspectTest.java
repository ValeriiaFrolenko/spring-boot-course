package vfrolenko.pr2.aop;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import vfrolenko.pr2.service.BookService;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class MethodLoggingAspectTest {

    @Autowired
    private BookService bookService;

    private ListAppender<ILoggingEvent> listAppender;
    private Logger aspectLogger;

    @BeforeEach
    void setUp() {
        aspectLogger = (Logger) LoggerFactory.getLogger(MethodLoggingAspect.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        aspectLogger.addAppender(listAppender);
    }

    @AfterEach
    void tearDown() {
        aspectLogger.detachAppender(listAppender);
    }

    @Test
    void findAll_shouldBeInterceptedByAspectAndLogExecution() {
        bookService.findAll();

        assertThat(listAppender.list)
                .isNotEmpty()
                .anyMatch(event -> event.getFormattedMessage().contains("-> [findAllBooks] args=[]"))
                .anyMatch(event -> event.getFormattedMessage().contains("<- [findAllBooks] completed in"));
    }
}