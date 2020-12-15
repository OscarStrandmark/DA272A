package Structures;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Knapsack {
    private int maxCapacity;
    private ArrayList<Item> items;

    public Knapsack(int capacity) {
        this.maxCapacity = capacity;
        this.items = new ArrayList<Item>();
    }

    public int getCurrentWeight() {
        int weight = 0;
        for(Item i : items) {
            weight += i.getWeight();
        }
        return weight;
    }

    public int getValue() {
        int value = 0;
        for(Item i : items) {
            value += i.getValue();
        }
        return value;
    }

    public void addItem(Item item) {
        if(item.getWeight() <= (maxCapacity - getCurrentWeight())) {
            items.add(item);
        }
        Collections.sort(items, Comparator.comparing(Item::getUtility)); //Sort
    }

    public Item getItem(int i) {
        return items.get(i);
    }

    public Item getHeaviestItemInKnapsackBelowOrEqualToArgumentWeight(int weight) {
        Item item = new Item(Integer.MIN_VALUE,Integer.MIN_VALUE);
        for(Item i : items) {
            if(i.getWeight() > item.getWeight() && i.getWeight() <= weight) {
                item = i;
            }
        }
        return item;
    }

    public ArrayList<Item> getAllItems() {
        return items;
    }

    public Item removeItem(int i) {
        return items.remove(i);
    }

    public void removeItem(Item item) {
        items.remove(item);
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public boolean itemFits(Item item) {
        return item.getWeight() <= (maxCapacity - getCurrentWeight());
    }

    public int getFreeWeight() {
        return getMaxCapacity() - getCurrentWeight();
    }

    public boolean isFull() {
        return (getMaxCapacity() == getCurrentWeight());
    }

    public Knapsack createCopy() {
        Knapsack copy = new Knapsack(maxCapacity);
        for(Item i : items) {
            copy.addItem(new Item(i.getValue(),i.getWeight()));
        }
        return copy;
    }
}
