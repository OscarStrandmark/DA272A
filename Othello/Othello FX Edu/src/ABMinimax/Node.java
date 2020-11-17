package ABMinimax;

import java.util.ArrayList;

public class Node {

    private Node parent;
    private ArrayList<Node> children;

    public int alfa = Integer.MIN_VALUE;
    public int beta = Integer.MAX_VALUE;

    public Node(Node parent) {
        children = new ArrayList<Node>();
        this.parent = parent;
    }

    public ArrayList<Node> getChildren() { return children; }
    public void addChild(Node child) { children.add(child); }
}
