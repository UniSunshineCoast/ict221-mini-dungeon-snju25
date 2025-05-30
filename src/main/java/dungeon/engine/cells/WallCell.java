package dungeon.engine.cells;

import dungeon.engine.Cell;
import dungeon.engine.Player;

import java.io.Serializable;

public class WallCell extends Cell implements Serializable {
    private static final long serialVersionUID = 1L;
    public WallCell(int x, int y) {
        super(Cell.Type.WALL, x, y);
    }

    @Override
    public void interact(Player player) {
        // Walls block movement but don't interact
    }
}