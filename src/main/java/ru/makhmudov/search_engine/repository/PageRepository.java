package ru.makhmudov.search_engine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.makhmudov.search_engine.entity.Page;

public interface PageRepository extends JpaRepository<Page, Long> {
}
