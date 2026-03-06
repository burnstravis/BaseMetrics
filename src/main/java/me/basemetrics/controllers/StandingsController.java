package me.basemetrics.controllers;

import me.basemetrics.models.Standings;
import me.basemetrics.repositories.StandingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/standings")
@CrossOrigin(origins = "*")
public class StandingsController {

    @Autowired
    private StandingsRepository standingsRepository;

    @GetMapping("/all")
    public List<Standings> getStandings() {
        return standingsRepository.findAll();
    }
}
