package me.basemetrics.controllers;

import me.basemetrics.models.Team;
import me.basemetrics.repositories.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
@CrossOrigin(origins = "*")
public class TeamController {

    @Autowired
    private TeamRepository teamRepository;

    @GetMapping("/all")
    public List<Team> getAllTeams() {
        return teamRepository.findAll();
    }

    @GetMapping("/team/{teamId}")
    public Team getByTeam(@PathVariable int teamId) {
        return teamRepository.findById(teamId);
    }
}
