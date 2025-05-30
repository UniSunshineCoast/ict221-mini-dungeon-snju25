package dungeon.engine.cells;

import dungeon.engine.Cell;
import dungeon.engine.Player;

import java.io.Serializable;

public class HealthPotionCell extends Cell implements Serializable {
    private static final long serialVersionUID = 1L;
    public HealthPotionCell(int x, int y) {
        super(Cell.Type.HEALTH_POTION, x, y);
    }

    @Override
    public void interact(Player player) {
        player.heal(4);
    }
}