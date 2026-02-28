package me.basemetrics.repositories;

import me.basemetrics.models.PlayerBio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerBioRepository extends JpaRepository<PlayerBio, Integer> {
}