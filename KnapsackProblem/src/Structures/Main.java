package Structures;

import java.util.*;

public class Main {

    public static void main(String[] args) {
        ArrayList<Item> items = createRandomItems(100);
        ArrayList<Knapsack> knapsacks = createRandomKnapsacks(5);
        ArrayList<Knapsack> greedyResult = runGreedy(items,knapsacks);
        printResult(greedyResult);
    }

    public static void printResult(ArrayList<Knapsack> knapsacks) {
        for (int i = 0; i < knapsacks.size(); i++) {
            Knapsack knapsack = knapsacks.get(i);
            ArrayList<Item> items = knapsack.getAllItems();
            System.out.println(String.format("Knapsack (%s/%s) Value = %s {",knapsack.getCurrentWeight(),knapsack.getMaxCapacity(),knapsack.getValue()));
            for (Item item : items) {
                System.out.println(String.format("    Item(%s,%s), utility: = ",item.getWeight(),item.getValue()) + item.getUtility());
            }
            System.out.println("}");
        }
    }

    public static ArrayList<Item> createRandomItems(int amount) {
        Random random = new Random(new Date().getTime());
        ArrayList<Item> items = new ArrayList<Item>();
        for (int i = 0; i < amount; i++) {
            items.add(new Item(random.nextInt(10)+1,random.nextInt(10)+1));
        }
        Collections.sort(items, Comparator.comparing(Item::getUtility));
        Collections.reverse(items);
        return items;
    }

    public static ArrayList<Knapsack> createRandomKnapsacks(int amount) {
        Random random = new Random(new Date().getTime());
        ArrayList<Knapsack> knapsacks = new ArrayList<Knapsack>();
        for (int i = 0; i < amount; i++) {
            knapsacks.add(new Knapsack(20 + random.nextInt(11))); //Capacity is 20-30
        }
        Collections.sort(knapsacks, Comparator.comparing(Knapsack::getMaxCapacity));
        Collections.reverse(knapsacks);
        return knapsacks;
    }

    //All arrays are already sorted, done outside this method for readability.
    public static ArrayList<Knapsack> runGreedy(ArrayList<Item> items, ArrayList<Knapsack> knapsacks) {
        Collections.sort(items,Comparator.comparing(Item::getUtility));
        Collections.reverse(items);
        int fitNotFoundCount = 0;
        for (int i = 0; i < items.size(); i++) {
            if (fitNotFoundCount > Integer.MAX_VALUE) break;
            Item item = items.get(i);
            for (int j = 0; j < knapsacks.size(); j++) {
                Knapsack knapsack = knapsacks.get(j);
                if (knapsack.itemFits(item)) {
                    items.remove(i);
                    knapsack.addItem(item);
                    item = null;
                    break;
                }
            }
            if (item != null) {
                fitNotFoundCount++;
            }
        }
        return knapsacks;
    }
}