package randomForestHOG.hog;


import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import fr.ensmp.caor.levis.sample.ImageUtils;
import fr.ensmp.caor.levis.sample.Sample;

public class HOG extends Sample{

	private static final long serialVersionUID = 1L;
	private static int cellSize = 6;     //Quote: best cell size: 6*6; best block size: 3*3
	
	private int numberCellX;      //number of cells
	private int numberCellY;
	
	private int[] HOGData;
	
	
	// private int[][][] BlockDescriptor ***** The final HOG vector :D
	// private int[][] _imageData **** imageData gives i,j where i is row number and j is column number
	

	public HOG(HOG sample) throws Exception {
		super(sample);
		this.HOGData = sample.getHOGData();
	}
	
	//constructeur qui prend un Sample et remplace sa "_imageData" avec le vecteur HOG calcul¨¦
	//pr¨¦condition: Sample s contient bien le _imageData et les param¨¨tres n¨¦cessaires
	//postcondition: un HOG est d¨¦fini
	public HOG(Sample s, HOGParam p) throws Exception{
		super(s);
		cellSize = p.getParam(1);
		this._imageData = s.getData();
		//if(_imageData.length != width*height)
		//	return;
		this.classId = s.classId;
		
		calculateData(fromImagedataToRealStuff(this._imageData));
	}
	
	/*
	public HOG(File paramFile, HOG paramSample, int paramInt) throws IOException {
		super(paramSample);
		this.classId = paramInt;

		this._src = paramFile;
		this._imageOrigine = javaBugGrayImage(ImageIO.read(paramFile));
		this.missClassified = false;
		int[][] rgbRawData = new int[3][this.width*this.height];
		
		int[] arrayOfInt;
		try
		{
		     arrayOfInt = ImageUtils.getData(this._imageOrigine, "Red", this.isColor);
		     rgbRawData[0] = ImageUtils.resize(arrayOfInt, this._imageOrigine.getWidth(), this._imageOrigine.getHeight(), this.width, this.height);
		     arrayOfInt = ImageUtils.getData(this._imageOrigine, "Green", this.isColor);
		     rgbRawData[1] = ImageUtils.resize(arrayOfInt, this._imageOrigine.getWidth(), this._imageOrigine.getHeight(), this.width, this.height);
		     arrayOfInt = ImageUtils.getData(this._imageOrigine, "Blue", this.isColor);
		     rgbRawData[2] = ImageUtils.resize(arrayOfInt, this._imageOrigine.getWidth(), this._imageOrigine.getHeight(), this.width, this.height);
		     
		     boolean bool = (this.channel.compareToIgnoreCase("all") == 0) && (this.isColor);
		     this._imageResize = ImageUtils.toBufferedImage(this._imageData, this.width, this.height, bool);
		}
		catch (Exception localException){
		       localException.printStackTrace();
		}
		calculateData(rgbRawData);

	}*/
	
	//fonction pour calculer le HOG ¨¤ partir de la matrice fournie et remplacer le _imageData avec le vecteur HOG
	//pr¨¦condition: la matrice de pixels bien d¨¦finie
	//postcondition: le _imageData vecteur est mis ¨¤ jour comme HOG vecteur
	private void calculateData(int[][] imagePixels){
		//code the HOG here
		int[][][] BlockDescriptor = CalculateGradient(imagePixels);
		
		//now convert HOG vector into one dimensional array
		HOGData = new int [(numberCellX-2)*(numberCellY-2)*9];
		for(int i=1; i<numberCellX-1 ; i++)
    		for(int j=1; j<numberCellY-1; j++)
    			for(int k=0; k<9 ; k++){
		             this.HOGData[i*(numberCellY-2)*9 + j*9 +k] = BlockDescriptor[i][j][k];
		             System.out.println(this.HOGData[i*(numberCellY-2)*9 + j*9 +k]);
    			}
		
		//normalize between 0 to 200
		int max = maxOf(HOGData);
		for(int i : HOGData)
			i = (int)(i/(1.0)/max*200);
	}
	
	//La fonction qui restaure le uni-dimensionnel _imageData ¨¤ la matrice conventionnelle d'image
	//pr¨¦condition: _imageData contient bien tous les pixels
	//postcondition: renvoi de la matrice restaur¨¦e
	private int[][] fromImagedataToRealStuff(int[] imageData) throws Exception{
		//System.out.println("imageData" + imageData[0] + " " + imageData[3]);
		int[][] output = new int[this.height][this.width];
		if(this.height*this.width != _imageData.length)
			throw new Exception("Actual size = "+this.height*this.width+"doesn't match the dimensions = "+_imageData.length);
		
		//****
		int i = 0;
		for(int j = 0 ; j < this.height ; j++){
			for(int m = 0 ; m < this.width ; m++){
				if (!this.isColor)
				{
				   output[j][m] = (imageData[(i++)] & 0xFF);    //get the last 8 bits
				}
				else if (this.channel.compareToIgnoreCase("Gray") == 0)
				{
					output[j][m] = (imageData[(i++)] >> 0 & 0xFF);
					}
					else if (this.channel.compareToIgnoreCase("All") == 0)
					{
						int n = imageData[i] >> 16 & 0xFF;
						int i1 = imageData[i] >> 8 & 0xFF;
	    				int i2 = imageData[i] >> 0 & 0xFF;
						output[j][m] = (int)(n+i1+i2)/3;
						i++;
					}
					else if (this.channel.compareToIgnoreCase("Red") == 0 || this.channel.compareToIgnoreCase("Green") == 0 || this.channel.compareToIgnoreCase("Blue") == 0)
					{
						output[j][m] = (imageData[(i++)] >> 0 & 0xFF);
					}
			        else
			        {
			        	throw new Exception("channel Error");
			        }
    			//System.out.println(output[j][m]);
			}
		}
		
		return output;
	}

    //La fonction qui calcule le gradient de couleur ¨¤ chaque pixel
	//pr¨¦condition: matrice bien d¨¦finie
	//postcondition: renvoi d'une 3d matrice qui donne le gradient et l'angle ¨¤ chaque pixel
	public int[][][] CalculateGradient(int imagePixels[][]){
		int[][][] Gradient = new int[imagePixels.length][imagePixels[0].length][2];
		
		for(int i = 1; i<imagePixels.length-1; i++)                  //for each row
			for(int j = 1; j<imagePixels[0].length-1; j++){          //for each column
	            int diffVer = imagePixels[i][j+1] - imagePixels[i][j-1];
	            
	            int diffHor = imagePixels[i+1][j] - imagePixels[i-1][j];
	            
	            int gradient = (int)Math.sqrt(diffVer*diffVer + diffHor*diffHor);
	            
	            if((diffVer<0&&diffHor<0)||(diffHor>0&&diffVer<0))   //can be therefore negative
	            	gradient*=-1;
	            
	            int angle = (int)(Math.atan(diffVer*1.0/diffHor)/Math.PI*180)+90;    //angle from 0 to 180
	            //int alpha = new Color(original.getRGB(i, j)).getAlpha();
	            Gradient[i][j][0]=gradient;
	            Gradient[i][j][1]=angle;
			}
		return orientationBinning(Gradient);
	}
	
	//La fonction qui calcule, ¨¤ partir des gradients, la direction g¨¦n¨¦rale dans chaque bin (une collection de pixel au voisin)
	//pr¨¦condition: la matrice de gradient est bien calcul¨¦e
	//postcondition: une 3d matrice de bins/cellules
    public int[][][] orientationBinning(int[][][] Gradient){
    	numberCellX = this.width/cellSize;
    	numberCellY = this.height/cellSize;                         //the residual is regrettably cast away
    	int[][][] BinValues = new int[numberCellX][numberCellY][9]; //x, y, 9 values representing angular histogram
    	//For each cell
    	for(int i=0; i<numberCellX ; i++)
    		for(int j=0; j<numberCellY ; j++){
    			//For each pixel in this cell
    			for(int a=0; a<cellSize; a++)
    				for(int b=0; b<cellSize; b++)
    				{
    					//Find Corresponding Gradient Values
    					int mag = Gradient[i*cellSize+a][j*cellSize+b][0];
    				    int angle = Gradient[i*cellSize+a][j*cellSize+b][1];
    				    if(angle == 180)
    				    	angle = 179;
    				    //Casting the weighted vote
    					BinValues[i][j][angle/20] += mag;
    				}
    		}
    	return blockNormalisation(BinValues);
    }
	
    //La fonction qui calcule encore la direction / magnitude de gradient dans un voisinage de cellules
    //pr¨¦condition: cellules bien calcul¨¦s
    //postcondition: une 3d matrice de la direction approximative (parmi 9 choix) et valeur de gradient de chaque bloc
    public int[][][] blockNormalisation(int[][][] BinValues){
    	//Notice the 0 and numberCellX/Y-1 **i.e boundaries** in the array have null values (for the loop below)
    	int[][][] BlockDescriptor = new int[numberCellX][numberCellY][9];
    	//We use 3*3 R-HOG blocks
    	//For each block ij
    	for(int i=1; i<numberCellX-1 ; i++)
    		for(int j=1; j<numberCellY-1; j++){
    			//Take all values in the 3*3 to be added to the central cell i.e. block ij
    		    for(int a=-1;a<2;a++)
    		    	for(int b=-1;b<2;b++)
    		    		//and add up each angle range (0-20 for example)
    		    		for(int angle=0; angle<9 ; angle++)
    		    		    BlockDescriptor[i][j][angle] += BinValues[i+a][j+b][angle];
    		    
    		    //Get the sum of all 9 vectors
    		    int sum = 0;
    		    for(int a=0; a<9; a++)
    		    	sum += BlockDescriptor[i][j][a]*BlockDescriptor[i][j][a];
    		    sum = (int)Math.sqrt(sum);   //modulo the vector BlockDescriptor[i][j], 9 element
    		    
    		    for(int a=0; a<9; a++){
    		    	if(sum==0)
    		    		BlockDescriptor[i][j][a] = 0;
    		    	else
    		    		BlockDescriptor[i][j][a] = BlockDescriptor[i][j][a]*255/sum;  //255 is for the transparency later in paint()
    	            //if(BlockDescriptor[i][j][a]>255)
    	            	//BlockDescriptor[i][j][a] = 255;
    		    }
    		    System.out.println(BlockDescriptor[i][j][1]);
    		}
    	return BlockDescriptor;
    }
    
	/*
	private BufferedImage javaBugGrayImage(BufferedImage paramBufferedImage)
	{
		if (paramBufferedImage.getType() == 10){
				byte[] arrayOfByte = new byte[256];
				for (int i = 0; i < 256; i++) {
					arrayOfByte[i] = ((byte)i);
				}
				IndexColorModel localIndexColorModel = new IndexColorModel(8, arrayOfByte.length, arrayOfByte, arrayOfByte, arrayOfByte);
				BufferedImage localBufferedImage = new BufferedImage(paramBufferedImage.getWidth(), paramBufferedImage.getHeight(), 13, localIndexColorModel);
				((Graphics2D)localBufferedImage.getGraphics()).drawImage(paramBufferedImage, AffineTransform.getRotateInstance(0.0D), null);
				paramBufferedImage.flush();
				paramBufferedImage = localBufferedImage;
			}
		return paramBufferedImage;
	}
    
    private static int colorToRGB(int alpha, int red, int green, int blue) {
   	 
        int newPixel = 0;
        newPixel += alpha;
        newPixel = newPixel << 8;
        newPixel += red; newPixel = newPixel << 8;
        newPixel += green; newPixel = newPixel << 8;
        newPixel += blue;
 
        return newPixel;
 
    }
    */
    
    public int[] getHOGData(){
    	return HOGData;
    }
    
    private int maxOf(int[] array){
    	int temp = 0;
    	for(int i : array)
    		if(i>temp)
    			temp = i;
    	return temp;
    }
}
