package me.basemetrics;

public class LiveGameTeam {

    private final String name;
    private final int score;
    private final int errors;

    public LiveGameTeam(String name, int score, int errors) {
        this.name = name;
        this.score = score;
        this.errors = errors;
    }

    public String getName() {
        return name;
    }
    public int getScore() {
        return score;
    }
    public int getErrors() {
        return errors;
    }
}
