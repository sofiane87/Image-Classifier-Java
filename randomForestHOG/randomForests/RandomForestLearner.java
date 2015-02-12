package randomForestHOG.randomForests;

import randomForestHOG.hog.*;
import fr.ensmp.caor.levis.classifier.Classifier;
import fr.ensmp.caor.levis.learner.Learner;
import fr.ensmp.caor.levis.sample.DataBase;
import fr.ensmp.caor.levis.sample.Sample;
import fr.ensmp.caor.levis.CheckParam;



public class RandomForestLearner extends Learner {
	private static final long serialVersionUID = 1L;
	private Classifier	_model = new RandomForest();
	private boolean _learnloop = true;
	private RF forest = new RF();
	private int maxIteration = 200;
	private static int maxTreeLevel ;
	private int treeNumber ; 
	private int cellSize;
	private boolean _useOutputForNegClass = false;
	private HOGParam hogParam = new HOGParam();
	
	public Classifier learn(DataBase learnData) throws Exception {
		hogParam.setParam(cellSize, treeNumber, maxTreeLevel);
		RF.setParam(hogParam);
		forest = new RF();
		learnData.computeAndAddHOGs(hogParam);
		forest.baggingAndLearn(learnData);
		RandomForest model = new RandomForest(forest);
		return model;
	}

	//--------------------------------------------------
	public static final String MAX_LEVEL = "Maximum Tree Level";
	public static final String TREE_NUMBER = "Number of Trees";
	public static final String CELL_SIZE = "Cell Size of HOG";
	
	protected String[] getLearnerParameters() {
		return new String[]{MAX_LEVEL, TREE_NUMBER, CELL_SIZE};
	}
	
	protected Object getLearnerParameter(String key) {
		if (key.compareTo(MAX_LEVEL) == 0) {
			return maxTreeLevel;
		} else if (key.compareTo(TREE_NUMBER) == 0) {
			return treeNumber;
		} else if(key.compareTo(Learner.CLASSIFIER) == 0) {
				return _model;
		}else if(key.compareTo(CELL_SIZE) == 0) {
			return cellSize; 
		}else {
			return null;
		}
	}
	
	protected String getLearnerParameterDescription(String key) {
		if (key.compareTo(MAX_LEVEL) == 0) {
			return "The maximal level or depth that a tree can reach.";
		} else if (key.compareTo(TREE_NUMBER) == 0) {
			return "The Number of tree to be used in the random forest";
		}else if(key.compareTo(CELL_SIZE) == 0) {
			return "Cell size used in HOG calculation, between 4 and 7"; 
		}
		return null;
	}

	protected void setLearnerParameter(String key, Object value) throws Exception {
		if (key.compareTo(MAX_LEVEL) == 0) {
			maxTreeLevel = CheckParam.getIntegerValue(MAX_LEVEL, value);
		} else if (key.compareTo(TREE_NUMBER) == 0) {
			treeNumber = CheckParam.getIntegerValue(TREE_NUMBER, value);
		} else if (key.compareTo(Learner.CLASSIFIER) == 0) {
			_model = CheckParam.getClassifier(Learner.CLASSIFIER, value, getLearnerPossibleValues(Learner.CLASSIFIER));
		}else if(key.compareTo(CELL_SIZE) == 0) {
			int x = CheckParam.getIntegerValue(TREE_NUMBER, value); 
			if(x>3 && x<8)
				cellSize = x;
		}
	}

	protected Object[] getLearnerPossibleValues(String key) {
		if (key.compareTo(Learner.CLASSIFIER) == 0) {
			return new Classifier[] {_model};
		} else 
			return null;
	}
	
}