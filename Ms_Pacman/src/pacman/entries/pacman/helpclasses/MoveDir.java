package pacman.entries.pacman.helpclasses;

import pacman.game.Constants.MOVE;

public class MoveDir {
    public MOVE move;
    public int count;

    public MoveDir(MOVE move) {
        this.move = move;
        count = 0;
    }

    public MOVE getMove() {
        return move;
    }

    public int getCount() {
        return count;
    }

    public void incrementCount() {
        count++;
    }
}
