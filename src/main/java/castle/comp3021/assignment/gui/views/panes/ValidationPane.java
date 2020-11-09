package castle.comp3021.assignment.gui.views.panes;

import castle.comp3021.assignment.gui.FXJesonMor;
import castle.comp3021.assignment.gui.controllers.SceneManager;
import castle.comp3021.assignment.protocol.Configuration;
import castle.comp3021.assignment.protocol.MoveRecord;
import castle.comp3021.assignment.protocol.Place;
import castle.comp3021.assignment.protocol.exception.InvalidConfigurationError;
import castle.comp3021.assignment.protocol.exception.InvalidGameException;
import castle.comp3021.assignment.protocol.io.Deserializer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import castle.comp3021.assignment.gui.views.BigButton;
import castle.comp3021.assignment.gui.views.BigVBox;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;


public class ValidationPane extends BasePane{
    @NotNull
    private final VBox leftContainer = new BigVBox();
    @NotNull
    private final BigVBox centerContainer = new BigVBox();
    @NotNull
    private final Label title = new Label("Jeson Mor");
    @NotNull
    private final Label explanation = new Label("Upload and validation the game history.");
    @NotNull
    private final Button loadButton = new BigButton("Load file");
    @NotNull
    private final Button validationButton = new BigButton("Validate");
    @NotNull
    private final Button replayButton = new BigButton("Replay");
    @NotNull
    private final Button returnButton = new BigButton("Return");

    private Canvas gamePlayCanvas = new Canvas();

    /**
     * store the loaded information
     */
    private Configuration loadedConfiguration;
    private Integer[] storedScores;
    private FXJesonMor loadedGame;
    private Place loadedCentralPlace;
    private ArrayList<MoveRecord> loadedMoveRecords = new ArrayList<>();
    private BooleanProperty isValid = new SimpleBooleanProperty(false);
    private Deserializer deserializer;


    public ValidationPane() {
        connectComponents();
        styleComponents();
        setCallbacks();
    }

    @Override
    void connectComponents() {
        // TODO
        leftContainer.getChildren().addAll(title, explanation, loadButton, validationButton, replayButton, returnButton);
        centerContainer.getChildren().addAll(gamePlayCanvas);
        setLeft(leftContainer);
        setCenter(centerContainer);
    }

    @Override
    void styleComponents() {
        title.getStyleClass().add("head-size");
    }

    /**
     * Add callbacks to each buttons.
     * Initially, replay button is disabled, gamePlayCanvas is empty
     * When validation passed, replay button is enabled.
     */
    @Override
    void setCallbacks() {
        //TODO
        validationButton.setDisable(true);
        replayButton.setDisable(true);

        loadButton.setOnAction(mouseEvent -> {
            replayButton.setDisable(true);
            clean();
            if (loadFromFile()) {
                validationButton.setDisable(false);
            }
        });

        validationButton.setOnAction(mouseEvent -> onClickValidationButton());

        replayButton.setOnAction(mouseEvent -> onClickReplayButton());

        returnButton.setOnAction(mouseEvent -> returnToMainMenu());
    }

    /**
     * load From File and deserializer the game by two steps:
     *      - {@link ValidationPane#getTargetLoadFile}
     *      - {@link Deserializer}
     * Hint:
     *      - Get file from {@link ValidationPane#getTargetLoadFile}
     *      - Instantiate an instance of {@link Deserializer} using the file's path
     *      - Using {@link Deserializer#parseGame()}
     *      - Initialize {@link ValidationPane#loadedConfiguration}, {@link ValidationPane#loadedCentralPlace},
     *                   {@link ValidationPane#loadedGame}, {@link ValidationPane#loadedMoveRecords}
     *                   {@link ValidationPane#storedScores}
     * @return whether the file and information have been loaded successfully.
     */
    private boolean loadFromFile() {
        //TODO
        File loadFile = this.getTargetLoadFile();
        if (loadFile == null) {
            return false;
        }
        try {
            this.deserializer = new Deserializer(loadFile.toPath());
            this.deserializer.parseGame();
            this.loadedConfiguration = this.deserializer.getLoadedConfiguration();
            this.loadedCentralPlace = this.deserializer.getCentralPlace();
            this.storedScores = this.deserializer.getStoredScores();
            this.loadedMoveRecords.addAll(this.deserializer.getMoveRecords());
            return true;
        } catch (FileNotFoundException fileNotFoundException) {
            fileNotFoundException.printStackTrace();
        } catch (InvalidGameException | InvalidConfigurationError e) {
            showErrorConfiguration(e.getMessage());
        }
        return false;
    }

    /**
     * When click validation button, validate the loaded game configuration and move history
     * Hint:
     *      - if nothing loaded, call {@link ValidationPane#showErrorMsg}
     *      - if loaded, check loaded content by calling {@link ValidationPane#validateHistory}
     *      - When the loaded file has passed validation, the "replay" button is enabled.
     */
    private void onClickValidationButton() {
        //TODO
        if (this.deserializer == null || this.loadedConfiguration == null || this.loadedCentralPlace == null) {  // nothing loaded
            showErrorMsg();
            return;
        }
        if (validateHistory()) {
            passValidationWindow();
            isValid.setValue(true);
            validationButton.setDisable(true);
            replayButton.setDisable(false);
        };
    }

    /**
     * Display the history of recorded move.
     * Hint:
     *      - You can add a "next" button to render each move, or
     *      - Or you can refer to {@link Task} for implementation.
     */
    private void onClickReplayButton() {
        //TODO

    }



    /**
     * Validate the {@link ValidationPane#loadedConfiguration}, {@link ValidationPane#loadedCentralPlace},
     *              {@link ValidationPane#loadedGame}, {@link ValidationPane#loadedMoveRecords}
     *              {@link ValidationPane#storedScores}
     * Hint:
     *      - validate configuration of game
     *      - whether each move is valid
     *      - whether scores are correct
     */
    private boolean validateHistory() {
        //TODO
        try {
            this.loadedGame = new FXJesonMor(new Configuration(this.loadedConfiguration.getSize(),
                    this.loadedConfiguration.getPlayers(), this.loadedConfiguration.getNumMovesProtection()));

            if (this.loadedGame.getCentralPlace().x() != this.loadedCentralPlace.x() ||
                this.loadedGame.getCentralPlace().y() != this.loadedCentralPlace.y()) {
                throw new InvalidConfigurationError("Invalid central place, should be " + this.loadedGame.getCentralPlace().toString() +
                        " but get " + this.loadedCentralPlace.toString());
            }

            int player1Scores = 0;
            int player2Scores = 0;
            int newPlayer1Scores = 0;
            int newPlayer2Scores = 0;
            for (var move : loadedMoveRecords) {
                if (move.getPlayer().equals(this.loadedConfiguration.getPlayers()[0])) {  // player 1
                    newPlayer1Scores = Math.abs(move.getMove().getSource().x() - move.getMove().getDestination().x());
                    newPlayer1Scores += Math.abs(move.getMove().getSource().y() - move.getMove().getDestination().y());
                    player1Scores += newPlayer1Scores;
                }
                if (move.getPlayer().equals(this.loadedConfiguration.getPlayers()[1])) {  // player 2
                    newPlayer2Scores = Math.abs(move.getMove().getSource().x() - move.getMove().getDestination().x());
                    newPlayer2Scores += Math.abs(move.getMove().getSource().y() - move.getMove().getDestination().y());
                    player2Scores += newPlayer2Scores;
                }
            }

            if (player1Scores != this.storedScores[0]) {
                throw new InvalidConfigurationError("Player 1's score was incorrect! Should be " + player1Scores
                        + " but get " + this.storedScores[0]);
            }
            if (player2Scores != this.storedScores[1]) {
                throw new InvalidConfigurationError("Player 2's score was incorrect! Should be " + player2Scores
                        + " but get " + this.storedScores[1]);
            }




            return true;
        } catch (InvalidConfigurationError error) {
            showErrorConfiguration(error.getMessage());
        }
        return false;
    }

    /**
     * Popup window show error message
     * Hint:
     *      - title: Invalid configuration or game process!
     *      - HeaderText: Due to following reason(s):
     *      - ContentText: errorMsg
     * @param errorMsg error message
     */
    private void showErrorConfiguration(String errorMsg) {
        // TODO
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Invalid configuration or game process!");
        alert.setHeaderText("Due to following reason(s):");
        alert.setContentText(errorMsg);
        alert.showAndWait();
    }

    /**
     * Pop up window to warn no record has been uploaded.
     * Hint:
     *      - title: Error!
     *      - ContentText: You haven't loaded a record, Please load first.
     */
    private void showErrorMsg() {
        //TODO
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error!");
        alert.setContentText("You haven't loaded a record, Please load first.");
        alert.showAndWait();
    }

    /**
     * Pop up window to show pass the validation
     * Hint:
     *     - title: Confirm
     *     - HeaderText: Pass validation!
     */
    private void passValidationWindow() {
        //TODO
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm");
        alert.setHeaderText("Pass validation!");
        alert.showAndWait();
    }


    /**
     * clear the rendered canvas, and clear stored information
     */
    private void clean() {
        this.gamePlayCanvas.getGraphicsContext2D().clearRect(0, 0,
                this.gamePlayCanvas.getWidth(), this.gamePlayCanvas.getHeight());
        loadedConfiguration = null;
        storedScores = null;
        loadedGame = null;
        loadedCentralPlace = null;
        loadedMoveRecords = new ArrayList<>();
        isValid.setValue(false);
    }

    /**
     * Return to Main menu
     * Hint:
     *  - Before return, clear the rendered canvas, and clear stored information
     */
    private void returnToMainMenu() {
        // TODO
        clean();
        validationButton.setDisable(true);
        replayButton.setDisable(true);
        SceneManager.getInstance().showPane(MainMenuPane.class);
    }


    /**
     * Prompts the user for the file to load.
     * <p>
     * Hint:
     * Use {@link FileChooser} and {@link FileChooser#setSelectedExtensionFilter(FileChooser.ExtensionFilter)}.
     *
     * @return {@link File} to load, or {@code null} if the operation is canceled.
     */
    @Nullable
    private File getTargetLoadFile() {
        //TODO
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Files", "*.*"),
                new FileChooser.ExtensionFilter("Text Documents (*.txt)", "*.txt")
        );
        fileChooser.setSelectedExtensionFilter(fileChooser.getSelectedExtensionFilter());
        return fileChooser.showOpenDialog(new Stage());
    }

}
