# Image-Classifier-Java

This repository presents a code that aimed to add a new Image classifier, using Random Forests to **Levis** an *Image classification software* coded by *Mines Paristech*'s Robotics lab : *CAOR*

The package is composed of two sub_modules : 
* hog : Histogram of Oriented Gradients
* randomForests : Random Forest Calssifier 

## hog :

This sub-module handles the images **preprocessing**, it takes images as inputs and returns the associated **Histogram of Oriented Gradients**

this folder contains the follower *java files* : 

*  **HOG** : Realize the preprocessing
*  **HOGPARAM** : stores and returns the parameters of the trees
*  **Tree** : Builds and store a tree


## Random Forests : 

As the name suggests it, this sub-module builds and trains the Random Forest. 

this folder contains the follower *java files* : 

* **RandomForest** : Builds and Store the random Forest
* **RandomForestLearner** : class that trains and update the Random Forest 
* **RF** : root class for *RandomForest* handles simple methods to leave only machine learning related methods to the *RandomForest* class.