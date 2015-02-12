package randomForestHOG.randomForests;

import randomForestHOG.hog.*;

import java.util.Hashtable;
import java.util.Random;
import java.util.Stack;

import fr.ensmp.caor.levis.sample.DataClass;
import fr.ensmp.caor.levis.sample.DataBase;

public class RF {
	public Tree[] myRoots;
    public static double baggingRatio = 0.6;
    private static int undecisiveIndex = -1;
	private static int treeNumber = 200;
	private static int maxTreeLevel = 10;
	private static int cellSize = 6;
	private static HOGParam hogParam;
	
	Random rand;
	
	//Initialisation de tous les Tree
	public RF(){
		rand = new Random();
		myRoots = new Tree[treeNumber];
		for(int i = 0 ; i < treeNumber ; i++){
			myRoots[i] = new Tree(0, undecisiveIndex);
		}
	}

	//Bagging de base de donn¨¦es pour l'apprentissage des Trees 
	public void baggingAndLearn(DataBase mybase) throws Exception{
		HOG[] allSamples = getAllSamplesInBase(mybase);
		for(Tree i : myRoots){
			i.learnStuff(getBag(allSamples));
		}
		return;
	}
	
	//Faire voter tous les arbres pour l'identit¨¦ de HOG ¨¤ tester
	//Policy: the result agreed by most trees
	public int vote(HOG test){
		
		Hashtable<Integer, Integer> IDHashTable = new Hashtable<Integer, Integer>();
		int mostVotedClass = undecisiveIndex;
    	int mostVotes = 0; //we are gonna update in "real time" the most voted class
		
    	for(int i = 1 ; i < myRoots.length ; i++){
    		int newVote = myRoots[i].vote(test);
    		if(IDHashTable.isEmpty()== true || IDHashTable.get(newVote) == null)
    			IDHashTable.put(newVote, 1);
    		else IDHashTable.put(newVote,IDHashTable.get(newVote)+1);
    		
    		//check if it's the most voted
    		if(IDHashTable.get(newVote) > mostVotes){
    			mostVotes = IDHashTable.get(newVote);
    			mostVotedClass = newVote;
    		}
    	}
    	return mostVotedClass;
	}
	
	//La fonction qui prend tous les Sample dans une base de donn¨¦e pour construire un array de HOG
	public HOG[] getAllSamplesInBase(DataBase mybase) throws Exception{
		Stack<HOG> list = new Stack<HOG>();
		for(int i = 0; i < mybase.getNbClasses() ; i++)
			for(int j = 0; j < mybase.getDataClass(i).size() ; j++){
				HOG hg = new HOG(mybase.getDataClass(i).get(j), hogParam);
				list.push(hg);
			}
		return list.toArray(new HOG[list.size()]);
	}
	
	//La fonction qui s¨¦lectionne une fraction de base (bagging)
	public HOG[] getBag(HOG[] base){
		HOG[] bag = new HOG[(int)(base.length*baggingRatio)];
		for(int i = 0 ; i < bag.length ; i++)
			bag[i] = base[i];
		
		for(int i = bag.length ; i < base.length ; i++){
			int j = rand.nextInt(i+1);
			if(j<=bag.length-1)
				bag[j] = base[i];
		}
		
		return bag;
	}
	
	public int getTreeNumber(){
		return treeNumber;
	}
	
	public boolean setTreeNumber(int n){
		if(n <= 0)
			return false;
		treeNumber = n;
		return true;
	}
	
	public int getMaxTreeLevel(){
		return Tree.maxTreeLevel;
	}
	
	public boolean setMaxTreeLevel(int n){
		if(n <= 0)
			return false;
		Tree.maxTreeLevel = n;
		return true;
	}
	public int getCellSize(){
		return cellSize;
	}
	public void setCellSize(int c){
		cellSize = c;
	}
	public static void setParam(HOGParam hog){
		hogParam = hog;
		cellSize = hog.getParam(1);
		treeNumber = hog.getParam(2);
		maxTreeLevel = hog.getParam(3);
	}
	
}
