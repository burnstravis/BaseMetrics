package me.basemetrics.repositories;

import me.basemetrics.models.LiveGame;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LiveGameRepository extends JpaRepository<LiveGame, Integer> {

}