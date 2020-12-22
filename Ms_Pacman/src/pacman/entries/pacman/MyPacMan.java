package pacman.entries.pacman;

import dataRecording.DataSaverLoader;
import dataRecording.DataTuple;
import pacman.controllers.Controller;
import pacman.entries.pacman.helpclasses.MoveDir;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

import javax.xml.crypto.Data;
import java.util.*;

/*
 * This is the class you need to modify for your entry. In particular, you need to
 * fill in the getAction() method. Any additional classes you write should either
 * be placed in this package or sub-packages (e.g., game.entries.pacman.mypackage).
 */
public class MyPacMan extends Controller<MOVE>
{
	private MOVE myMove=MOVE.NEUTRAL;

	private ArrayList<DataTuple> trainingData = new ArrayList<DataTuple>();
	private ArrayList<DataTuple> testData = new ArrayList<DataTuple>();

	private HashMap<String,ArrayList<String>> attributes = new HashMap<String,ArrayList<String>>();

	private Node rootNode;


	public MyPacMan () {
		fetchData();
		initLists();
		rootNode = buildTree(trainingData,attributes);
	}

	public MOVE getMove(Game game, long timeDue) 
	{
		return traverseTree(rootNode,new DataTuple(game,null));
	}

	private MOVE traverseTree(Node node, DataTuple tuple) {
		MOVE m = null;

		if(node.children.size() == 0) { //If leaf node
			m = MOVE.valueOf( node.getLabel() );
		} else {
			//Get label of attribute
			String attributeLabel = tuple.getValueOfAttribute(node.getLabel());

			//Var for holding next node to traverse.
			Node nextNode = null;

			//Get all children nodes of node
			ArrayList<Node> children = node.children;
			for(Node child : children) {
				if(child.getLabel().equals(attributeLabel)) {
					nextNode = child;
				}
			}

			m = traverseTree(nextNode,tuple);
		}

		return m;
	}

	private Node buildTree(ArrayList<DataTuple> D, HashMap<String,ArrayList<String>> attributeList) {

		// 1: Create root node
		Node N = new Node();

		// 2: If every tuple in D has the same class C, return N as a leaf node labeled as C.
		MOVE move = D.get(0).DirectionChosen;
		boolean classDiff = false;
		for (int i = 1; i < D.size(); i++) {
			if(D.get(i).DirectionChosen != move) {
				classDiff = true;
				i = D.size();
			}
		}
		if(!classDiff) {
			N.setLabel(move.toString());
			return N;
		}

		// 3: If the attribute list is empty, return N as a leaf node labeled with the majority class in D
		if(attributeList.size() == 0) {
			N.setLabel(classMajority(D).toString());
			return N;
		}

		// 4.1: Call attribute selection method on D and the attribute list, in order to choose the current attribute A
		String A = S(D,attributeList);

		// 4.2 Label N as A and remove A from the attribute list.
		N.setLabel(A);
		ArrayList<String> valuesOfA = attributeList.remove(A);

		// 4.3 For each value a_j in attribute A
		for(String a_j : valuesOfA) {

			// 4.3.1 Seperate all tuples in D so that attribute A takes the value a_j, creating the subset D_j
			ArrayList<DataTuple> D_j = new ArrayList<DataTuple>();
			for(DataTuple dataTuple : D) {
				if(dataTuple.getValueOfAttribute(A).equals(a_j)) {
					D_j.add(dataTuple);
				}
			}

			// 4.3.2 If D_j is empty, add a child node to N labeled with the majority class in D.
			if(D_j.size() == 0) {
				N.addChild(new Node(classMajority(D).toString()));
			} else {
				// 4.3.3 Otherwise, add the resulting node from calling buildTree(D_j,attribute) as a child node to N.
				N.addChild(buildTree(D_j,(HashMap<String,ArrayList<String>>)attributeList.clone()));
			}
		}

		// 4.4 Return N
		return N;
	}

	/**
	 * S is the ID3 algorithm used in building the tree.
	 * @param data
	 * @param attributeValues
	 * @return
	 */
	private String S(ArrayList<DataTuple> data, HashMap<String,ArrayList<String>> attributeValues) {
		//Variable for holding the return string
		String retVal = "";

		//Variable for holding the best information gain value. Set large at start for comparison later.
		double infoGainOnAofD = 999999999;

		//Create list of attributes
		ArrayList<String> attributeList = new ArrayList<>(attributeValues.keySet());

		//Iterate through all attributes
		for (String attribute : attributeList) {

			//Var for holding the info gain on the current attribute.
			double infoThisAttribute = 0.0;

			//Get all possible values for an attribute
			ArrayList<String> possibleAttributeValues = attributeValues.get(attribute);

			HashMap<String,Integer> valueCountMap = new HashMap<String,Integer>();

			//For every possible value of the current attribute
			for(String attributeValue : possibleAttributeValues) {

				//Create empty subset of tuples
				ArrayList<DataTuple> subset = new ArrayList<DataTuple>();

				//Count occurrences
				valueCountMap.put(attributeValue,0);

				//Iterate though all data
				for(DataTuple tuple : data) {

					//If the tuple in the data set has the value of the currently iterating attribute value
					if(tuple.getValueOfAttribute(attribute).equals(attributeValue)) {

						//Increase attribute value count by 1
						valueCountMap.put(attributeValue,valueCountMap.get(attributeValue) + 1);

						//Add the tuple to the subset
						subset.add(tuple);
					}
				}

				//Map for holding direction count values
				HashMap<MOVE,Integer> directionCountMap = new HashMap<MOVE,Integer>();
				directionCountMap.put(MOVE.UP,0);
				directionCountMap.put(MOVE.DOWN,0);
				directionCountMap.put(MOVE.LEFT,0);
				directionCountMap.put(MOVE.RIGHT,0);
				directionCountMap.put(MOVE.NEUTRAL,0);

				//Count occurrences of the different DirectionChosen values in the subset.
				for(DataTuple tuple : subset) {
					switch (tuple.DirectionChosen) {
						case UP:
							directionCountMap.put(MOVE.UP,directionCountMap.get(MOVE.UP) + 1);
							break;
						case DOWN:
							directionCountMap.put(MOVE.DOWN,directionCountMap.get(MOVE.DOWN) + 1);
							break;
						case LEFT:
							directionCountMap.put(MOVE.LEFT,directionCountMap.get(MOVE.LEFT) + 1);
							break;
						case RIGHT:
							directionCountMap.put(MOVE.RIGHT,directionCountMap.get(MOVE.RIGHT) + 1);
							break;
						case NEUTRAL:
							directionCountMap.put(MOVE.NEUTRAL,directionCountMap.get(MOVE.NEUTRAL) + 1);
							break;
					}
				}

				//Get count of occurrences of the given attribute value
				double valueCount = valueCountMap.get(attributeValue);

				//If there are any occurrences5
				if(valueCount != 0) {
					//Get value from the map for better readability
					int upCount = directionCountMap.get(MOVE.UP);
					int downCount = directionCountMap.get(MOVE.DOWN);
					int leftCount = directionCountMap.get(MOVE.LEFT);
					int rightCount = directionCountMap.get(MOVE.RIGHT);
					int neutralCount = directionCountMap.get(MOVE.NEUTRAL);

					//Math
					double AttributeValueOccurrences = (valueCount / data.size());
					double upInSubset = ((upCount / valueCount) * (MathHelper.Log2((upCount / valueCount))));
					double downInSubset = ((downCount / valueCount) * (MathHelper.Log2((downCount / valueCount))));
					double leftInSubset = ((leftCount / valueCount) * (MathHelper.Log2((leftCount / valueCount))));
					double rightInSubset = ((rightCount / valueCount) * (MathHelper.Log2((rightCount / valueCount))));
					double neutralInSubset = ((neutralCount / valueCount) * (MathHelper.Log2((neutralCount / valueCount))));


					infoThisAttribute = AttributeValueOccurrences * (- upInSubset - downInSubset - rightInSubset - leftInSubset - neutralInSubset);
				}
			}

			if(infoThisAttribute < infoGainOnAofD) {
				infoGainOnAofD = infoThisAttribute;
				retVal = attribute;
			}
		}
		if(retVal.equals("")) {
			int nbr = 0;
		}
		return retVal;
	}

	/**
	 * Get the majority class of the parameter data
	 * @param data
	 * @return a MOVE
	 */
	private MOVE classMajority (ArrayList<DataTuple> data) {
		ArrayList<MoveDir> moveArr = new ArrayList<MoveDir>();
		moveArr.add(new MoveDir(MOVE.UP));
		moveArr.add(new MoveDir(MOVE.DOWN));
		moveArr.add(new MoveDir(MOVE.LEFT));
		moveArr.add(new MoveDir(MOVE.RIGHT));
		moveArr.add(new MoveDir(MOVE.NEUTRAL));

		for (DataTuple tuple : data) {
			switch (tuple.DirectionChosen) {
				case UP:
					moveArr.get(0).incrementCount();
					break;
				case DOWN:
					moveArr.get(1).incrementCount();
					break;
				case LEFT:
					moveArr.get(2).incrementCount();
					break;
				case RIGHT:
					moveArr.get(3).incrementCount();
					break;
				case NEUTRAL:
					moveArr.get(4).incrementCount();
					break;
			}
		}

		Collections.sort(moveArr,Comparator.comparing(MoveDir::getCount));

		MOVE majorityMove = moveArr.get(4).move;

		return majorityMove;
	}

	private void fetchData() {
		ArrayList<DataTuple> data = new ArrayList<DataTuple>(Arrays.asList(DataSaverLoader.LoadPacManData()));
		Random r = new Random(new Date().getTime()); //Seed
		int datacap = (int)(data.size() * 0.33);

		//Get 33% of data and mark it as test data
		for (int i = 0; i < datacap; i++) {
			int index = r.nextInt(data.size());
			testData.add(data.get(index));
			data.remove(index);
		}

		//Mark the rest of the data as training data
		for (int i = 0; i < data.size(); i++) {
			trainingData.add(data.get(i));
		}
	}

	private void initLists() {
		//Add all attributes and their discrete possible values to attributes

		ArrayList<String> boolStrings = new ArrayList<String>();
		boolStrings.add("true");
		boolStrings.add("false");

		attributes.put("isBlinkyEdible",boolStrings);
		attributes.put("isInkyEdible",boolStrings);
		attributes.put("isPinkyEdible",boolStrings);
		attributes.put("isSueEdible",boolStrings);

		ArrayList<String> distanceStrings = new ArrayList<String>();
		distanceStrings.add("NONE");
		distanceStrings.add("VERY_LOW");
		distanceStrings.add("LOW");
		distanceStrings.add("MEDIUM");
		distanceStrings.add("HIGH");
		distanceStrings.add("VERY_HIGH");

		attributes.put("blinkyDist", distanceStrings);
		attributes.put("inkyDist", distanceStrings);
		attributes.put("pinkyDist", distanceStrings);
		attributes.put("sueDist", distanceStrings);

		ArrayList<String> directionStrings = new ArrayList<String>();
		directionStrings.add("UP");
		directionStrings.add("DOWN");
		directionStrings.add("LEFT");
		directionStrings.add("RIGHT");
		directionStrings.add("NEUTRAL");

		attributes.put("blinkyDir", directionStrings);
		attributes.put("inkyDir", directionStrings);
		attributes.put("pinkyDir", directionStrings);
		attributes.put("sueDir", directionStrings);
	}



}