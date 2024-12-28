package example;

import mdmtsp.DataReader;

import java.util.Arrays;

public class ExampleReadDataset {
    public static void main(String[] args) {
        String filename = "src/dataset/burma14.tsp";
        double[][] distanceMatrix = new DataReader().read(filename);

        // print dataset
        System.out.println("DATASET");
        System.out.println(Arrays.deepToString(distanceMatrix));

        // print dataset using for loop
        System.out.println("DATASET");
        for (int i=0;i<distanceMatrix.length;i++){
            System.out.print("[");
            for (int j=0;j< distanceMatrix[i].length;j++){
                if(j>0){
                    System.out.print(", ");
                }
                System.out.print(distanceMatrix[i][j]);
            }
            System.out.println("]");
        }
    }
}
