import dungeon.engine.Cell;
import dungeon.engine.GameEngine;
import dungeon.engine.Player;
import dungeon.engine.cells.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestGameEngine {

    private GameEngine ge;

    @BeforeEach
    void setUp() {
        ge = new GameEngine(3, false); // size = 3, textMode = false
    }

    @Test
    void testGetSize() {
        assertEquals(12, ge.getSize(), "Game map size should be 12");
    }

    @Test
    void testInitialPlayerPosition() {
        Player p = ge.getPlayer();
        assertEquals(1, p.getX());
        assertEquals(10, p.getY());
    }

    @Test
    void testWallBoundaries() {
        Cell[][] map = ge.getMap();
        int n = ge.getSize();
        for (int i = 0; i < n; i++) {
            assertEquals(Cell.Type.WALL, map[0][i].getType());
            assertEquals(Cell.Type.WALL, map[n - 1][i].getType());
            assertEquals(Cell.Type.WALL, map[i][0].getType());
            assertEquals(Cell.Type.WALL, map[i][n - 1].getType());
        }
    }

    @Test
    void testInvalidMoveDoesNotChangePositionOrSteps() {
        int beforeSteps = ge.getStepsRemaining();
        Player p = ge.getPlayer();
        int x = p.getX();
        int y = p.getY();

        boolean moved = ge.movePlayer(-1, 0); // Move into wall
        assertFalse(moved);
        assertEquals(x, p.getX());
        assertEquals(y, p.getY());
        assertEquals(beforeSteps, ge.getStepsRemaining());
    }

    @Test
    void testValidMoveDecrementsSteps() {
        int beforeSteps = ge.getStepsRemaining();
        boolean moved = ge.movePlayer(0, -1); // Move up
        assertTrue(moved);
        assertEquals(beforeSteps - 1, ge.getStepsRemaining());
    }

    @Test
    void testGoldPickup() {
        Player p = ge.getPlayer();
        int x = p.getX();
        int y = p.getY() - 1;
        ge.getMap()[y][x] = new GoldCell(x, y);

        ge.movePlayer(0, -1);
        assertEquals(2, p.getScore());
        assertEquals(Cell.Type.EMPTY, ge.getMap()[y][x].getType());
    }

    @Test
    void testTrapDamage() {
        Player p = ge.getPlayer();
        int x = p.getX() + 1;
        int y = p.getY();
        ge.getMap()[y][x] = new TrapCell(x, y);

        int beforeHp = p.getHp();
        ge.movePlayer(1, 0);
        assertEquals(beforeHp - 2, p.getHp());
        assertEquals(Cell.Type.TRAP, ge.getMap()[y][x].getType());
    }

    @Test
    void testHealthPotion() {
        Player p = ge.getPlayer();
        p.takeDamage(5);
        int x = p.getX();
        int y = p.getY() - 1;
        ge.getMap()[y][x] = new HealthPotionCell(x, y);

        ge.movePlayer(0, -1);
        assertTrue(p.getHp() > 5 && p.getHp() <= 10);
        assertEquals(Cell.Type.EMPTY, ge.getMap()[y][x].getType());
    }

    @Test
    void testMeleeMutantInteraction() {
        Player p = ge.getPlayer();
        int x = p.getX() + 1;
        int y = p.getY();
        ge.getMap()[y][x] = new MeleeMutantCell(x, y);
        p.takeDamage(4);

        ge.movePlayer(1, 0);

        assertEquals(2, p.getScore());
        assertEquals(Cell.Type.EMPTY, ge.getMap()[y][x].getType());
        assertEquals(4, p.getHp()); // Mutant did 2 damage
    }

    @Test
    void testStepsAfterMultipleMoves() {
        int start = ge.getStepsRemaining();
        ge.movePlayer(0, -1);
        ge.movePlayer(0, -1);
        ge.movePlayer(1, 0);
        assertEquals(start - 3, ge.getStepsRemaining());
    }
}
