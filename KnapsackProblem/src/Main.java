import Structures.Item;
import Structures.Knapsack;
import Structures.Result;

import java.util.*;

public class Main {

    public static void main(String[] args) {

        //Use fixed items for proof or not?
        showProof useFixed = showProof.FALSE;

        ArrayList<Item> items;
        ArrayList<Knapsack> knapsacks;

        if(useFixed == showProof.TRUE) {
            items = createFixedItems();
            knapsacks = createFixedKnapsacks();
        } else {
            items = createRandomItems(50);
            knapsacks = createRandomKnapsacks(10);
        }

        Result greedyResult = runGreedyBestFit(items,knapsacks);
        printResult(greedyResult.knapsacks);
        items = greedyResult.items;

        System.out.println("Total value of sacks: " + valueOfAllKnapsacks(greedyResult.knapsacks));
        System.out.println("\n" + "---------------------------------------------------------------------------------" + "\n");

        ArrayList<Knapsack> neighborResult = runNeighbor(items, greedyResult.knapsacks);
        printResult(neighborResult);
        System.out.println("Total value of sacks: " + valueOfAllKnapsacks(neighborResult));

        System.out.println("\n");
        System.out.println("Leftover Items:");
        if(items.size() == 0) {
            System.out.println("None!");
        } else {
            for (Item item : items) {
                System.out.println("Item - Weight: " + item.getWeight() + " Value: " + item.getValue() + " Utility: " + item.getUtility());
            }
        }

        int greedyResultNbr = valueOfAllKnapsacks(greedyResult.knapsacks);
        int neighbourhoodResult = valueOfAllKnapsacks(neighborResult);
        System.out.println("\n\n");
        System.out.println(greedyResultNbr + " --> " + neighbourhoodResult);
    }

    //Greedy algorithms using a first-fit heuristic
    public static Result runGreedyFirstFit(ArrayList<Item> items, ArrayList<Knapsack> knapsacksParam) {

        //Create copies of the lists to work in
        ArrayList<Knapsack> knapsacks = new ArrayList<Knapsack>(knapsacksParam.size());
        for(Knapsack sack : knapsacksParam) knapsacks.add(sack.createCopy());


        //Sort items from best utility to worst
        Collections.sort(items,Comparator.comparing(Item::getUtility));
        Collections.reverse(items);

        //Standard first fit
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
        return new Result(items,knapsacks);
    }

    //Greedy algorithms using a best-fit heuristic
    public static Result runGreedyBestFit(ArrayList<Item> items, ArrayList<Knapsack> knapsacksParam) {
        //Create copies of the lists to work in
        ArrayList<Knapsack> knapsacks = new ArrayList<Knapsack>(knapsacksParam.size());
        for(Knapsack sack : knapsacksParam) knapsacks.add(sack.createCopy());
        ArrayList<Item> leftoverItems = new ArrayList<>();

        //Sort items from best utility to worst
        Collections.sort(items,Comparator.comparing(Item::getUtility));
        Collections.reverse(items);

        //Standard best fit
        for (Item item : items) {

            //Default values to be able to detect failure later
            int bestFitKnapsackIndex = Integer.MAX_VALUE;
            int bestFitKnapsackLeftover = Integer.MAX_VALUE;

            //Find best knapsack
            for (int j = 0; j < knapsacks.size(); j++) {

                //Get knapsack
                Knapsack knapsack = knapsacks.get(j);

                //Calculate leftover weight if item is inserted
                int leftoverWeight = knapsack.getFreeWeight() - item.getWeight();

                //If item can fit
                if(leftoverWeight > 0) {

                    //If current sack is better than the currently best found one.
                    if(leftoverWeight <= bestFitKnapsackLeftover) {
                        bestFitKnapsackIndex = j;
                        bestFitKnapsackLeftover = leftoverWeight;
                    }
                }
            }

            //If a fit was found
            if(bestFitKnapsackIndex != Integer.MAX_VALUE) {
                knapsacks.get(bestFitKnapsackIndex).addItem(item);
            } else {
                leftoverItems.add(item);
            }
        }
        return new Result(leftoverItems,knapsacks);
    }

    //The neighbourhood search algorithm
    public static ArrayList<Knapsack> runNeighbor(ArrayList<Item> items, ArrayList<Knapsack> greedyResult) {
        int greedyValue = valueOfAllKnapsacks(greedyResult);
        ArrayList<Knapsack> bestSolution = greedyResult;
        if(!allSacksFull(greedyResult) && items.size() > 0) { //All sacks not full and there are items left.

            for (int i = 0; i < 8; i++) {
                ArrayList<Knapsack> rotateResult = rotateItemsInKnapsacks(items,bestSolution);

                if(valueOfAllKnapsacks(rotateResult) > valueOfAllKnapsacks(bestSolution)) {
                    bestSolution = rotateResult;
                    i = 0;
                    System.out.println("better");
                }
            }
        }
        return bestSolution;
    }

    //Private method for rotating items in the knapsacks, and filling the leftover capacity. Used in neighbourhood search
    private static ArrayList<Knapsack> rotateItemsInKnapsacks(ArrayList<Item> items, ArrayList<Knapsack> knapsacks) {

        //Create a copy of the knapsacks
        ArrayList<Knapsack> knapsacksCopy = new ArrayList<Knapsack>(knapsacks.size()); //Create copy of list of knapsacks
        for(Knapsack sack : knapsacks) knapsacksCopy.add(sack.createCopy()); //Copy knapsacks to copy list

        //Iterate through all knapsacks
        for (int i = 0; i < knapsacksCopy.size(); i++) {

            //Get current knapsack
            Knapsack sack = knapsacksCopy.get(i);
            if(sack.getFreeWeight() > 0) { //If there is free space in the current knapsack

                //Get index of next knapsack, circular
                int nextKnapsackIndex = ((i + 1) % knapsacksCopy.size());

                //Continue until all knapsacks have been iterated through
                while(i != nextKnapsackIndex) {

                    //Get next knapsack
                    Knapsack nextKnapsack = knapsacksCopy.get(nextKnapsackIndex);

                    //Get heaviest item in a sack below a certain weight that fits in the current sack
                    Item heaviestItem = nextKnapsack.getHeaviestItemInKnapsackBelowOrEqualToArgumentWeight(sack.getFreeWeight());

                    //Check if an item was found
                    if(heaviestItem.getWeight() != Integer.MIN_VALUE) {

                        //Remove item from source sack
                        nextKnapsack.removeItem(heaviestItem);

                        //Add the item to destination sack
                        sack.addItem(heaviestItem);

                        //Now we need to insert an item into all that leftover capacity in nextKnapsack
                        Item bestItem = new Item(Integer.MIN_VALUE,Integer.MIN_VALUE);

                        //Iterate through all unused items
                        for(Item item : items) {

                            //Find the heaviest item in unused items
                            if(item.getWeight() > bestItem.getWeight() && item.getWeight() <= nextKnapsack.getFreeWeight()) {
                                bestItem = item;
                            }
                        }

                        //Add that item to the empty space
                        if (bestItem.getWeight() != Integer.MIN_VALUE){
                            nextKnapsack.addItem(bestItem);
                            items.remove(bestItem);
                        }
                    }

                    //Move on to the next sack
                    nextKnapsackIndex = ((nextKnapsackIndex + 1) % knapsacksCopy.size());
                }
            }
        }
        return knapsacksCopy;
    }

    /*
    ------------------------------------------------------------------------------------------------------------------------------------------------------------
    ----------------------------------------------------------------UTILITY METHODS-----------------------------------------------------------------------------
    ------------------------------------------------------------------------------------------------------------------------------------------------------------
     */

    //Check if there is any leftover space in knapsacks
    private static boolean allSacksFull(ArrayList<Knapsack> knapsacks) {
        for(Knapsack sack : knapsacks) {
            if(sack.getFreeWeight() > 0) return false;
        }
        return true;
    }

    //Calculate value of all knapsacks in list
    private static int valueOfAllKnapsacks(ArrayList<Knapsack> knapsacks) {
        int totalValue = 0;
        for(Knapsack sack : knapsacks) totalValue += sack.getValue();
        return totalValue;
    }

    //Create random sized knapsacks.
    public static ArrayList<Item> createRandomItems(int amount) {
        Random random = new Random(new Date().getTime());
        ArrayList<Item> items = new ArrayList<Item>();
        for (int i = 0; i < amount; i++) {
            items.add(new Item(random.nextInt(20)+1,random.nextInt(10)+1));
        }
        Collections.sort(items, Comparator.comparing(Item::getUtility));
        Collections.reverse(items);
        return items;
    }

    //Create knapsacks with randomized capacity
    public static ArrayList<Knapsack> createRandomKnapsacks(int amount) {
        Random random = new Random(new Date().getTime());
        ArrayList<Knapsack> knapsacks = new ArrayList<Knapsack>();
        for (int i = 0; i < amount; i++) {
            knapsacks.add(new Knapsack(10 + random.nextInt(11))); //Capacity is 10-20
        }
        Collections.sort(knapsacks, Comparator.comparing(Knapsack::getMaxCapacity));
        Collections.reverse(knapsacks);
        return knapsacks;
    }

    //Create a pre-defined set of knapsacks for testing
    public static ArrayList<Knapsack> createFixedKnapsacks() {
        ArrayList<Knapsack> knapsacks = new ArrayList<Knapsack>();
        knapsacks.add(new Knapsack(15));
        knapsacks.add(new Knapsack(10));
        return knapsacks;
    }

    //Create a pre-defined set of items for testing
    public static ArrayList<Item> createFixedItems() {
        ArrayList<Item> items = new ArrayList<Item>();
        items.add(new Item(5,10)); //0.5
        items.add(new Item(4,4)); //1
        items.add(new Item(7,5)); //1,4
        items.add(new Item(12,6)); //2
        return items;
    }

    //Print knapsacks nicely
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

    private enum showProof {
        TRUE,FALSE;
    }
}