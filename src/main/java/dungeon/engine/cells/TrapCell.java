package dungeon.engine.cells;

import dungeon.engine.Cell;
import dungeon.engine.Player;

import java.io.Serializable;

public class TrapCell extends Cell implements Serializable {
    private static final long serialVersionUID = 1L;
    public TrapCell(int x, int y) {
        super(Cell.Type.TRAP, x, y);
    }

    @Override
    public void interact(Player player) {
        player.takeDamage(2);
    }
}