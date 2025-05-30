package dungeon.engine.cells;

import dungeon.engine.Cell;
import dungeon.engine.Player;

import java.io.Serializable;
import java.util.Random;

public class RangedMutantCell extends Cell implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Random RAND = new Random();

    public RangedMutantCell(int x, int y) {
        super(Cell.Type.RANGED_MUTANT, x, y);
    }

    @Override
    public void interact(Player player) {
        player.addScore(2);
    }

    public boolean attemptAttack() {
        return RAND.nextDouble() < 0.5;
    }
}