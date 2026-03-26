package me.basemetrics.controllers;

import me.basemetrics.models.Player;
import me.basemetrics.models.PlayerBio;
import me.basemetrics.repositories.PlayerRepository;
import me.basemetrics.repositories.PlayerBioRepository;
import me.basemetrics.services.PlayerDataService;
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
    private PlayerBioRepository playerBioRepository;

    @Autowired
    private PlayerDataService playerDataService;

    @GetMapping("/search")
    public Page<Player> searchPlayers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String position,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "80") int size) {

        String searchName = (name != null && !name.isBlank()) ? name.trim() : null;
        String searchPos = (position != null && !position.isBlank()) ? position.trim() : null;

        return playerDataService.getPagedPlayers(searchName, searchPos, PageRequest.of(page, size));
    }

    @GetMapping("/team/{teamId}")
    public List<Player> getByTeam(@PathVariable int teamId) {
        return playerDataService.getPlayersByTeam(teamId);
    }

    @GetMapping("/{id}/bio")
    public PlayerBio getPlayerBio(@PathVariable int id) {
        return playerBioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Player not found"));
    }
}
