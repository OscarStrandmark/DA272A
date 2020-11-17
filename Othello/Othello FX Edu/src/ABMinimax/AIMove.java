package ABMinimax;

import com.eudycontreras.othello.capsules.AgentMove;

public class AIMove extends AgentMove {
    @Override
    public boolean isValid() {
        return false;
    }

    @Override
    public int compareTo(AgentMove o) {
        return 0;
    }
}
