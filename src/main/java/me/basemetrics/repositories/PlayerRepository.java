package me.basemetrics.repositories;

import me.basemetrics.models.Player;
import me.basemetrics.models.PlayerBio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Integer> {
    List<Player> findByTeamId(int teamId);

    List<Player> findByNameContainingIgnoreCase(String name);

    @Query("SELECT p FROM Player p WHERE " +
            "(:name IS NULL OR p.name LIKE %:name%) AND (" +
            "(:pos = 'P' AND (p.position = 'P' OR p.position = 'TWP')) OR " +
            "(:pos = 'NOT_P' AND p.position <> 'P') OR " +
            "(:pos IS NOT NULL AND :pos <> 'P' AND :pos <> 'NOT_P' AND p.position = :pos)" +
            ")")
    Page<Player> findByFilters(@Param("name") String name, @Param("pos") String pos, Pageable pageable);
}