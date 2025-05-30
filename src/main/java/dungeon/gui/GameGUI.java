package dungeon.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.nio.file.Paths;

public class GameGUI extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load FXML from the same directory
        Parent root = FXMLLoader.load(Paths.get("src/main/java/dungeon/gui/game_gui.fxml").toUri().toURL());

        Scene scene = new Scene(root, 900, 700);

        // Load CSS from the same directory
        scene.getStylesheets().add(
                Paths.get("src/main/java/dungeon/gui/styles.css").toUri().toString()
        );

        primaryStage.setScene(scene);
        primaryStage.setTitle("MiniDungeon Game");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}