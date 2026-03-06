package me.basemetrics.controllers;

import me.basemetrics.models.Player;
import me.basemetrics.models.PlayerBio;
import me.basemetrics.repositories.PlayerRepository;
import me.basemetrics.repositories.PlayerBioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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

    @GetMapping("/search")
    public Page<Player> searchPlayers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String position,
            @RequestParam(defaultValue = "0") int page,    // Current page (starts at 0)
            @RequestParam(defaultValue = "80") int size) { // Items per page

        String searchName = (name != null && !name.isBlank()) ? name.trim() : null;
        String searchPos = (position != null && !position.isBlank()) ? position.trim() : null;

        return playerRepository.findByFilters(searchName, searchPos, PageRequest.of(page, size));
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
