package dungeon.gui;

import dungeon.engine.Cell;
import dungeon.engine.GameEngine;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.paint.Color;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

public class Controller {
    @FXML private GridPane gridPane;
    @FXML private Label scoreLabel;
    @FXML private Label hpLabel;
    @FXML private Label stepsLabel;
    @FXML private TextArea scoreBoard;
    @FXML private TextArea statusLog;
    @FXML private ComboBox<Integer> difficultyCombo;
    @FXML private StackPane centerStack;
    @FXML private VBox welcomeBox;
    @FXML private ImageView welcomeImage;

    private GameEngine engine;
    private static final String SAVE_FILE = "minidungeon.sav";

    @FXML
    public void initialize() {
        // difficulty selector
        for (int i = 0; i <= 10; i++) difficultyCombo.getItems().add(i);
        difficultyCombo.getSelectionModel().select(3);

        // hide grid, show welcome
        gridPane.setVisible(false);
        welcomeBox.setVisible(true);

        // prompt for empty scoreboard
        scoreBoard.setPromptText("No scores yet");

        // load welcome image
        try (InputStream s = getClass().getClassLoader().getResourceAsStream("player.png")) {
            if (s != null) welcomeImage.setImage(new Image(s));
        } catch (Exception e) {
            System.err.println("Error loading welcome image: " + e.getMessage());
        }
    }

    @FXML
    private void startGame() {
        welcomeBox.setVisible(false);
        int diff = difficultyCombo.getValue() != null ? difficultyCombo.getValue() : 3;
        engine = new GameEngine(diff);

        gridPane.setVisible(true);
        renderGameGrid();
        updateGameInfo();
        updateScoreBoard();
        updateStatusLog();
    }

    private void renderGameGrid() {
        gridPane.getChildren().clear();
        int size = engine.getSize();
        gridPane.setPadding(new Insets(5));
        gridPane.setHgap(2);
        gridPane.setVgap(2);

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                Cell cell = engine.getMap()[y][x];
                StackPane cellPane = new StackPane();
                cellPane.getStyleClass().add("grid-cell");
                cellPane.setMinSize(40, 40);
                cellPane.setMaxSize(40, 40);

                ImageView iv = new ImageView(getImageForCell(cell));
                iv.setFitWidth(40);
                iv.setFitHeight(40);
                cellPane.getChildren().add(iv);

                if (engine.getPlayer().getX()==x && engine.getPlayer().getY()==y) {
                    ImageView p = new ImageView(getPlayerImage());
                    p.setFitWidth(30);
                    p.setFitHeight(30);
                    cellPane.getChildren().add(p);
                }

                gridPane.add(cellPane, x, y);
            }
        }
    }

    private Image getImageForCell(Cell cell) {
        String file = switch (cell.getType()) {
            case WALL          -> "wall.png";
            case ENTRY         -> "entry.png";
            case LADDER        -> "ladder.png";
            case TRAP          -> "trap.png";
            case GOLD          -> "gold.png";
            case MELEE_MUTANT  -> "melee_mutant.png";
            case RANGED_MUTANT -> "ranged_mutant.png";
            case HEALTH_POTION -> "healthPotion.png";
            default             -> "empty.png";
        };
        return loadImage(file, cell.getType().name().charAt(0));
    }

    private Image getPlayerImage() {
        return loadImage("player.png", 'P');
    }

    private Image loadImage(String fname, char fallback) {
        try (InputStream s = getClass().getClassLoader().getResourceAsStream(fname)) {
            if (s != null) return new Image(s);
        } catch (Exception e) {
            System.err.println("Error loading "+fname+": "+e.getMessage());
        }
        Text t = new Text(String.valueOf(fallback));
        t.setStyle("-fx-font-weight:bold; -fx-font-size:20;");
        t.setTextAlignment(TextAlignment.CENTER);
        t.setFill(Color.BLACK);
        return t.snapshot(null, null);
    }

    private void updateGameInfo() {
        scoreLabel.setText("Score: "+engine.getPlayer().getScore());
        hpLabel.setText("HP: "+engine.getPlayer().getHp()+"/10");
        stepsLabel.setText("Steps: "+engine.getStepsRemaining());

        hpLabel.setStyle(engine.getPlayer().getHp()<=3
                ? "-fx-text-fill:#FF5252; -fx-font-weight:bold;"
                : "-fx-text-fill:white;");
        stepsLabel.setStyle(engine.getStepsRemaining()<=20
                ? "-fx-text-fill:#29B6F6; -fx-font-weight:bold;"
                : "-fx-text-fill:white;");
    }

    private void updateStatusLog() {
        statusLog.clear();
        for (String e : engine.getEventLog()) statusLog.appendText("• "+e+"\n");
        statusLog.setScrollTop(Double.MAX_VALUE);
    }

    private void updateScoreBoard() {
        scoreBoard.clear();
        List<String> top = engine.getTopScores();
        if (top.isEmpty()) {
            scoreBoard.setPromptText("No scores yet");
        } else {
            top.forEach(s -> scoreBoard.appendText(s+"\n"));
        }
    }

    @FXML private void handleUp()    { move(0,-1); }
    @FXML private void handleDown()  { move(0,1); }
    @FXML private void handleLeft()  { move(-1,0); }
    @FXML private void handleRight() { move(1,0); }

    private void move(int dx, int dy) {
        if (engine.isGameOver()||engine.isGameWon()) { showGameOverAlert(); return; }
        if (engine.movePlayer(dx,dy)) {
            renderGameGrid();
            updateGameInfo();
            updateStatusLog();
            if (engine.isGameOver()||engine.isGameWon()) {
                updateScoreBoard();
                showGameOverAlert();
            }
        }
    }

    private void showGameOverAlert() {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Game Over");

        String header, msg;
        if (engine.isGameWon()) {
            header="You Escaped the Dungeon!"; msg="Congratulations!";
        } else if (engine.getPlayer().getHp()<=0) {
            header="You Have Died!"; msg="Your HP reached 0.";
        } else {
            header="Out of Time!"; msg="You ran out of steps.";
        }
        a.setHeaderText(header);

        Label content = new Label(msg);
        Label score = new Label("Final score: "+engine.getPlayer().getScore());
        score.getStyleClass().add("final-score");
        VBox box = new VBox(10, content, score);
        box.setAlignment(Pos.CENTER);
        a.getDialogPane().setContent(box);

        ButtonType restart = new ButtonType("Restart Game");
        ButtonType exit    = new ButtonType("Exit to Menu");
        a.getButtonTypes().setAll(restart, exit);

        Optional<ButtonType> res = a.showAndWait();
        if (res.isPresent() && res.get()==restart) {
            startGame();
        } else {
            welcomeBox.setVisible(true);
            gridPane.setVisible(false);
        }
    }

    @FXML
    private void handleSave() {
        engine.saveGame(SAVE_FILE);
        engine.logEvent("Game saved successfully");
        updateStatusLog();
    }

    @FXML
    private void handleLoad() {
        GameEngine loaded = GameEngine.loadGame(SAVE_FILE);
        if (loaded!=null) {
            engine = loaded;
            engine.logEvent("Game loaded successfully");
            welcomeBox.setVisible(false);
            gridPane.setVisible(true);
            renderGameGrid();
            updateGameInfo();
            updateScoreBoard();
            updateStatusLog();
        } else {
            statusLog.appendText("Failed to load saved game\n");
        }
    }

    @FXML
    private void handleHelp() {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Game Help");
        a.setHeaderText("Mini Dungeon Adventure");
        a.setContentText(
                "HOW TO PLAY:\n\n" +
                        "- Use direction buttons to move\n" +
                        "- Collect gold (G) to increase score\n" +
                        "- Avoid traps (T) that reduce HP\n" +
                        "- Use health potions (H) to restore HP\n" +
                        "- Defeat mutants (M/R) to clear paths\n" +
                        "- Find the ladder (L) to advance levels\n\n" +
                        "GOAL:\n" +
                        "Reach the ladder in Level 2 to win!\n\n" +
                        "STATUS LOG:\n" +
                        "All game events are shown in the log\n\n" +
                        "TIP: Higher difficulty means more enemies!"
        );
        a.showAndWait();
    }
}
