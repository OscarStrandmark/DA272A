package Structures;

import java.util.ArrayList;

public class Result {
    public ArrayList<Item> items;
    public ArrayList<Knapsack> knapsacks;

    public Result(ArrayList<Item> items, ArrayList<Knapsack> knapsacks) {
        this.items = items;
        this.knapsacks = knapsacks;
    }
}
