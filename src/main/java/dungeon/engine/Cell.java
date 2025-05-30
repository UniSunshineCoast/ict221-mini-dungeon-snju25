package dungeon.engine;

import java.io.Serializable;

public abstract class Cell implements Serializable {
    private static final long serialVersionUID = 1L;
    public enum Type { WALL, ENTRY, LADDER, TRAP, GOLD, MELEE_MUTANT, RANGED_MUTANT, HEALTH_POTION, EMPTY }

    protected final Type type;
    protected final int x;
    protected final int y;

    public Cell(Type type, int x, int y) {
        this.type = type;
        this.x = x;
        this.y = y;
    }

    public Type getType() { return type; }
    public int getX() { return x; }
    public int getY() { return y; }

    public abstract void interact(Player player);
}