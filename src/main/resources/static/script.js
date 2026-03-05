//placeholder

const API_BASE_URL = "http://localhost:8080/api";

async function getData(endpoint) {
    try {
        const response = await fetch(`${API_BASE_URL}${endpoint}`);
        if (!response.ok) throw new Error('Network response was not ok');
        return await response.json();
    } catch (error) {
        console.error("Fetch error:", error);
        return null;
    }
}

function getGameStatus(game) {

    if (!game.inning || game.inning === 0 || (!game.inning_half && game.home_score === 0 && game.away_score === 0)) {
        return "Scheduled";
    }

    const inningNum = game.inning || 0;
    const inningHalf = game.inning_half;
    const outs = game.outs || 0;


    if (inningNum >= 9) {
        const half = inningHalf.toLowerCase();
        if ((half === "bottom" && game.home_score > game.away_score) ||
            (half === "top" && outs === 3 && game.home_score > game.away_score) ||
            (half === "bottom" && outs === 3)) {
            return "Final";
        }
    }


    return `
            <span class="inning">${inningHalf} ${inningNum}</span>
            <span class="outs">${outs} out${outs === 1 ? '' : 's'}</span>
            `;
}

function createGameCard(game, status){

    return `
            <span class="game-card">
                <div class="game-status">
                   ${status}
                </div>
                <div class="grid-header">
                       <span class="spacer"></span> <span class="label">R</span>
                       <span class="label">H</span>
                       <span class="label">E</span>
                </div>
                <div class="team-row">
                    <span class="team-name">${game.away_team_name}</span>
                    <span class="team-score">${game.away_score}</span>
                    <span class="team-hits">${game.away_hits}</span>
                    <span class="team-errors">${game.away_errors}</span>
                </div>
                <div class="team-row">
                    <span class="team-name">${game.home_team_name}</span>
                    <span class="team-score">${game.home_score}</span>
                    <span class="team-hits">${game.home_hits}</span>
                    <span class="team-errors">${game.home_errors}</span>
                </div>
                <div class="faceoff-field">
                    <div class="at-bat-info">
                        <span class="hitter"><strong>B:</strong> ${game.batter}</span>
                        <span class="pitcher"><strong>P:</strong> ${game.pitcher}</span>
                    </div>

                    <div class="diamond-wrapper">
                        <div class="base second ${game.on_second ? 'active' : ''}"></div>
                        <div class="base third ${game.on_third ? 'active' : ''}"></div>
                        <div class="base first ${game.on_first ? 'active' : ''}"></div>
                        <div class="home-plate"></div>
                    </div>
                </div>
                
                <div class="game-venue">Venue</div>
            </div>
        `;
}

async function renderAllLiveGames() {
    const gamesData = await getData("/games/live");

    if(!gamesData) {
        console.log("error");
        return;
    }

    const results = document.querySelector(".games-all-cards");

    if (!results) {
        return;
    }

    const hour = new Date().getHours();

    const scheduled = [];
    const live = [];
    const finals = [];

    try {
        gamesData.forEach(game => {
            const status = getGameStatus(game);
            const inning = game.inning || 0;
            const isFinal = status === "Final" && inning > 0;
            const isLive = status !== "Scheduled" && !isFinal;
            const isScheduled = status === "Scheduled";

            if (hour < 5) {
                if (isFinal) {
                    finals.push({ game, card: createGameCard(game, status) });
                } else if (isLive) {
                    live.push({ game, card: createGameCard(game, status) });
                }
            } else {
                if (isLive) {
                    live.push({ game, card: createGameCard(game, status) });
                } else if (isScheduled) {
                    scheduled.push({ game, card: createGameCard(game, status) });
                } else if (isFinal) {
                    finals.push({ game, card: createGameCard(game, status) });
                }
            }

        });
    } catch (e) {
        console.log(e);
    }

    live.sort((a, b) => b.game.inning - a.game.inning || (b.game.outs || 0) - (a.game.outs || 0));
    scheduled.sort((a, b) => a.game.home_team.localeCompare(b.game.home_team));
    const displayOrder = [...live, ...(hour >= 5 ? scheduled : []), ...finals];
    displayOrder.forEach(item => {
        results.innerHTML += item.card;
    })
}

const standingsData = [
    {
        division: "AL East",
        teams: [
            { team_id: 1, league: "AL", division: "East", team: "Tampa Bay Rays", wins: 100, losses: 67, ties: 0, games_back: "0.5"},
            { team_id: 2, league: "AL", division: "East", team: "Boston Red Sox", wins: 60, losses: 84, ties: 0, games_back: "10.0"}
        ]
    },
    {
        division: "AL Central",
        teams: [
            { team_id: 3, league: "AL", division: "West", team: "Seattle Mariners", wins: 80, losses: 30, ties: 0, games_back: "3.5"},
            { team_id: 4, league: "AL", division: "West", team: "Houston Astros", wins: 88, losses: 50, ties: 0, games_back: "5.0"}
        ]
    }
];

function renderStandings() {
    const container = document.querySelector(".standings-all-tables");

    if (!container) return;

    const standingsHTML = standingsData.map(div => `
        <div class="division-wrap">
            <h3 class="division-name">${div.division}</h3>
            <table class="standings-table">
                <thead>
                    <tr>
                        <th class="text-left">Team</th>
                        <th>W</th>
                        <th>L</th>
                        <th>PCT</th>
                        <th>GB</th>
                    </tr>
                </thead>
                <tbody>
                    ${div.teams.map(team => `
                        <tr>
                            <td class="team-name"><strong>${team.team}</strong></td>
                            <td>${team.wins}</td>
                            <td>${team.losses}</td>
                            <td>${(team.wins / (team.wins + team.losses)).toPrecision(3)}</td>
                            <td>${team.games_back}</td>
                        </tr>
                    `).join('')}
                </tbody>
            </table>
        </div>
    `).join('');

    container.innerHTML = standingsHTML;
}

const playersData = [
    {
        category: "Batting Average Leaders",
        stats: [
            { id: 101, name: "Aaron Judge", team: "NYY", g: 150, avg: ".322", hr: 58, rbi: 135 },
            { id: 102, name: "Bobby Witt Jr.", team: "KC", g: 155, avg: ".318", hr: 32, rbi: 108 },
            { id: 103, name: "Vladimir Guerrero Jr.", team: "TOR", g: 152, avg: ".312", hr: 30, rbi: 99 }
        ]
    },
    {
        category: "Home Run Leaders",
        stats: [
            { id: 101, name: "Aaron Judge", team: "NYY", g: 150, avg: ".322", hr: 58, rbi: 135 },
            { id: 104, name: "Shohei Ohtani", team: "LAD", g: 148, avg: ".305", hr: 54, rbi: 130 },
            { id: 105, name: "Anthony Santander", team: "BAL", g: 145, avg: ".240", hr: 44, rbi: 102 }
        ]
    }
];

function renderPlayerLeaders() {
    const container = document.querySelector(".players-all-tables");

    if (!container) return;

    const playersHTML = playersData.map(cat => `
        <div class="category-wrap">
            <h3 class="category-name">${cat.category}</h3>
            <div class="table-responsive">
                <table class="players-table">
                    <thead>
                        <tr>
                            <th class="text-left">Player</th>
                            <th>Team</th>
                            <th>G</th>
                            <th>HR</th>
                            <th>RBI</th>
                            <th>AVG</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${cat.stats.map(player => `
                            <tr>
                                <td class="player-name text-left">
                                    <strong>${player.name}</strong>
                                </td>
                                <td>${player.team}</td>
                                <td>${player.g}</td>
                                <td>${player.hr}</td>
                                <td>${player.rbi}</td>
                                <td class="font-bold">${player.avg}</td>
                            </tr>
                        `).join('')}
                    </tbody>
                </table>
            </div>
        </div>
    `).join('');

    container.innerHTML = playersHTML;
}

const teamsData = [
    {
        id: 1,
        name: "Boston Red Sox",
        abbreviation: "BOS",
        location: "Boston, MA",
        image_url: "https://example.com/logos/bos.png"
    },
    {
        id: 2,
        name: "New York Yankees",
        abbreviation: "NYY",
        location: "Bronx, NY",
        image_url: "https://example.com/logos/nyy.png"
    },
    {
        id: 3,
        name: "Los Angeles Dodgers",
        abbreviation: "LAD",
        location: "Los Angeles, CA",
        image_url: "https://example.com/logos/lad.png"
    },
    {
        id: 4,
        name: "Chicago Cubs",
        abbreviation: "CHC",
        location: "Chicago, IL",
        image_url: "https://example.com/logos/chc.png"
    },
    {
        id: 5,
        name: "Tampa Bay Rays",
        abbreviation: "TB",
        location: "St. Petersburg, FL",
        image_url: "https://example.com/logos/tb.png"
    }
];

function renderTeamList() {
    const container = document.querySelector(".teams-all-cards");
    if (!container) return;

    container.innerHTML = teamsData.map(team => `
    <div class="team-card" data-id="${team.id}">
        <div class="team-info">
            <div class="team-text-wrapper">
                <h4 class="team-title">${team.name} (${team.abbreviation})</h4>
                <p class="team-location">${team.location}</p>
            </div>
            
            <img src="${team.image_url}" alt="${team.name} logo" class="team-logo">
        </div>
    </div>
    `).join('');
}