package me.basemetrics.repositories;

import me.basemetrics.models.LiveGame;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LiveGameRepository extends JpaRepository<LiveGame, Integer> {

    @Modifying
    @Query("DELETE FROM LiveGame g WHERE g.last_updated < :cutoff")
    void deleteOldGames(@Param("cutoff") LocalDateTime cutoff);

}