package castle.comp3021.assignment.piece;

import castle.comp3021.assignment.protocol.Game;
import castle.comp3021.assignment.protocol.Move;
import castle.comp3021.assignment.protocol.Player;

/**
 * Global rule that requires the moving piece must belong to current player.
 */
public class BelongingRule implements Rule {

    private Player currentPlayer;

    public BelongingRule(Player currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    @Override
    public boolean validate(Game game, Move move) {
        var source = move.getSource();
        if (!game.getPiece(source).getPlayer().equals(this.currentPlayer)) {
            return false;
        }
        return true;
    }

    @Override
    public String getDescription() {
        return "The piece you moved does not belong to you!";
    }
}
