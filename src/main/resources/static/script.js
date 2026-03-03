//placeholder
const gamesData = [
    {
        gameId: 123456,
        status: "Live",
        inning: "Bottom 5th",
        venue: "Fenway Park",
        teams: {
            away: { abbreviation: "NYY", runs: 3, hits: 5 },
            home: { abbreviation: "BOS", runs: 4, hits: 7 }
        }
    },
    {
        gameId: 789012,
        status: "Final",
        inning: "9th",
        venue: "Dodger Stadium",
        teams: {
            away: { abbreviation: "SFG", runs: 1, hits: 4 },
            home: { abbreviation: "LAD", runs: 6, hits: 10 }
        }
    },
    {
        gameId: 345678,
        status: "Scheduled",
        inning: "7:10 PM",
        venue: "Wrigley Field",
        teams: {
            away: { abbreviation: "STL", runs: 0, hits: 0 },
            home: { abbreviation: "CHC", runs: 0, hits: 0 }
        }
    }
];

function allLiveGames() {
    const results = document.querySelector(".games-all-cards");

    if (!results) {
        return;
    }

    results.innerHTML = "";

    gamesData.forEach(game => {
        const gameCard = `
            <span class="game-card">
                <div class="game-status">
                    <span class="status">${game.status}</span>
                    <span class="inning">${game.inning}</span>
                </div>
                <div class="team-row">
                    <span class="team-name">${game.teams.away.abbreviation}</span>
                    <span class="team-score">${game.teams.away.runs}</span>
                </div>
                <div class="team-row">
                    <span class="team-name">${game.teams.home.abbreviation}</span>
                    <span class="team-score">${game.teams.home.runs}</span>
                </div>
                <div class="game-venue">${game.venue}</div>
            </div>
        `;
        results.innerHTML += gameCard;
    });
}