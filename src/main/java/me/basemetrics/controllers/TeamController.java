package me.basemetrics.controllers;

import me.basemetrics.models.Team;
import me.basemetrics.repositories.TeamRepository;
import me.basemetrics.services.TeamDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
@CrossOrigin(origins = "*")
public class TeamController {

    @Autowired
    private TeamDataService teamDataService;

    @GetMapping("/all")
    public List<Team> getAllTeams() {
        return teamDataService.getAllTeams();
    }

    @GetMapping("/team/{teamId}")
    public Team getByTeam(@PathVariable int teamId) {
        return teamDataService.getTeamFromDb(teamId); // Now this is cached!
    }
}
