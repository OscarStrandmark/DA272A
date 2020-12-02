package Structures;

public class Item {

    private int value;
    private int weight;
    private double utility;

    public Item(int value, int weight) {
        this.value = value;
        this.weight = weight;
        this.utility = ((double)value / (double)weight);
    }

    public int getWeight() {
        return weight;
    }

    public int getValue() {
        return value;
    }

    public double getUtility() {
        return utility;
    }
}
