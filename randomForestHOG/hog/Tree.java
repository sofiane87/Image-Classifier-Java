package randomForestHOG.hog;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Random;
import java.util.Stack;

import fr.ensmp.caor.levis.sample.DataClass;
import fr.ensmp.caor.levis.sample.DataBase;

public class Tree {
	public static int maxTreeLevel = 10;
	private static double giniThreshold = 0.2; //le seuil pour d¨¦terminer un bon split
	public static int undecisiveIndex = -1;  //quand la feuille n'a pas assez d'info, elle ne d¨¦cide pas (donc elle ne vote pas)
	public int treeLevel;
	
	public Tree leftTree;
	public Tree rightTree;
	public int criteriaValue;
	public int criteriaIndex;
	public boolean isEnd; //while classifying new HOG, this serves to signal that the tree has the answer
	public int classID;   //utile ssi isEnd==true, c'est indice de d¨¦cision prise (et cet arbre sera une feuille, c.f. undecisiveIndex)
	
	Random rand;
	
	public Tree(){
		//nothing is done here
	}
	
	//constructeur de Tree qui initialise tous les sousTrees (jusqu'aux feuilles)
    public Tree(int treeLvl, int index){
    	rand = new Random(); //for feature bagging
    	isEnd = false;
    	undecisiveIndex = index;
    	if(treeLvl < maxTreeLevel){
    		leftTree = new Tree(treeLvl+1, index);
    		rightTree = new Tree(treeLvl+1, index);
    		this.treeLevel = treeLvl;
    	}
    	else{
    		isEnd = true;
    		leftTree = null;
    		rightTree = null;
    	}
    }
	
    //Fonction pour l'apprentissage qui prend la base de donn¨¦es et modifie les param¨¨tres importants de tous les Trees
    //pr¨¦condtion:
    //postcondition: les crit¨¨res et les postions de crit¨¨re sont tous d¨¦finis pour chaque arbre; la d¨¦cision si elle est une feuille, sinon le Tree gauche et le Tree droite
    public void learnStuff(HOG[] base){
    	//End leaf condition
    	if(treeLevel == maxTreeLevel){
    		isEnd = true;
    		classID = undecisiveIndex;
    		leftTree = null;
    		rightTree = null;
    	}
    	else if(subsetGini(base) <= giniThreshold){
    		isEnd = true;
    		classID = getModeClass(base);
    		leftTree = null;
    		rightTree = null;
    	}
    	else{
        //else generate more branches
    	//criteriaIndex and criteriaValue will be obtained in gini(base)
        HOG[][] splitBases = gini(base);
        //
        if(leftTree!=null && splitBases != null && splitBases[0]!=null && splitBases[0].length!=0)
        	leftTree.learnStuff(splitBases[0]);
        if(rightTree!=null && splitBases != null && splitBases[1]!=null && splitBases[1].length!=0)       	
        	rightTree.learnStuff(splitBases[1]);
    	}
    }
    
    //La fonction qui prend un HOG et faire apprendre ce Tree pour une d¨¦cision. -1 si ce Tree n'a pas de d¨¦cision i.e.indecisiveIndex
    public int vote(HOG sample){
    	//if we have an answer
    	if(isEnd == true){
    		return classID;
    	}
    	//if there is no more subtrees left, give indecisive
    	if(treeLevel == maxTreeLevel)
    		return classID;
    	
    	if(sample.getHOGData()[criteriaIndex] > criteriaValue)
    		return rightTree.vote(sample);
    	else return leftTree.vote(sample);
    }
    
    //Recherche d'un split de la base qui minimise le gini ainsi que le gini qui r¨¦sulte
    //pr¨¦conditon: la base n'est pas vide, sinon gini = 0
    public HOG[][] gini(HOG[] base){
    	if(base.length <= 1)
    		return null;

    	double gini = 0;
    	double leftGini = 0;
    	double rightGini = 0;

    	HOG[][] res = new HOG[2][];
    	HOG[][] temp = new HOG[2][];
    	int HOGVectorLength = base[0].getHOGData().length;
    	
    	double bestGini = 1;
    	int bestCriteriaValue = 0; //store the criteria maximising gini to split database in the end
    	int bestCriteriaIndex = 0;
    	
    	//For each possible criteria position ******BAGGED!!!
    	for(int i : bagArrayIndices(HOGVectorLength)){
    		//***************************
    		//For each possible value, here we take the gradient to be between 0 and 200, so threshold from 20 to 180
    		for (int j = 1 ; j < 10 ; j++){
    			temp = splitListOfHOG(base, i, j*20);
    			leftGini = subsetGini(temp[0]);
    			rightGini = subsetGini(temp[1]);
    			gini = temp[0].length*leftGini/base.length + temp[1].length*rightGini/base.length;
    			
    			if(gini < bestGini){
    				bestGini = gini;
    				bestCriteriaValue = j*20;
    				bestCriteriaIndex = i;
    				res = temp;
    				//System.out.println(bestGini);
    			}
    		}
    		//***************************
    	}
    	if(bestGini!=1){
        	splitListOfHOG(base, bestCriteriaIndex, bestCriteriaValue);
        	criteriaValue = bestCriteriaValue;
        	criteriaIndex = bestCriteriaIndex;
        	//System.out.println("Best criteria: "+criteriaValue+" at: " + criteriaIndex + " gini= " + bestGini + " treeLevl= " +treeLevel);
    	}

    	return res;
    }
    
    //La fonction qui divise la base en deux selon le crit¨¨re pour construire les sousTrees
    private HOG[][] splitListOfHOG(HOG[] base, int variableIndex, int threshold)throws RuntimeException{
    	if(variableIndex<0)
    		return null;
    	
    	if(base.length<=1)
    		return null;
    	Stack<HOG> toLeft = new Stack<HOG>();
    	Stack<HOG> toRight = new Stack<HOG>();    	
    	
    	//For each element of the base
    	for(int i = 0 ; i < base.length ; i++){
    		//We check the criteria
			//System.out.println("tree = " +treeLevel+" HOG= "+base[i].getHOGData()[variableIndex]);
    		if(base[i].getHOGData()[variableIndex] > threshold)
    			toRight.add(base[i]);
    		else toLeft.add(base[i]);
    	}
    	
    	HOG[][] res = new HOG[2][];
    	res[0] = toLeft.toArray(new HOG[toLeft.size()]);
    	res[1] = toRight.toArray(new HOG[toRight.size()]);
    	return res;
    }
    
    //Calcule d'un coefficient de gini d'une seule base de donn¨¦e
    private double subsetGini(HOG[] images){    	
    	if(images==null || images.length <= 1)
    		return 0;
    	
    	double gini = 1;
    	int baseSize = images.length;
    	
    	//Le hashtable contient le imageClassID(key) et le nombre d'image avec ce ID (value)
    	Hashtable<Integer, Integer> IDHashTable = new Hashtable<Integer, Integer>();
    	IDHashTable.put(images[0].classId, 1);
    	for(int i = 1 ; i < baseSize ; i++){
    		if(IDHashTable.isEmpty() || IDHashTable.get(images[i].classId) == null){
    			IDHashTable.put(images[i].classId, 1);
    		}
    		else IDHashTable.put(images[i].classId,IDHashTable.get(images[i].classId)+1);
    	}
    	
    	//now calculate gini -- Gini = 1 - sum of (Fi)^2
    	for(int i = 0; i<baseSize ; i++){
    		gini -= (IDHashTable.get(images[i].classId)*1.0/baseSize)*(IDHashTable.get(images[i].classId)*1.0/baseSize);
    		IDHashTable.put(images[i].classId , 0); //so that we do not calculate again for this class
    	}

    	return gini;
    }
    
    //Parmi un array de HOG, trouver la classe qui est le plus nombreuse dans cet array. 
    //Cet fonction sert ¨¤ trouver le vote le plus populaire
    private int getModeClass(HOG[] samples){
    	int result = -1;
    	int numberOfLegion = 0;
    	
    	Hashtable<Integer, Integer> IDHashTable = new Hashtable<Integer, Integer>();
    	for(int i = 1 ; i < samples.length ; i++){
    		if(IDHashTable.get(samples[i].classId) == null)
    			IDHashTable.put(samples[i].classId, 1);
    		else IDHashTable.put(samples[i].classId,IDHashTable.get(samples[i].classId)+1);
    	}
    	
    	for(int i = 1 ; i < samples.length ; i++){
    		if(IDHashTable.get(samples[i].classId)>numberOfLegion){
    			result = samples[i].classId;
    			numberOfLegion = IDHashTable.get(samples[i].classId);
    		}
    	}
    	
    	return result;
    }
    
    //bagging des crit¨¨res: retourne un array de l'indice choisi al¨¦atoirement 
    //		de 0 ¨¤ arraySize. le r¨¦sultat sera les indices de HOG vecteur ¨¤ 
    //		consid¨¦rer pour le calcul du gini
    private int[] bagArrayIndices(int arraySize){
    	int bagSize = (int)Math.sqrt(arraySize);
    	int[] s = new int[arraySize];
    	int[] output = new int[bagSize];
    	
    	//Reservoir algorithm - fill in the first k cells
    	for(int i = 0 ; i < s.length ; i++){
    		s[i] = i;
    		if(i < bagSize)
    			output[i] = i;
    	}
    	
    	for(int i = bagSize; i < s.length ; i++){
    		int j = rand.nextInt(i+1);
    		if(j <= bagSize-1)
    			output[j] = s[i];
    	}
    	
    	return output;
    }
}
