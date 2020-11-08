package castle.comp3021.assignment.protocol.io;

import castle.comp3021.assignment.player.RandomPlayer;
import castle.comp3021.assignment.protocol.*;
import castle.comp3021.assignment.protocol.exception.InvalidConfigurationError;
import castle.comp3021.assignment.protocol.exception.InvalidGameException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

public class Deserializer {
    @NotNull
    private Path path;

    private Configuration configuration;

    private Integer[] storedScores;

    Place centralPlace;

    private ArrayList<MoveRecord> moveRecords = new ArrayList<>();



    public Deserializer(@NotNull final Path path) throws FileNotFoundException {
        if (!path.toFile().exists()) {
            throw new FileNotFoundException("Cannot find file to load!");
        }
        this.path = path;
    }

    /**
     * Returns the first non-empty and non-comment (starts with '#') line from the reader.
     *
     * @param br {@link BufferedReader} to read from.
     * @return First line that is a parsable line, or {@code null} there are no lines to read.
     * @throws IOException if the reader fails to read a line
     * @throws InvalidGameException if unexpected end of file
     */
    @Nullable
    private String getFirstNonEmptyLine(@NotNull final BufferedReader br) throws IOException {
        // TODO
        String line;
        while ((line = br.readLine()) != null) {
            if (!line.isEmpty() && !line.contains("#") && !line.contains("END")) {
                return line;
            }
        }
        return null;
    }

    public void parseGame() {
        try (var reader = new BufferedReader(new FileReader(path.toFile()))) {
            String line;
            int size;
            line = getFirstNonEmptyLine(reader);
            if (line != null) {
                // TODO: get size here
                size = Integer.parseInt(line.replaceAll("[^0-9]", ""));
                System.out.println("size =  " + size);
            } else {
                throw new InvalidGameException("Unexpected EOF when parsing number of board size");
            }

            int numMovesProtection;
            line = getFirstNonEmptyLine(reader);
            if (line != null) {
                // TODO: get numMovesProtection here
                numMovesProtection = Integer.parseInt(line.replaceAll("[^0-9]", ""));
                System.out.println("numMovesProtection = " + numMovesProtection);
            } else {
                throw new InvalidGameException("Unexpected EOF when parsing number of MovesProtection");
            }

            //TODO
            /**
             *  read central place here
             *  If success, assign to {@link Deserializer#centralPlace}
             *  Hint: You may use {@link Deserializer#parsePlace(String)}
             */
            line = getFirstNonEmptyLine(reader);
            if (line != null) {
                this.centralPlace = parsePlace(line);
                System.out.println(this.centralPlace.x() + "," + this.centralPlace.y());
            } else {
                throw new InvalidGameException("Unexpected EOF when parsing central place");
            }


            int numPlayers;
            line = getFirstNonEmptyLine(reader);
            if (line != null) {
                //TODO: get number of players here
                numPlayers = Integer.parseInt(line.replaceAll("[^0-9]", ""));
                System.out.println("numPlayers = " + numPlayers);
            } else {
                throw new InvalidGameException("Unexpected EOF when parsing number of players");
            }


            // TODO:
            /**
             * create an array of players {@link Player} with length of numPlayers, and name it by the read-in name
             * Also create an array representing scores {@link Deserializer#storedScores} of players with length of numPlayers
             */
            Player[] players = new Player[numPlayers];
            this.storedScores = new Integer[numPlayers];
            String playerLine1 = getFirstNonEmptyLine(reader);
            String playerLine2 = getFirstNonEmptyLine(reader);
            if (playerLine1 != null && playerLine2 != null) {
                String player1Name = playerLine1.substring(playerLine1.indexOf(":") + 1, playerLine1.indexOf(";"));
                String player2Name = playerLine2.substring(playerLine2.indexOf(":") + 1, playerLine2.indexOf(";"));
                this.storedScores[0] = Integer.parseInt(playerLine1.replaceAll("[^0-9]", ""));
                this.storedScores[1] = Integer.parseInt(playerLine2.replaceAll("[^0-9]", ""));
                players[0] = new RandomPlayer(player1Name);
                players[1] = new RandomPlayer(player2Name);
                System.out.println("player1Name = " + player1Name + " player2Name = " + player2Name);
                System.out.println("player1Score = " + storedScores[0] + " player2Score = " + storedScores[1]);

            } else {
                throw new InvalidGameException("Unexpected EOF when parsing number of players");
            }


            // TODO
            /**
             * try to initialize a configuration object  with the above read-in variables
             * if fail, throw InvalidConfigurationError exception
             * if success, assign to {@link Deserializer#configuration}
             */


            // TODO
            /**
             * Parse the string of move records into an array of {@link MoveRecord}
             * Assign to {@link Deserializer#moveRecords}
             * You should first implement the following methods:
             * - {@link Deserializer#parseMoveRecord(String)}}
             * - {@link Deserializer#parseMove(String)} ()}
             * - {@link Deserializer#parsePlace(String)} ()}
             */

        } catch (IOException ioe) {
            throw new InvalidGameException(ioe);
        }

    }

    public Configuration getLoadedConfiguration() {
        return configuration;
    }

    public Integer[] getStoredScores(){
        return storedScores;
    }

    public ArrayList<MoveRecord> getMoveRecords() {
        return moveRecords;
    }

    /**
     * Parse the string into a {@link MoveRecord}
     * Handle InvalidConfigurationError if the parse fails.
     * @param moveRecordString a string of a move record
     * @return a {@link MoveRecord}
     */
    private MoveRecord parseMoveRecord(String moveRecordString){
        // TODO
        return null;
    }

    /**
     * Parse a string of move to a {@link Move}
     * Handle InvalidConfigurationError if the parse fails.
     * @param moveString given string
     * @return {@link Move}
     */
    private Move parseMove(String moveString) {
        // TODO
        return null;
    }

    /**
     * Parse a string of place to a {@link Place}
     * Handle InvalidConfigurationError if the parse fails.
     * @param placeString given string
     * @return {@link Place}
     */
    private Place parsePlace(String placeString) {
        //TODO
        String result = placeString.substring(placeString.indexOf("(") + 1, placeString.indexOf(")"));
        var segments = result.split(",");
        if (segments.length < 2 ) {
            return null;
        }
        try {
            int x = Integer.parseInt(segments[0]);
            int y = Integer.parseInt(segments[1]);
            return new Place(x, y);
        } catch (NumberFormatException e) {
            throw new InvalidConfigurationError("Parse place record failed. Please check format!");
        }
    }


}
