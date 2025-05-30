package dungeon.engine;

import dungeon.engine.cells.*;
import java.io.*;
import java.time.LocalDate;
import java.util.*;

public class GameEngine implements  Serializable {
    private static final long serialVersionUID = 1L;
    private Cell[][] map;
    private Player player;
    private int stepsRemaining;
    private int difficulty;
    private int currentLevel;
    private transient Random random = new Random();
    private List<ScoreEntry> topScores = new ArrayList<>();
    private List<String> eventLog = new ArrayList<>();
    private boolean gameWon = false;
    private transient  boolean textMode = false;

    public GameEngine(int difficulty) {
        this(difficulty, false);
    }

    public GameEngine(int difficulty, boolean textMode) {
        this.textMode = textMode;
        setDifficulty(difficulty);
        initializeGame();
        loadTopScores();
    }

    private void setDifficulty(int d) {
        this.difficulty = Math.max(0, Math.min(10, d));
    }

    private void initializeGame() {
        this.player = new Player();
        this.stepsRemaining = 100;
        this.currentLevel = 1;
        this.eventLog.clear();
        this.gameWon = false;
        generateLevel();
        logEvent("Game started! Difficulty: " + difficulty);
        if (textMode) printMap();
    }

    public int getSize() { return 12; }

    private void generateLevel() {
        map = new Cell[12][12];
        List<int[]> availablePositions = new ArrayList<>();

        // Initialize grid with walls
        for (int y = 0; y < 12; y++) {
            for (int x = 0; x < 12; x++) {
                if (x == 0 || x == 11 || y == 0 || y == 11) {
                    map[y][x] = new WallCell(x, y);
                } else {
                    map[y][x] = new EmptyCell(x, y);
                    availablePositions.add(new int[]{x, y});
                }
            }
        }
        // Set player's starting position and reserve it
        player.setPosition(1, 10);
        availablePositions.removeIf(pos -> pos[0] == 1 && pos[1] == 10); // Always reserve (1,1

        if (currentLevel == 1) {
            // Create starting area
            map[11][1] = new EntryCell(1, 11);
            // Place ladder in Level 1
            int[] ladderPos = getRandomPosition(availablePositions);
            map[ladderPos[1]][ladderPos[0]] = new LadderCell(ladderPos[0], ladderPos[1]);
        } else {
            // Place ladder in Level 2
            int[] ladderPos = getRandomPosition(availablePositions);
            map[ladderPos[1]][ladderPos[0]] = new LadderCell(ladderPos[0], ladderPos[1]);
        }

        // Place items according to specs
        placeItems(availablePositions, 5, Cell.Type.GOLD);
        placeItems(availablePositions, 5, Cell.Type.TRAP);
        placeItems(availablePositions, 3, Cell.Type.MELEE_MUTANT);
        placeItems(availablePositions, difficulty, Cell.Type.RANGED_MUTANT);
        placeItems(availablePositions, 2, Cell.Type.HEALTH_POTION);

        logEvent("Generated Level " + currentLevel);
        if (textMode) printMap();
    }

    private int[] getRandomPosition(List<int[]> positions) {
        return positions.remove(random.nextInt(positions.size()));
    }

    private void placeItems(List<int[]> positions, int count, Cell.Type type) {
        for(int i = 0; i < count && !positions.isEmpty(); i++) {
            int[] pos = getRandomPosition(positions);
            switch(type) {
                case GOLD -> map[pos[1]][pos[0]] = new GoldCell(pos[0], pos[1]);
                case TRAP -> map[pos[1]][pos[0]] = new TrapCell(pos[0], pos[1]);
                case MELEE_MUTANT -> map[pos[1]][pos[0]] = new MeleeMutantCell(pos[0], pos[1]);
                case RANGED_MUTANT -> map[pos[1]][pos[0]] = new RangedMutantCell(pos[0], pos[1]);
                case HEALTH_POTION -> map[pos[1]][pos[0]] = new HealthPotionCell(pos[0], pos[1]);
            }
        }
    }

    public boolean movePlayer(int dx, int dy) {
        int newX = player.getX() + dx;
        int newY = player.getY() + dy;

        if(!isValidMove(newX, newY)) {
            logEvent("You tried to move but hit a wall");
            return false;
        }

        stepsRemaining--;
        player.setPosition(newX, newY);
        Cell currentCell = map[newY][newX];

        // Log movement
        String direction = getDirection(dx, dy);
        logEvent("You moved " + direction);

        // Handle cell interaction
        handleCellInteraction(currentCell);

        // Handle ranged attacks
        handleRangedAttacks();

        // Check game state
        checkGameState();

        if (textMode) printMap();
        return true;
    }

    private String getDirection(int dx, int dy) {
        if (dx == -1) return "left";
        if (dx == 1) return "right";
        if (dy == -1) return "up";
        if (dy == 1) return "down";
        return "unknown";
    }

    private void handleCellInteraction(Cell cell) {
        int prevHp = player.getHp();
        int prevScore = player.getScore();

        cell.interact(player);

        // Log interactions
        switch(cell.getType()) {
            case GOLD:
                int goldGained = player.getScore() - prevScore;
                if (goldGained > 0) {
                    logEvent("You picked up " + goldGained + " gold!");
                }
                break;

            case TRAP:
                int damage = prevHp - player.getHp();
                if (damage > 0) {
                    logEvent("You fell into a trap! Lost " + damage + " HP");
                }
                break;

            case HEALTH_POTION:
                int healAmount = player.getHp() - prevHp;
                if (healAmount > 0) {
                    logEvent("You drank a health potion! Restored " + healAmount + " HP");
                }
                break;

            case MELEE_MUTANT:
                if (player.getHp() < prevHp) {
                    logEvent("You were defeated by a melee mutant!");
                } else {
                    logEvent("You defeated a melee mutant!");
                }
                break;

            case RANGED_MUTANT:
                if (player.getHp() < prevHp) {
                    logEvent("You were defeated by a ranged mutant!");
                } else {
                    logEvent("You defeated a ranged mutant!");
                }
                break;
        }

        // Replace consumed cells
        switch(cell.getType()) {
            case GOLD, HEALTH_POTION, MELEE_MUTANT, RANGED_MUTANT ->
                    map[cell.getY()][cell.getX()] = new EmptyCell(cell.getX(), cell.getY());
        }
    }

    private void handleRangedAttacks() {
        for(int y = 0; y < 12; y++) {
            for(int x = 0; x < 12; x++) {
                Cell cell = map[y][x];
                if(cell.getType() == Cell.Type.RANGED_MUTANT) {
                    RangedMutantCell mutant = (RangedMutantCell) cell;
                    if(isInRange(x, y)) {
                        if(mutant.attemptAttack()) {
                            player.takeDamage(2);
                            logEvent("A ranged mutant attacked! You lost 2 HP");
                        } else {
                            logEvent("A ranged mutant attacked, but missed!");
                        }
                    }
                }
            }
        }
    }

    private boolean isValidMove(int x, int y) {
        return x >= 1 && x <= 10 && y >= 1 && y <= 10 &&
                map[y][x].getType() != Cell.Type.WALL;
    }

    private boolean isInRange(int mutantX, int mutantY) {
        int px = player.getX();
        int py = player.getY();
        return (Math.abs(mutantX - px) == 2 && mutantY == py) ||
                (Math.abs(mutantY - py) == 2 && mutantX == px);
    }

    private void checkGameState() {
        if(player.getHp() <= 0 || stepsRemaining <= 0) {
            endGame(false);
        } else if(map[player.getY()][player.getX()].getType() == Cell.Type.LADDER) {
            if(currentLevel == 2) {
                endGame(true);
            } else {
                advanceLevel();
            }
        }
    }

    private void advanceLevel() {
        currentLevel++;
        difficulty += 2;
        logEvent("Advanced to Level " + currentLevel + "! Difficulty increased");
        generateLevel();
    }

    private void endGame(boolean won) {
        this.gameWon = won;
        if(won) {
            checkTopScores(player.getScore());
            logEvent("CONGRATULATIONS! You escaped the dungeon!");
            logEvent("Final score: " + player.getScore());
        } else {
            logEvent("GAME OVER! Score: -1");
        }
        saveTopScores();
        showTopScores();
    }

    public void logEvent(String message) {
        eventLog.add(message);
        if (textMode) System.out.println("> " + message);
    }

    public List<String> getEventLog() {
        return new ArrayList<>(eventLog);
    }

    public void clearEventLog() {
        eventLog.clear();
    }

    // Top Scores System
    private static class ScoreEntry implements Comparable<ScoreEntry>, Serializable {
        final int score;
        final LocalDate date;

        ScoreEntry(int score) {
            this.score = score;
            this.date = LocalDate.now();
        }

        @Override
        public int compareTo(ScoreEntry o) {
            return Integer.compare(o.score, this.score);
        }

        @Override
        public String toString() {
            return String.format("%d - %s", score, date);
        }
    }

    private void checkTopScores(int score) {
        if(score <= 0) return;
        topScores.add(new ScoreEntry(score));
        Collections.sort(topScores);
        if(topScores.size() > 5) {
            topScores = topScores.subList(0, 5);
        }
    }

    private void saveTopScores() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("scores.dat"))) {
            oos.writeObject(topScores);
        } catch (IOException e) {
            System.out.println("Error saving scores: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void loadTopScores() {
        File file = new File("scores.dat");
        if(file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                topScores = (ArrayList<ScoreEntry>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                topScores = new ArrayList<>();
            }
        }
    }

    public List<String> getTopScores() {
        List<String> formatted = new ArrayList<>();
        int rank = 1;
        for(ScoreEntry entry : topScores) {
            formatted.add(String.format("#%d: %d points (%s)", rank++, entry.score, entry.date));
        }
        return formatted;
    }

    private void showTopScores() {
        if (textMode) {
            System.out.println("\n=== TOP 5 SCORES ===");
            getTopScores().forEach(System.out::println);
            System.out.println("=====================");
        }
    }

    public void saveGame(String filename) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(this);
        } catch (IOException e) {
            logEvent("Save failed: " + e.getMessage());
        }
    }

    public static GameEngine loadGame(String filename) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            return (GameEngine) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            return null;
        }
    }

    // Getters
    public Cell[][] getMap() { return map; }
    public Player getPlayer() { return player; }
    public int getStepsRemaining() { return stepsRemaining; }
    public boolean isGameOver() {
        return player.getHp() <= 0 || stepsRemaining <= 0;
    }
    public boolean isGameWon() { return gameWon; }

    // Text UI
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int difficulty = 3;

        System.out.println("=== MINI DUNGEON ===");
        System.out.print("Enter difficulty (0-10): ");
        try {
            difficulty = scanner.nextInt();
        } catch (InputMismatchException e) {
            System.out.println("Invalid input. Using default difficulty 3.");
        }

        new GameEngine(difficulty, true).startTextGame();
    }

    private void startTextGame() {
        Scanner scanner = new Scanner(System.in);
        while(!isGameOver() && !isGameWon()) {
            System.out.println("\nHP: " + player.getHp() +
                    " | Steps: " + stepsRemaining +
                    " | Score: " + player.getScore());
            System.out.print("Move [U/D/L/R]: ");
            String input = scanner.next().toUpperCase();
            boolean moved = switch(input) {
                case "U" -> movePlayer(0, -1);
                case "D" -> movePlayer(0, 1);
                case "L" -> movePlayer(-1, 0);
                case "R" -> movePlayer(1, 0);
                default -> false;
            };
            if(!moved) System.out.println("Invalid move!");
        }

        if (isGameWon()) {
            System.out.println("\nCONGRATULATIONS! You escaped the dungeon!");
            System.out.println("Final score: " + player.getScore());
        } else {
            System.out.println("\nGAME OVER! Score: -1");
        }

        showTopScores();
    }

    private void printMap() {
        for(int y = 0; y < 12; y++) {
            for(int x = 0; x < 12; x++) {
                char symbol;
                if(player.getX() == x && player.getY() == y) {
                    symbol = 'P';
                } else {
                    symbol = switch(map[y][x].getType()) {
                        case WALL -> '#';
                        case ENTRY -> 'E';
                        case LADDER -> 'L';
                        case TRAP -> 'T';
                        case GOLD -> 'G';
                        case MELEE_MUTANT -> 'M';
                        case RANGED_MUTANT -> 'R';
                        case HEALTH_POTION -> 'H';
                        default -> '.';
                    };
                }
                System.out.print(symbol + " ");
            }
            System.out.println();
        }
    }
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        random = new Random();
        textMode = false;
        clearPlayerCell(); // Ensure player's position is safe after loading
    }
    // Clears any interactive cell at player's position (except ladder)
    private void clearPlayerCell() {
        int x = player.getX();
        int y = player.getY();
        Cell current = map[y][x];
        if (current.getType() != Cell.Type.LADDER) {
            map[y][x] = new EmptyCell(x, y);
        }
    }

}