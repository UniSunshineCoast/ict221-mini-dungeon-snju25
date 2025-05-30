package dungeon.engine.cells;

import dungeon.engine.Cell;
import dungeon.engine.Player;

import java.io.Serializable;

public class EntryCell extends Cell implements Serializable {
    private static final long serialVersionUID = 1L;
    public EntryCell(int x, int y) {
        super(Cell.Type.ENTRY, x, y);
    }

    @Override
    public void interact(Player player) {
        // Entry point doesn't do anything
    }
}