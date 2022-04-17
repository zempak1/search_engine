package ru.makhmudov.search_engine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.makhmudov.search_engine.entity.Field;

public interface FieldRepository extends JpaRepository<Field, Long> {
}
