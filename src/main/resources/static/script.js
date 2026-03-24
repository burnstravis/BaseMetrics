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

    const gameHeader = `
        <div class="games-header">
            <h1 id="games-title">Live Games</h1>
            <p id="games-message">View all the action happening right now</p>
        </div>
    `;

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
    scheduled.sort((a, b) => a.game.home_team_name.localeCompare(b.game.home_team_name));
    const displayOrder = [...live, ...(hour >= 5 ? scheduled : []), ...finals];
    results.innerHTML += gameHeader;
    displayOrder.forEach(item => {
        results.innerHTML += item.card;
    })
}

async function renderOnlyLiveGames(){
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

    try {
        gamesData.forEach(game => {
            const status = getGameStatus(game);
            const inning = game.inning || 0;
            const isFinal = status === "Final" && inning > 0;
            const isLive = status !== "Scheduled" && !isFinal;

            if (isLive) {
                live.push({ game, card: createGameCard(game, status) });
            }

        });
    } catch (e) {
        console.log(e);
    }

    live.sort((a, b) => b.game.inning - a.game.inning || (b.game.outs || 0) - (a.game.outs || 0));
    scheduled.sort((a, b) => a.game.home_team_name.localeCompare(b.game.home_team_name));
    const displayOrder = [...live, ...(hour >= 5 ? scheduled : [])];
    displayOrder.forEach(item => {
        results.innerHTML += item.card;
    })
}


async function fetchPlayerStats(page = 0) {

    const nameValue = document.getElementById("searchInput").value;
    const isPitching = document.getElementById("toggle-pitching").classList.contains("active");
    const posValue = document.getElementById("players-options").value;
    console.log(nameValue);
    const params = new URLSearchParams();
    if(isPitching === true){
        if (nameValue) params.append("name", nameValue);
        params.append("position", "P");
    }
    else {
        if (nameValue) params.append("name", nameValue);

        if (posValue && posValue !== "all") {
            params.append("position", posValue);
        } else {
            params.append("position", "NOT_P");
        }
    }
    params.append("page", page);
    params.append("size", 80);
    try {
        const pageData = await getData(`/players/search?${params.toString()}`);

        const players = pageData.content;
        const totalPages = pageData.totalPages;
        const currentPage = pageData.number;

        renderPlayers(players, isPitching);

    } catch (e){
        console.error("Error fetching players", e);
    }
}

function renderPlayers(players, isPitching) {
    const container = document.querySelector(".players-all-tables");
    if (!container) return;

    if (!players || players.length === 0) {
        container.innerHTML = "<p>No players found.</p>";
        return;
    }

    const playersHTML = `
        <div class="table-responsive">
            <table class="players-table">
                <thead>
                    <tr>
                        <th class="text-left">Player</th>
                        <th>Pos</th>
                        ${isPitching
                            ? `<th>W</th><th>L</th><th>PCT</th><th>ERA</th><th>G</th><th>IP</th><th>H</th><th>R</th><th>ER</th><th>HR</th><th>SO</th><th>BB</th><th>HBP</th><th>GIDP</th><th>SB</th><th>BF</th><th>WHIP</th>`
                            : `<th>G</th><th>AB</th><th>PA</th><th>AVG</th><th>OBP</th><th>SLG</th><th>OPS</th><th>2B</th><th>3B</th><th>HR</th><th>BB</th><th>R</th><th>RBI</th><th>SO</th><th>GIDP</th><th>SB</th><th>CS</th><th>HBP</th>`
                        }
                    </tr>
                </thead>
                <tbody>
                    ${players.map(player => {
                        const stats = isPitching ? (player.pitchingStats || {}) : (player.battingStats || {});
                        const wins = stats.wins || 0;
                        const losses = stats.losses || 0;
                        const winPct = (wins + losses) > 0
                            ? (wins / (wins + losses)).toFixed(3).replace(/^0/, '')
                            : ".000";
                
                        return `
                        <tr>
                            <td class="player-name text-left">
                                <a href="/player.html?player_id=${player.id}" class="player-name">
                                    <strong>${player.fullName}</strong>
                                </a>
                            </td>
                            <td>${player.position}</td>
                            ${isPitching ? `
                                <td>${wins}</td>
                                <td>${losses}</td>
                                <td>${winPct}</td>
                                <td class="font-bold">${stats.era ?? '—'}</td>
                                <td>${stats.gamesPlayed ?? 0}</td>
                                <td>${stats.inningsPitched ?? '0.0'}</td>
                                <td>${stats.hits ?? 0}</td>
                                <td>${stats.runs ?? 0}</td>
                                <td>${stats.earnedRuns ?? 0}</td>
                                <td>${stats.homeRuns ?? 0}</td>
                                <td>${stats.strikeOuts ?? 0}</td>
                                <td>${stats.p_walks ?? 0}</td>
                                <td>${stats.hitByPitch ?? 0}</td>
                                <td>${stats.groundIntoDoublePlay ?? 0}</td>
                                <td>${stats.stolenBases ?? 0}</td>
                                <td>${stats.battersFaced ?? 0}</td>
                                <td>${stats.whip ?? '—'}</td>
                            ` : `
                                <td>${stats.gamesPlayed ?? 0}</td>
                                <td>${stats.atBats ?? 0}</td>
                                <td>${stats.plateAppearances ?? 0}</td>
                                <td class="font-bold">${stats.avg ?? '.000'}</td>
                                <td>${stats.obp ?? '.000'}</td>
                                <td>${stats.slg ?? '.000'}</td>
                                <td>${stats.ops ?? '.000'}</td>
                                <td>${stats.doubles ?? 0}</td>
                                <td>${stats.triples ?? 0}</td>
                                <td>${stats.homeRuns ?? 0}</td>
                                <td>${stats.baseOnBalls ?? 0}</td>
                                <td>${stats.runs ?? 0}</td>
                                <td>${stats.rbi ?? 0}</td>
                                <td>${stats.strikeOuts ?? 0}</td>
                                <td>${stats.groundIntoDoublePlay ?? 0}</td>
                                <td>${stats.stolenBases ?? 0}</td>
                                <td>${stats.caughtStealing ?? 0}</td>
                                <td>${stats.hitByPitch ?? 0}</td>
                            `}
                        </tr>
                        `;
                    }).join('')}
                </tbody>
            </table>
        </div>`;

    container.innerHTML = playersHTML;
}

async function fetchTeamData(){

    const teamsData = await getData("/teams/all");

    if(!teamsData) {
        console.log("error");
        return;
    }

    try{
        renderTeamList(teamsData);
    } catch (e){
        console.error(e);
    }

}

function renderTeamList(teamsData) {
    const container = document.querySelector(".teams-all-cards");
    if (!container) return;
    const titleCard = `
    <h1 id="teams-title">Teams</h1>
    `;

    container.innerHTML =  titleCard + teamsData.map(team => `
    <div class="team-card" data-id="${team.id}">
        <div class="team-info">
            <div class="team-text-wrapper">
                <h4 class="team-title">${team.name} (${team.abbreviation})</h4>
                <p class="team-location">${team.locationName}</p>
            </div>
            
            <div class="team-logo-wrapper" style="--logo-url: url('${team.image_url}')"></div>      
         </div>
    </div>
    `).join('');
}

async function fetchStandingsData(sort){

    const standingsData = await getData("/standings/all");

    if(!standingsData) {
        console.log("error");
        return;
    }

    try{
        renderStandings(standingsData, sort);
    } catch (e){
        console.error(e);
    }

}

function renderStandings(standingsData, sort) {
    if (!standingsData || standingsData.length === 0) return;

    const divisionOrder = {
        "American League East": 1,
        "American League Central": 2,
        "American League West": 3,
        "National League East": 4,
        "National League Central": 5,
        "National League West": 6
    };

    const container = document.querySelector(".standings-all-tables");
    if (!container) return;

    container.setAttribute('data-view', sort);

    let grouped = {};

    if(sort === "division") {
        grouped = standingsData.reduce((acc, team) => {
            const divName = team.division || "Unknown"; //
            if (!acc[divName]) acc[divName] = [];
            acc[divName].push(team);
            return acc;
        }, {});
    } else if (sort === "league") {
        grouped = standingsData.reduce((acc, team) => {
            const leagueName = team.league || "Unknown"; //
            if (!acc[leagueName]) acc[leagueName] = [];
            acc[leagueName].push(team);
            return acc;
        }, {});
    } else {
        grouped = {"Major League Baseball": standingsData};
    }

    let groupedEntries = Object.entries(grouped).sort((a, b) => {
        const nameA = a[0];
        const nameB = b[0];

        if (sort === "division") {
            const rankA = divisionOrder[nameA];
            const rankB = divisionOrder[nameB];

            if (rankA !== rankB) {
                return rankA - rankB;
            }
        }

        else return null;
    });

    const standingsHTML = groupedEntries.map(([groupName, teams]) => {
        // Sort teams within the group by wins
        teams.sort((a, b) => b.wins - a.wins);
        return `
        <div class="division-wrap">
            <h3 class="division-name">${groupName}</h3>
            <table class="standings-table">
                <thead>
                    <tr>
                        <th class="text-left">Team</th>
                        <th>W</th>
                        <th>L</th>
                        <th>PCT</th>
                        <th>GB</th>
                        <th>WCGB</th>
                        <th>L10</th>
                        <th>STRK</th>
                        <th>X-W/L</th>
                        <th>HOME</th>
                        <th>AWAY</th>
                        <th>>.500</th>
                    </tr>
                </thead>
                <tbody>
                    ${teams.map(team => {
            const winPct = (team.wins + team.losses) > 0
                ? (team.wins / (team.wins + team.losses)).toFixed(3)
                : ".000";

            const xWinPct = (team.expectedWins + team.expectedLosses) > 0
                ? (team.expectedWins / (team.expectedWins + team.expectedLosses)).toFixed(3)
                : ".000";
            return `
                        <tr>
                            <td class="team-cell">
                                <div class="team-logo-stencil" 
                                     style="--logo-url: url('https://www.mlbstatic.com/team-logos/${team.team_id}.svg')">
                                </div>
                                <strong>${team.teamName}</strong>
                            </td>
                            <td>${team.wins}</td>
                            <td>${team.losses}</td>
                            <td>${winPct}</td>
                            <td>${team.gamesBack === 0 ? '-' : team.gamesBack}</td>
                            <td>${team.wildCardGamesBack}</td>
                            <td>${team.lastTenWins} - ${team.lastTenLosses}</td>
                            <td>${team.streakCode}</td>
                            <td>${xWinPct}</td>
                            <td>${team.homeWins} - ${team.homeLosses}</td>
                            <td>${team.awayWins} - ${team.awayLosses}</td>
                            <td>${team.above500Wins} - ${team.above500Losses}</td>
                        </tr>`;
        }).join('')}
                </tbody>
            </table>
        </div>`;
    }).join('');

    container.innerHTML = standingsHTML;
}




