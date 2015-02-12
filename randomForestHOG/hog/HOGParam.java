package randomForestHOG.hog;

public class HOGParam {
    public static int cellSize = 6;
    public static int treeNumber = 100;
    public static int treeLevel = 10;
	
	public HOGParam(){
	}
	
	public void setParam(int a, int b, int c){
		cellSize = a;
		treeNumber = b;
		treeLevel = c;
	}
	
	public int getParam(int index){
		switch(index){
		case 1: return cellSize;
		case 2: return treeNumber;
		case 3: return treeLevel;
		default: return -1;
		}
	}
	/*
	public void main(){
		HOGParam n = new HOGParam();
		n.setParam();
	}
	*/
}
