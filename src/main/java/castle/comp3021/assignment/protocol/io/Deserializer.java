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
        while ((line = br.readLine()) != null && !line.equals("END")) {
            if (!line.isEmpty() && !line.contains("#")) {
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
                String subLine = line.substring(line.indexOf(":") + 1).strip();
                try {
                    size = Integer.parseInt(subLine);
//                    System.out.println("size =  " + size);
                } catch (Exception ex) {
                    throw new InvalidConfigurationError("Parse board size failed. Please check format! " +
                            "For input string: \""+ subLine + "\"");
                }
            } else {
                throw new InvalidGameException("Unexpected EOF when parsing number of board size");
            }

            int numMovesProtection;
            line = getFirstNonEmptyLine(reader);
            if (line != null) {
                // TODO: get numMovesProtection here
                String subLine = line.substring(line.indexOf(":") + 1).strip();
                try {
                    numMovesProtection = Integer.parseInt(subLine);
//                    System.out.println("numMovesProtection = " + numMovesProtection);
                } catch (Exception ex) {
                    throw new InvalidConfigurationError("Parse num of move protection failed. Please check format! " +
                            "For input string: \""+ subLine + "\"");
                }
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
//                System.out.println(this.centralPlace.x() + "," + this.centralPlace.y());
            } else {
                throw new InvalidGameException("Unexpected EOF when parsing central place");
            }


            int numPlayers;
            line = getFirstNonEmptyLine(reader);
            if (line != null) {
                String subLine = line.substring(line.indexOf(":") + 1).strip();
                //TODO: get number of players here
                try {
                    numPlayers = Integer.parseInt(subLine);
//                    System.out.println("numPlayers = " + numPlayers);
                } catch (Exception ex) {
                    throw new InvalidConfigurationError("Parse num of players failed. Please check format! " +
                            "For input string: \""+ subLine + "\"");
                }
            } else {
                throw new InvalidGameException("Unexpected EOF when parsing number of players");
            }



            // TODO:
            /**
             * create an array of players {@link Player} with length of numPlayers, and name it by the read-in name
             * Also create an array representing scores {@link Deserializer#storedScores} of players with length of numPlayers
             */
            Player[] players = new Player[numPlayers];  // check 2 players??
            this.storedScores = new Integer[numPlayers];
            String[] playerLines = new String[numPlayers];
            for (int i = 0; i < numPlayers; i++) {
                playerLines[i] = getFirstNonEmptyLine(reader);
                if (playerLines[i] != null) {
                    String[] segments = playerLines[i].split(";");
                    if (segments.length != 2) {
                        throw new InvalidConfigurationError("Parse player info failed. Please check format!");
                    }
                    if (!segments[0].contains("name:")) {
                        throw new InvalidConfigurationError("Parse player name failed. Please check format!");
                    }
                    if (!segments[1].contains("score:")) {
                        throw new InvalidConfigurationError("Parse player score failed. Please check format!");
                    }

                    String subNameLine = segments[0].substring(segments[0].indexOf(":") + 1).strip();
                    String subLine = segments[1].substring(segments[1].indexOf(":") + 1).strip();
                    players[i] = new RandomPlayer(subNameLine);
                    try {
                        this.storedScores[i] = Integer.parseInt(subLine);
                    } catch (Exception ex) {
                        throw new InvalidConfigurationError("Parse player score failed. Please check format! " +
                                "For input string: \""+ subLine + "\"");
                    }
//                    System.out.printf("player %d name = %s\n", i, players[i].getName());
//                    System.out.printf("player %d score = %d\n", i, storedScores[i]);
                } else {
                    throw new InvalidGameException("Unexpected EOF when parsing number of players");
                }
            }


            // TODO
            /**
             * try to initialize a configuration object with the above read-in variables
             * if fail, throw InvalidConfigurationError exception
             * if success, assign to {@link Deserializer#configuration}
             */
            this.configuration = new Configuration(players);
            this.configuration.setSize(size);
            this.configuration.setNumMovesProtection(numMovesProtection);



            // TODO
            /**
             * Parse the string of move records into an array of {@link MoveRecord}
             * Assign to {@link Deserializer#moveRecords}
             * You should first implement the following methods:
             * - {@link Deserializer#parseMoveRecord(String)}}
             * - {@link Deserializer#parseMove(String)} ()}
             * - {@link Deserializer#parsePlace(String)} ()}
             */
            while ((line = getFirstNonEmptyLine(reader)) != null) {
                this.moveRecords.add(parseMoveRecord(line));
            }
//            for (var mov : moveRecords) {
//                System.out.println(mov.getPlayer().getName() + " = " + mov.getMove().toString());
//            }

        } catch (IOException ioe) {
            throw new InvalidGameException(ioe);
        }

    }

    public Place getCentralPlace() {
        return centralPlace;}

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
    private MoveRecord parseMoveRecord(String moveRecordString) {
        // TODO
        var segments = moveRecordString.split(";");
        if (segments.length != 2) {
            throw new InvalidConfigurationError("Parse move record failed. Please check format!");
        }
        if (!segments[0].contains("player:") || !segments[1].contains("move:")) {
            throw new InvalidConfigurationError("Parse player move failed. Please check format!");
        }
        // player name is not important here
        String moveLine = segments[1].substring(segments[1].indexOf(":") + 1).strip();
        var move = parseMove(moveLine);
        if (move != null) {
            return new MoveRecord(this.configuration.getPlayers()[this.moveRecords.size() % 2], move);
        } else {
            throw new InvalidConfigurationError("One move should contain both source and target!");
        }
    }

    /**
     * Parse a string of move to a {@link Move}
     * Handle InvalidConfigurationError if the parse fails.
     * @param moveString given string
     * @return {@link Move}
     */
    private Move parseMove(String moveString) {
        // TODO
        var segments = moveString.split("->");
        if (segments.length != 2) {
            return null;
        }
        Place source = parsePlace(segments[0].strip());
        Place destination = parsePlace(segments[1].strip());
        return new Move(source, destination);
    }

    /**
     * Parse a string of place to a {@link Place}
     * Handle InvalidConfigurationError if the parse fails.
     * @param placeString given string
     * @return {@link Place}
     */
    private Place parsePlace(String placeString) {
        //TODO
        String subLine = placeString.substring(placeString.indexOf("(") + 1, placeString.indexOf(")"));
        var segments = subLine.split(",");
        if (segments.length != 2 ) {
            throw new InvalidConfigurationError("Parse place record failed. Please check format! " +
                    "Should be only 2 elements(x,y), but " + segments.length + " found");
        }
        try {
            int x = Integer.parseInt(segments[0].strip());
            int y = Integer.parseInt(segments[1].strip());
            return new Place(x, y);
        } catch (NumberFormatException e) {
            throw new InvalidConfigurationError("Parse place record failed. Please check format! " +
                    "For input string: \"("+ subLine + ")\"");
        }
    }


}
