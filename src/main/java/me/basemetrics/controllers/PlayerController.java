package me.basemetrics.controllers;

import me.basemetrics.models.Player;
import me.basemetrics.models.PlayerBio;
import me.basemetrics.repositories.PlayerRepository;
import me.basemetrics.repositories.PlayerBioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/players")
@CrossOrigin(origins = "*")
public class PlayerController {

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private PlayerBioRepository playerBioRepository;

    @GetMapping("/all")
    public List<Player> getAllPlayers() {
        return playerRepository.findAll();
    }

    @GetMapping("/filter")
    public List<Player> getPlayersByName(@RequestParam String query) {
        return playerRepository.findByNameContainingIgnoreCase(query);
    }

    @GetMapping("/position")
    public List<Player> getPlayersByPosition(@RequestParam String query) {
        return playerRepository.findByPosition(query);
    }

    @GetMapping("/team/{teamId}")
    public List<Player> getByTeam(@PathVariable int teamId) {
        return playerRepository.findByTeamId(teamId);
    }

    @GetMapping("/{id}/bio")
    public PlayerBio getPlayerBio(@PathVariable int id) {
        return playerBioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Player not found"));
    }
}
