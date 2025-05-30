package dungeon.engine.cells;

import dungeon.engine.Cell;
import dungeon.engine.Player;

import java.io.Serializable;

public class EmptyCell extends Cell implements Serializable {
    private static final long serialVersionUID = 1L;
    public EmptyCell(int x, int y) {
        super(Cell.Type.EMPTY, x, y);
    }

    @Override
    public void interact(Player player) {
        // Empty cells do nothing
    }
}