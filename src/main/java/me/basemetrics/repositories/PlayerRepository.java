package me.basemetrics.repositories;

import me.basemetrics.models.Player;
import me.basemetrics.models.PlayerBio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Integer> {
    List<Player> findByTeamId(int teamId);

    List<Player> findByName(String name);

    List<Player> findByNameContainingIgnoreCase(String name);

    List<Player> findByPosition(String position);

}