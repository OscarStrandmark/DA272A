package pacman.entries.pacman;

import java.util.ArrayList;

public class Node {

    private String label;

    public ArrayList<Node> children = new ArrayList<Node>();

    public Node(){}

    public Node(String label) {
        this.label = label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLabel() { return label; }

    public void addChild(Node node) { children.add(node); }
}
