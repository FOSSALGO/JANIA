package test;

import abc.ArtificialBeeColony;
import mdmtsp.DataReader;
import mdmtsp.Individual;
import mdmtsp.MDMTSP;
import mdmtsp.SearchOption;

public class Test002_ABC {

    public static void main(String[] args) {
        //parameters
        String filename = "src/dataset/burma14.tsp";//"src/dataset/kro124p.atsp";//
        double[][] distanceMatrix = new DataReader().read(filename);
        int[] depots = {0,7};
        int numberOfSalesmans = 2;
        MDMTSP mdmtsp = new MDMTSP(distanceMatrix, depots, numberOfSalesmans);

        int populationSize = 100;
        int MAX_GENERATION = 400;
        int upperLimitOfFoodSourceCounter = 2;
        //Search option
        SearchOption swapOperation = SearchOption.PARTIAL;
        SearchOption slideOperation = SearchOption.PARTIAL;
        SearchOption flipOperation = SearchOption.PARTIAL;
        SearchOption breakpointOperation = SearchOption.PARTIAL;
        SearchOption startDepotOperation = SearchOption.PARTIAL;   
        
        ArtificialBeeColony abc = new ArtificialBeeColony(mdmtsp, populationSize, MAX_GENERATION, upperLimitOfFoodSourceCounter);
        abc.setSearchOption(swapOperation, slideOperation, flipOperation, breakpointOperation, startDepotOperation);
        // Partial Search
        abc.process();

        Individual bestSolution = abc.getBestSolution();
        System.out.println(bestSolution);

    }
}
