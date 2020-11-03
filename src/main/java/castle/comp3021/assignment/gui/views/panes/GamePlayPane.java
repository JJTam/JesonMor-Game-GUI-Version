package castle.comp3021.assignment.gui.views.panes;

import castle.comp3021.assignment.gui.DurationTimer;
import castle.comp3021.assignment.gui.FXJesonMor;
import castle.comp3021.assignment.gui.ViewConfig;
import castle.comp3021.assignment.gui.controllers.SceneManager;
import castle.comp3021.assignment.gui.views.BigButton;
import castle.comp3021.assignment.gui.views.BigVBox;
import castle.comp3021.assignment.gui.views.GameplayInfoPane;
import castle.comp3021.assignment.gui.views.SideMenuVBox;
import castle.comp3021.assignment.player.ConsolePlayer;
import castle.comp3021.assignment.protocol.*;
import castle.comp3021.assignment.gui.controllers.Renderer;
import javafx.beans.property.*;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * This class implements the main playing function of Jeson Mor
 * The necessary components have been already defined (e.g., topBar, title, buttons).
 * Basic functions:
 *      - Start game and play, update scores
 *      - Restart the game
 *      - Return to main menu
 *      - Elapsed Timer (ticking from 00:00 -> 00:01 -> 00:02 -> ...)
 *          - The format is defined in {@link GameplayInfoPane#formatTime(int)}
 * Requirement:
 *      - The game should be initialized by configuration passed from {@link GamePane}, instead of the default configuration
 *      - The information of the game (including scores, current player name, ect.) is implemented in {@link GameplayInfoPane}
 *      - The center canvas (defined as gamePlayCanvas) should be disabled when current player is computer
 * Bonus:
 *      - A countdown timer (if this is implemented, then elapsed timer can be either kept or removed)
 *      - The format of countdown timer is defined in {@link GameplayInfoPane#countdownFormat(int)}
 *      - If one player runs out of time of each round {@link DurationTimer#getDefaultEachRound()}, then the player loses the game.
 * Hint:
 *      - You may find it useful to synchronize javafx UI-thread using {@link javafx.application.Platform#runLater}
 */ 

public class GamePlayPane extends BasePane {
    @NotNull
    private final HBox topBar = new HBox(20);
    @NotNull
    private final SideMenuVBox leftContainer = new SideMenuVBox();
    @NotNull
    private final Label title = new Label("Jeson Mor");
    @NotNull
    private final Text parameterText = new Text();
    @NotNull
    private final BigButton returnButton = new BigButton("Return");
    @NotNull
    private final BigButton startButton = new BigButton("Start");
    @NotNull
    private final BigButton restartButton = new BigButton("Restart");
    @NotNull
    private final BigVBox centerContainer = new BigVBox();
    @NotNull
    private final Label historyLabel = new Label("History");

    @NotNull
    private final Text historyFiled = new Text();
    @NotNull
    private final ScrollPane scrollPane = new ScrollPane();

    /**
     * time passed in seconds
     * Hint:
     *      - Bind it to time passed in {@link GameplayInfoPane}
     */
    private final IntegerProperty ticksElapsed = new SimpleIntegerProperty();

    @NotNull
    private final Canvas gamePlayCanvas = new Canvas();

    private GameplayInfoPane infoPane = null;

    /**
     * You can add more necessary variable here.
     * Hint:
     *      - the passed in {@link FXJesonMor}
     *      - other global variable you want to note down.
     */
    // TODO
    private FXJesonMor fxJesonMor = null;  // store this JesonMor
    private Configuration configuration = null; // store the passed configuration
    private Player winner = null;
    private Player currentPlayer = null;


    public GamePlayPane() {
        connectComponents();
        styleComponents();
        setCallbacks();
    }

    /**
     * Components are added, adjust it by your own choice
     */
    @Override
    void connectComponents() {
        //TODO
        topBar.getChildren().add(title);
        topBar.setAlignment(Pos.TOP_CENTER);
        leftContainer.getChildren().addAll(parameterText, historyLabel, scrollPane, startButton, restartButton, returnButton);
        setTop(topBar);
        setLeft(leftContainer);
        setCenter(centerContainer);
    }

    /**
     * style of title and scrollPane have been set up, no need to add more
     */
    @Override
    void styleComponents() {
        title.getStyleClass().add("head-size");
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefSize(ViewConfig.WIDTH / 4.0, ViewConfig.HEIGHT / 3.0 );
        scrollPane.setContent(historyFiled);
    }

    /**
     * The listeners are added here.
     */
    @Override
    void setCallbacks() {
        //TODO
        startButton.setOnAction(mouseEvent -> startGame());
        restartButton.setOnAction(mouseEvent -> onRestartButtonClick());
        returnButton.setOnAction(mouseEvent -> doQuitToMenuAction());
    }

    /**
     * Set up necessary initialization.
     * Hint:
     *      - Set buttons enable/disable
     *          - Start button: enable
     *          - restart button: disable
     *      - This function can be invoked before {@link GamePlayPane#startGame()} for setting up
     *
     * @param fxJesonMor pass in an instance of {@link FXJesonMor}
     */
    void initializeGame(@NotNull FXJesonMor fxJesonMor) {
        //TODO
        this.fxJesonMor = fxJesonMor;
        this.configuration = fxJesonMor.getConfiguration();
        this.fxJesonMor.renderBoard(gamePlayCanvas);
        centerContainer.getChildren().addAll(gamePlayCanvas,
                                            infoPane = new GameplayInfoPane(this.fxJesonMor.getPlayer1Score(),
                                                                            this.fxJesonMor.getPlayer2Score(),
                                                                            this.fxJesonMor.getCurPlayerName(),
                                                                            ticksElapsed)
        );
        parameterText.setText("Parameters:\n" + "\nSize of board: " + this.fxJesonMor.getConfiguration().getSize()
                            + "\nNum of protection moves: " + this.fxJesonMor.getConfiguration().getNumMovesProtection()
                            + "\nPlayer White" + (this.fxJesonMor.getConfiguration().isFirstPlayerHuman() ? "(human)" : "(computer)")
                            + "\nPlayer Black" + (this.fxJesonMor.getConfiguration().isSecondPlayerHuman() ? "(human)" : "(computer)")
                            + "\n"
        );
        startButton.setDisable(false);
        restartButton.setDisable(true);
    }

    /**
     * enable canvas clickable
     */
    private void enableCanvas() {
        gamePlayCanvas.setDisable(false);
    }

    /**
     * disable canvas clickable
     */
    private void disableCanvas() {
        gamePlayCanvas.setDisable(true);
    }

    /**
     * After click "start" button, everything will start from here
     * No explicit skeleton is given here.
     * Hint:
     *      - Give a carefully thought to how to activate next round of play
     *      - When a new {@link Move} is acquired, it needs to be check whether this move is valid.
     *          - If is valid, make the move, render the {@link GamePlayPane#gamePlayCanvas}
     *          - If is invalid, abort the move
     *          - Update score, add the move to {@link GamePlayPane#historyFiled}, also record the move
     *          - Move forward to next player
     *      - The player can be either computer or human, when the computer is playing, disable {@link GamePlayPane#gamePlayCanvas}
     *      - You can add a button to enable next move once current move finishes.
     *          - or you can add handler when mouse is released
     *          - or you can take advantage of timer to automatically change player. (Bonus)
     */
    public void startGame() {
        //TODO
        startButton.setDisable(true);
        restartButton.setDisable(false);
        winner = null;
        int numMoves = 0;
        var board = this.configuration.getInitialBoard();
        while (winner == null) {
            currentPlayer = this.configuration.getPlayers()[numMoves % this.configuration.getPlayers().length];
            var availableMoves = this.fxJesonMor.getAvailableMoves(currentPlayer);
            if (availableMoves.length <= 0) {
                showInvalidMoveMsg("No available moves for the player " + currentPlayer.getName());
                if (this.configuration.getPlayers()[0].getScore() < this.configuration.getPlayers()[1].getScore()) {
                    winner = this.configuration.getPlayers()[0];
                } else if (this.configuration.getPlayers()[0].getScore() > this.configuration.getPlayers()[1].getScore()) {
                    winner = this.configuration.getPlayers()[1];
                } else {
                    winner = currentPlayer;
                }
            } else {
                if (currentPlayer instanceof ConsolePlayer) {  // human player
                    enableCanvas();
                    this.gamePlayCanvas.setOnMousePressed(this::onCanvasPressed);
                } else {  // computer
                    disableCanvas();
                    var move = currentPlayer.nextMove(this.fxJesonMor, availableMoves);
                    var movedPiece = this.fxJesonMor.getPiece(move.getSource());
                    // make move
                    this.fxJesonMor.movePiece(move);
                    this.fxJesonMor.updateScore(currentPlayer, movedPiece, move);
                }
                System.out.println(numMoves);
                numMoves++;

            }

            if (winner != null) {
                createWinPopup(winner.getName());
            }
        }
    }

    /**
     * Restart the game
     * Hint: end the current game and start a new game
     */
    private void onRestartButtonClick() {
        //TODO
        endGame();
        initializeGame(new FXJesonMor(this.configuration));
    }

    /**
     * Add mouse pressed handler here.
     * Play click.mp3
     * draw a rectangle at clicked board tile to show which tile is selected
     * Hint:
     *      - Highlight the selected board cell using {@link Renderer#drawRectangle(GraphicsContext, double, double)}
     *      - Refer to {@link GamePlayPane#toBoardCoordinate(double)} for help
     * @param event mouse click
     */
    private void onCanvasPressed(MouseEvent event) {
        // TODO
        System.out.println("Mouse Press");

    }

    /**
     * When mouse dragging, draw a path
     * Hint:
     *      - When mouse dragging, you can use {@link Renderer#drawOval(GraphicsContext, double, double)} to show the path
     *      - Refer to {@link GamePlayPane#toBoardCoordinate(double)} for help
     * @param event mouse position
     */
    private void onCanvasDragged(MouseEvent event) {
        //TODO
    }

    /**
     * Mouse release handler
     * Hint:
     *      - When mouse released, a {@link Move} is completed, you can either validate and make the move here, or somewhere else.
     *      - Refer to {@link GamePlayPane#toBoardCoordinate(double)} for help
     *      - If the piece has been successfully moved, play place.mp3 here (or somewhere else)
     * @param event mouse release
     */
    private void onCanvasReleased(MouseEvent event) {
        // TODO
    }

    /**
     * Creates a popup which tells the winner
     */
    private void createWinPopup(String winnerName) {
        //TODO
        System.out.println(winnerName);

    }


    /**
     * check winner, if winner comes out, then play the win.mp3 and popup window.
     * The window has three options:
     *      - Start New Game: the same function as clicking "restart" button
     *      - Export Move Records: Using {@link castle.comp3021.assignment.protocol.io.Serializer} to write game's configuration to file
     *      - Return to Main menu, using {@link GamePlayPane#doQuitToMenuAction()}
     */
    private void checkWinner() {
        //TODO
        winner = currentPlayer;

    }

    /**
     * Popup a window showing invalid move information
     * @param errorMsg error string stating why this move is invalid
     */
    private void showInvalidMoveMsg(String errorMsg) {
        //TODO
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Invalid move");
        alert.setHeaderText("Your movement is invalid due to following reason(s):");
        alert.setContentText(errorMsg);
        alert.showAndWait();
    }

    /**
     * Before actually quit to main menu, popup a alert window to double check
     * Hint:
     *      - title: Confirm
     *      - HeaderText: Return to menu?
     *      - ContentText: Game progress will be lost.
     *      - Buttons: CANCEL and OK
     *  If click OK, then refer to {@link GamePlayPane#doQuitToMenu()}
     *  If click Cancel, than do nothing.
     */
    private void doQuitToMenuAction() {
        // TODO
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm");
        alert.setHeaderText("Return to menu?");
        alert.setContentText("Game progress will be lost.");
        var cancelButton = alert.showAndWait().orElse(ButtonType.OK);
        if (cancelButton == ButtonType.OK) {
            doQuitToMenu();
        }
    }

    /**
     * Update the move to the historyFiled
     * @param move the last move that has been made
     */
    private void updateHistoryField(Move move) {
        //TODO
    }

    /**
     * Go back to main menu
     * Hint: before quit, you need to end the game
     */
    private void doQuitToMenu() {
        // TODO
        final var gamePane = SceneManager.getInstance().<GamePane>getPane(GamePane.class);
        gamePane.fillValues(); // update default of gamePane before returning
        endGame();
        SceneManager.getInstance().showPane(MainMenuPane.class);
    }

    /**
     * Converting a vertical or horizontal coordinate x to the coordinate in board
     * Hint:
     *      The pixel size of every piece is defined in {@link ViewConfig#PIECE_SIZE}
     * @param x coordinate of mouse click
     * @return the coordinate on board
     */
    private int toBoardCoordinate(double x) {
        // TODO
        return 0;
    }

    /**
     * Handler of ending a game
     * Hint:
     *      - Clear the board, history text field
     *      - Reset buttons
     *      - Reset timer
     *
     */
    private void endGame() {
        //TODO
        this.centerContainer.getChildren().clear();
        this.historyFiled.setText(null);
        this.parameterText.setText(null);
        this.gamePlayCanvas.getGraphicsContext2D().clearRect(0, 0, this.gamePlayCanvas.getWidth(), this.gamePlayCanvas.getHeight());
        startButton.setDisable(false);
        restartButton.setDisable(true);
    }
}
