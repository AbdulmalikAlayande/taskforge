package app.bola.taskforge.repository;

import app.bola.taskforge.domain.entity.History;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoryRepository extends JpaRepository<History, String> {
}
