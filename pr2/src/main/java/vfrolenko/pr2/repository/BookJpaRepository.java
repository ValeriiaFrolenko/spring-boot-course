package vfrolenko.pr2.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vfrolenko.pr2.entity.BookJpaEntity;

import java.util.List;
import java.util.UUID;

public interface BookJpaRepository extends JpaRepository<BookJpaEntity, UUID> {

    boolean existsByTitleIgnoreCaseAndAuthorIgnoreCase(String title, String author);

    List<BookJpaEntity> findByTitleContainingIgnoreCase(String fragment);
}