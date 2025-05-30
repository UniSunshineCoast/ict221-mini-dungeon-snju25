package dungeon.engine.cells;

import dungeon.engine.Cell;
import dungeon.engine.Player;

import java.io.Serializable;
import java.util.Random;

public class MeleeMutantCell extends Cell implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Random RAND = new Random();

    public MeleeMutantCell(int x, int y) {
        super(Cell.Type.MELEE_MUTANT, x, y);
    }

    @Override
    public void interact(Player player) {
        player.takeDamage(2);
        player.addScore(2);
    }
}