package dungeon.engine.cells;

import dungeon.engine.Cell;
import dungeon.engine.Player;

import java.io.Serializable;

public class GoldCell extends Cell implements Serializable {
    private static final long serialVersionUID = 1L;
    public GoldCell(int x, int y) {
        super(Cell.Type.GOLD, x, y);
    }

    @Override
    public void interact(Player player) {
        player.addScore(2);
    }
}