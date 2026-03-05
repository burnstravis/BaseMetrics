package me.basemetrics.controllers;

import me.basemetrics.models.LiveGame;
import me.basemetrics.repositories.LiveGameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/games")
@CrossOrigin(origins = "*")
public class LiveGameController {

    @Autowired
    private LiveGameRepository liveGameRepository;

    @GetMapping("/live")
    public List<LiveGame> getLiveGames() {
        return liveGameRepository.findAll();
    }
}
