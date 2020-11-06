package castle.comp3021.assignment.gui.views.panes;

import castle.comp3021.assignment.gui.DurationTimer;
import castle.comp3021.assignment.gui.FXJesonMor;
import castle.comp3021.assignment.gui.ViewConfig;
import castle.comp3021.assignment.gui.controllers.SceneManager;
import castle.comp3021.assignment.gui.views.BigButton;
import castle.comp3021.assignment.gui.views.BigVBox;
import castle.comp3021.assignment.gui.views.GameplayInfoPane;
import castle.comp3021.assignment.gui.views.SideMenuVBox;
import castle.comp3021.assignment.piece.Knight;
import castle.comp3021.assignment.player.ConsolePlayer;
import castle.comp3021.assignment.protocol.*;
import castle.comp3021.assignment.gui.controllers.Renderer;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import org.jetbrains.annotations.NotNull;

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
    // add global variables
    private FXJesonMor fxJesonMor = null;  // store this JesonMor
    private Configuration configuration = null; // store the passed configuration
    private final BooleanProperty enterNextTurn = new SimpleBooleanProperty(false);
    private Player currentPlayer = null;
    private Piece movedPiece = null;
    private Move lastMove = null;
    private Player winner = null;
    private int numMoves = 0;


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
        enterNextTurn.setValue(false);
        fxJesonMor.startCountdown();
//        var board = this.configuration.getInitialBoard();
        this.fxJesonMor.addOnTickHandler(new Runnable() {
            @Override
            public void run() {
                ticksElapsed.setValue(ticksElapsed.getValue() + 1);
                System.out.println(GameplayInfoPane.countdownFormat(ticksElapsed.getValue()));
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (ticksElapsed.getValue() == DurationTimer.getDefaultEachRound()) {  // time out
                            fxJesonMor.stopCountdown();
                            createLosePopup(currentPlayer.getName());
                        }

                        if (enterNextTurn.getValue()) {  // next round, if true
                            enterNextTurn.setValue(false);
                            ticksElapsed.setValue(0);
                            nextRound();
                        }
                    }
                });
            }
        });
        nextRound();
    }


    /**
     * Helper method
     * next round to be performed
     */
    public void nextRound() {
        this.currentPlayer = this.configuration.getPlayers()[this.numMoves % this.configuration.getPlayers().length];
        var availableMoves = this.fxJesonMor.getAvailableMoves(currentPlayer);
        if (availableMoves.length <= 0) {
            this.fxJesonMor.stopCountdown();
            showInvalidMoveMsg("No available moves for the player " + currentPlayer.getName());
            if (this.configuration.getPlayers()[0].getScore() < this.configuration.getPlayers()[1].getScore()) {
                this.winner = this.configuration.getPlayers()[0];
            } else if (this.configuration.getPlayers()[0].getScore() > this.configuration.getPlayers()[1].getScore()) {
                this.winner = this.configuration.getPlayers()[1];
            } else {
                this.winner = currentPlayer;
            }
            createWinPopup(this.winner.getName());
        } else {
            if (currentPlayer instanceof ConsolePlayer) {  // human player turn
                enableCanvas();
                this.gamePlayCanvas.setOnMousePressed(this::onCanvasPressed);
                this.gamePlayCanvas.setOnMouseDragged(this::onCanvasDragged);
                this.gamePlayCanvas.setOnMouseReleased(this::onCanvasReleased);

            } else {   // computer player turn
                disableCanvas();
                this.lastMove = currentPlayer.nextMove(this.fxJesonMor, availableMoves);
                this.movedPiece = this.fxJesonMor.getPiece( this.lastMove.getSource());
                this.fxJesonMor.movePiece( this.lastMove);
                this.numMoves++;
                updateHistoryField(this.lastMove);
                this.fxJesonMor.updateScore(this.currentPlayer, this.movedPiece, this.lastMove);
                enterNextTurn.setValue(true);
            }
            System.out.printf("move: %d\n", this.numMoves);
            checkWinner();
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
        System.out.println("Mouse press");

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
        System.out.println("Mouse dragging");
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
        System.out.println("Mouse release");
        this.enterNextTurn.setValue(true);
    }

    /**
     * Creates a popup which tells the winner
     */
    private void createWinPopup(String winnerName) {
        //TODO
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Congratulations!");
        alert.setHeaderText("Conformation");
        alert.setContentText(winnerName + " wins!");
        var cancelButton = alert.showAndWait().orElse(ButtonType.OK);
        if (cancelButton == ButtonType.OK) {
            doQuitToMenu();
        }

    }

    /**
     * Creates a popup which tells the loser(Time out only)
     */
    private void createLosePopup(String loserName) {
        //TODO
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Sorry! Time up");
        alert.setHeaderText("Conformation");
        alert.setContentText(loserName + " lose!");
        var cancelButton = alert.showAndWait().orElse(ButtonType.OK);
        if (cancelButton == ButtonType.OK) {
            doQuitToMenu();
        }

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

        // no winner within numMovesProtection moves
        if (this.numMoves <= this.configuration.getNumMovesProtection()) {
            return;
        }

        // first way to win: a piece leaves the central square, the piece should not be an Archer
        if ((this.movedPiece instanceof Knight) && this.lastMove.getSource().equals(this.configuration.getCentralPlace())
                && !this.lastMove.getDestination().equals(this.configuration.getCentralPlace())) {
            this.winner = this.currentPlayer;
        } else {
            // second way to win: one player captures all the pieces of other players
            Player remainingPlayer = null;
            for (int i = 0; i < this.configuration.getSize(); i++) {
                for (int j = 0; j < this.configuration.getSize(); j++) {
                    var piece = this.fxJesonMor.getPiece(i, j);
                    if (piece == null) {
                        continue;
                    }
                    if (remainingPlayer == null) {
                        remainingPlayer = piece.getPlayer();
                    } else if (remainingPlayer != piece.getPlayer()) {
                        // there are still two players having pieces on board
                        return;
                    }
                }
            }
            // if the previous for loop terminates, then there must be 1 player on board (it cannot be null).
            // then winner appears
            this.winner = remainingPlayer;
        }

        if (this.winner != null) {  // if winner appear
            this.fxJesonMor.stopCountdown();
            createWinPopup(this.winner.getName());
        }
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
        this.historyFiled.setText(this.historyFiled.getText() + "\n  " +
                "[" + move.getSource().x() + ", " + move.getSource().y() + "]" + " -> " +
                "[" + move.getDestination().x() + ", " + move.getDestination().y() + "]");
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

        // reset most of the global variables
        this.fxJesonMor.stopCountdown();
        this.centerContainer.getChildren().clear();
        this.historyFiled.setText(null);
        this.parameterText.setText(null);
        this.gamePlayCanvas.getGraphicsContext2D().clearRect(0, 0,
                this.gamePlayCanvas.getWidth(), this.gamePlayCanvas.getHeight());
        this.enterNextTurn.setValue(false);
        this.ticksElapsed.setValue(0);
        this.winner = null;
        this.currentPlayer = null;
        this.movedPiece = null;
        this.lastMove = null;
        this.numMoves = 0;
        startButton.setDisable(false);
        restartButton.setDisable(true);
    }
}
