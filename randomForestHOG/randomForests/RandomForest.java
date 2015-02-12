package randomForestHOG.randomForests;

import randomForestHOG.hog.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import fr.ensmp.caor.levis.sample.DataClass;
import fr.ensmp.caor.levis.sample.DataBase;
import fr.ensmp.caor.levis.classifier.Classifier;
import fr.ensmp.caor.levis.sample.DataBase;
import fr.ensmp.caor.levis.sample.Sample;
import fr.ensmp.caor.levis.CheckParam;

public class RandomForest extends Classifier  {
	private static final long serialVersionUID = 1L;
	
	private RF randomForest;

	public RandomForest(){
	}

	public RandomForest(RF randomForest){
		this.randomForest = randomForest;
	}



	public void read(BufferedReader paramBufferedReader) throws Exception{
		randomForest = new RF();
		
		//Get the whole file
		ArrayList<String> treesText = new ArrayList<String>();
		String bufferLine;
		
		//while there is still line, and each line is a complete Tree (or Root)
		while ((bufferLine = paramBufferedReader.readLine()) != null) {
			treesText.add(bufferLine);
		}

		ArrayList<Tree> myRootsHere = new ArrayList<Tree>();

		for (String treeText : treesText){
			myRootsHere.add(parseTreeText(treeText));
		}

		randomForest.myRoots = myRootsHere.toArray(new Tree[myRootsHere.size()]);
	}
	
	public void write(BufferedWriter paramBufferedWriter, boolean paramBoolean) throws IOException{
		/*
		if (!paramBoolean) {
			this._model.write(paramBufferedWriter);
		}*/

		for (Tree tree : this.randomForest.myRoots){
			paramBufferedWriter.write(this.serializeTree(tree, 0));
		}
	}

	private String serializeTree(Tree tree, int treeLevel){
		if(tree == null)
			return "";
		
		StringBuffer treelvl = new StringBuffer("treeID= "+treeLevel);
		StringBuffer res = new StringBuffer("<" + treelvl);
		//StringBuffer tab = new StringBuffer(treeLevel + "\t");
		//.replace("\0", "\t");

		res.append(String.format(
			" %s %s %s >", 
			tree.criteriaValue, 
			tree.criteriaIndex, 
			tree.classID)
		);
		
		//Now it's something like: <treeID= 0 0 0 0 >
		if (!tree.isEnd){
			res.append(" "+serializeTree(tree.leftTree, treeLevel + 1));
			res.append(" "+serializeTree(tree.rightTree, treeLevel + 1)+" ");
		}
		res.append("<"+treelvl+" >");

		if(treeLevel == 0)
			res.append("\n");    //retour de ligne si et seulement si c'est un Tree complet
		return res.toString();
	}


	public Tree parseTreeText(String treeText){
		int treeLevel = 0;
		Tree thisTree = new Tree();
		//********** Identifier le Tree *****************
		String key = "((<treeID=) (.+?)) ";
		Pattern p1 = Pattern.compile(key);
		Matcher m1 = p1.matcher(treeText);
		
		if(m1.find())
			treeLevel = Integer.parseInt(m1.group(3));
		
		//*********** Remplir les donnees de Tree *****************
		String treeContentKey = "((<treeID=) (.+?) (.+?) (.+?) (.+?)) ";
		Pattern treePattern = Pattern.compile(treeContentKey);
        Matcher treeDataMatcher = treePattern.matcher(treeText);

        if(treeDataMatcher.find()){
        	thisTree.criteriaValue = Integer.parseInt(treeDataMatcher.group(4));
        	thisTree.criteriaIndex = Integer.parseInt(treeDataMatcher.group(5));
        	thisTree.classID = Integer.parseInt(treeDataMatcher.group(6));
        }
        //************ Les Tree-enfants? **********
		String childrenKey = "((>) (.+?) (<treeID= "+treeLevel+" >))";
		Pattern childrenPattern = Pattern.compile(childrenKey);
		Matcher childrenMatcher = childrenPattern.matcher(treeText);
		String childrenContent = null;

		if(childrenMatcher.find())
			childrenContent = childrenMatcher.group(3);
		
		if(childrenContent.length() <= 4)
			thisTree.isEnd = true;
		else{
			thisTree.isEnd = false;
			//****** pour les enfants!! *******
			int enfantLevel = treeLevel+1;
			String treeIDKey = "(<treeID= "+enfantLevel+" >)";
			
			String rightKey = treeIDKey + " (.+?) " + treeIDKey;
			Pattern pR = Pattern.compile(rightKey);
			Matcher mR = pR.matcher(childrenContent);
			String leftKey =  "(.+?) " + treeIDKey;
			Pattern pL = Pattern.compile(leftKey);
			Matcher mL = pL.matcher(childrenContent);
			
			if(mL.find()){
				//treeID = Integer.parseInt(m1.group(3));
				String left = mL.group(1) + " <treeID= "+enfantLevel+" >";
				thisTree.leftTree = parseTreeText(left);
			}
			if(mR.find()){
				//treeID = Integer.parseInt(m1.group(3));
				String right = mR.group(2) + " <treeID= "+enfantLevel+" >";
				thisTree.rightTree = parseTreeText(right);
			}
		}
		return thisTree;
	}

	@Override
	public Object getParameter(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getParameterDescription(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getParameters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object[] getPossibleValues(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setParameter(String arg0, Object arg1) throws Exception {
		// TODO Auto-generated method stub
	}

	@Override
	public int classify(Sample arg0) {
		if(arg0 instanceof HOG)
			return this.randomForest.vote((HOG)arg0);
		// TODO Auto-generated method stub
		return -1;
	}

}