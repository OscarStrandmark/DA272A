package Structures;

import java.util.*;

public class Main {

    public static void main(String[] args) {
        ArrayList<Item> items = createRandomItems(100);
        ArrayList<Knapsack> knapsacks = createRandomKnapsacks(10);

        ArrayList<Knapsack> greedyResult = runGreedy(items,knapsacks);
        printResult(greedyResult);

        System.out.println("Total value of sacks: " + valueOfAllKnapsacks(greedyResult));
        System.out.println("\n" + "---------------------------------------------------------------------------------" + "\n");

        ArrayList<Knapsack> neighborResult = runNeighbor(items, greedyResult);
        printResult(neighborResult);
        System.out.println("Total value of sacks: " + valueOfAllKnapsacks(neighborResult));

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
            items.add(new Item(random.nextInt(50)+1,random.nextInt(20)+4));
        }
        Collections.sort(items, Comparator.comparing(Item::getUtility));
        Collections.reverse(items);
        return items;
    }

    public static ArrayList<Knapsack> createRandomKnapsacks(int amount) {
        Random random = new Random(new Date().getTime());
        ArrayList<Knapsack> knapsacks = new ArrayList<Knapsack>();
        for (int i = 0; i < amount; i++) {
            knapsacks.add(new Knapsack(25 + random.nextInt(11))); //Capacity is 20-30
        }
        Collections.sort(knapsacks, Comparator.comparing(Knapsack::getMaxCapacity));
        Collections.reverse(knapsacks);
        return knapsacks;
    }

    //All arrays are already sorted, done outside this method for readability.
    public static ArrayList<Knapsack> runGreedy(ArrayList<Item> items, ArrayList<Knapsack> knapsacks) {
        Collections.sort(items,Comparator.comparing(Item::getUtility));
        Collections.reverse(items);
        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            for (int j = 0; j < knapsacks.size(); j++) {
                Knapsack knapsack = knapsacks.get(j);
                if (knapsack.itemFits(item)) {
                    items.remove(i);
                    knapsack.addItem(item);
                    break;
                }
            }

        }
        return knapsacks;
    }

    public static ArrayList<Knapsack> runNeighbor(ArrayList<Item> items, ArrayList<Knapsack> greedyResult) {
        int greedyValue = valueOfAllKnapsacks(greedyResult);
        ArrayList<Knapsack> bestSolution = greedyResult;
        if(!allSacksFull(greedyResult) && items.size() > 0) { //All sacks not full and there are items left.

            ArrayList<Knapsack> rotateResult = rotateItemsInKnapsacks(items,greedyResult);

            if(valueOfAllKnapsacks(rotateResult) > valueOfAllKnapsacks(greedyResult)) {
                bestSolution = rotateResult;
            }
        }

        return bestSolution;
    }

    private static ArrayList<Knapsack> rotateItemsInKnapsacks(ArrayList<Item> items, ArrayList<Knapsack> knapsacks) {
        ArrayList<Knapsack> knapsacksCopy = new ArrayList<Knapsack>(knapsacks.size()); //Create copy of list of knapsacks
        for(Knapsack sack : knapsacks) knapsacksCopy.add(sack.createCopy()); //Copy knapsacks to copy list
        for (int i = 0; i < knapsacksCopy.size(); i++) { //Iterate through all knapsacks
            Knapsack sack = knapsacksCopy.get(i); //Get current knapsack
            if(sack.getFreeWeight() > 0) { //If there is free space in the current knapsack
                int nextKnapsackIndex = ((i + 1) % knapsacksCopy.size()); //Get index of next knapsack, circular
                while(i != nextKnapsackIndex) { //Check all other knapsacks
                    Knapsack nextKnapsack = knapsacksCopy.get(nextKnapsackIndex); //Get next knapsack
                    Item heaviestItem = nextKnapsack.getHeaviestItemInKnapsackBelowOrEqualToArgumentWeight(sack.getFreeWeight()); //Get heaviest item below a certain weight that fits in the current sack
                    if(heaviestItem.getWeight() != Integer.MIN_VALUE) { //Check if an item was found
                        nextKnapsack.removeItem(heaviestItem); //Remove item from source sack
                        sack.addItem(heaviestItem); //Add the item to destination sack
                        Item bestItem = new Item(Integer.MIN_VALUE,Integer.MIN_VALUE); //Now we need to insert an item into all that leftover capacity in nextKnapsack
                        for(Item item : items) { //Iterate through all unused items
                            if(item.getWeight() > bestItem.getWeight() && item.getWeight() <= nextKnapsack.getFreeWeight()) { //Find the heaviest item in unused items
                                bestItem = item;
                            }
                        }
                        if (bestItem.getWeight() != Integer.MIN_VALUE) nextKnapsack.addItem(bestItem); //Add that item to the empty space
                    }
                    nextKnapsackIndex = ((nextKnapsackIndex + 1) % knapsacksCopy.size()); //Move on to the next sack
                }
            }
        }
        return knapsacksCopy;
    }

    //Check if there is any leftover space in knapsacks
    private static boolean allSacksFull(ArrayList<Knapsack> knapsacks) {
        for(Knapsack sack : knapsacks) {
            if(sack.getFreeWeight() > 0) return false;
        }
        return true;
    }

    private static int valueOfAllKnapsacks(ArrayList<Knapsack> knapsacks) {
        int totalValue = 0;
        for(Knapsack sack : knapsacks) totalValue += sack.getValue();
        return totalValue;
    }
}