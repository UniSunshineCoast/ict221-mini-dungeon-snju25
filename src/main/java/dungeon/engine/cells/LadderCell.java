package dungeon.engine.cells;

import dungeon.engine.Cell;
import dungeon.engine.Player;

import java.io.Serializable;

public class LadderCell extends Cell implements Serializable {
    private static final long serialVersionUID = 1L;
    public LadderCell(int x, int y) {
        super(Cell.Type.LADDER, x, y);
    }

    @Override
    public void interact(Player player) {
        // Ladder logic handled in GameEngine
    }
}