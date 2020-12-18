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

	private HashMap<String,List<String>> attributes = new HashMap<String,List<String>>();

	private Node rootNode;


	public MyPacMan () {

		fetchData();
		initLists();
		rootNode = buildTree();
	}

	public MOVE getMove(Game game, long timeDue) 
	{
		//Place your game logic here to play the game as Ms Pac-Man
		
		return myMove;
	}

	private Node buildTree() {

		// 1: Create root node
		Node node = new Node();

		// 2: If every tuple in D has the same class C, return N as a leaf node labeled as C.
		MOVE move = trainingData.get(0).DirectionChosen;
		boolean classDiff = false;
		for (int i = 1; i < trainingData.size(); i++) {
			if(trainingData.get(i).DirectionChosen != move) {
				classDiff = true;
				i = trainingData.size();
			}
		}
		if(!classDiff) {
			node.setLabel(move.toString());
			return node;
		}

		// 3: If the attribute list is empty, return N as a leaf node labeled with the majority class in D
		if(attributes.size() == 0) {
			node.setLabel(classMajority(trainingData).toString());
			return node;
		}

		// 4.1: Call attribute selection method on D and the attribute list, in order to choose the current attribute A
		String A = S(trainingData,attributes);


		return node;
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

	/**
	 * S is the ID3 algorithm used in building the tree.
	 * @param data
	 * @param attributes
	 * @return
	 */
	private String S(ArrayList<DataTuple> data, HashMap<String,List<String>> attributes) {
		//Variable for holding the return string
		String retVal = "";

		//Variable for holding the best information gain value. Set to max at start for comparison later.
		double infoGainOnAofD = Double.MAX_VALUE;
		ArrayList<String> attributeList = new ArrayList<>(attributes.keySet());
		for (String attribute : attributeList) {
			
		}



		return "";
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