package test;

import mdmtsp.DataReader;
import mdmtsp.Individual;
import mdmtsp.MDMTSP;
import mdmtsp.SearchOption;
import pga.ParthenoGeneticAlgorithm;

public class Test006_PGA {

    public static void main(String[] args) {
        //parameters
        String filename = "src/dataset/burma14.tsp";//"src/dataset/kro124p.atsp";//
        double[][] distanceMatrix = new DataReader().read(filename);
        int[] depots = {0};
        int numberOfSalesmans = 1;
        MDMTSP mdmtsp = new MDMTSP(distanceMatrix, depots, numberOfSalesmans);

        int populationSize = 100;
        int MAX_GENERATION = 100;
        double mutationRate = 0.1;
        
        //Search option
        SearchOption swapOperation = SearchOption.PARTIAL;
        SearchOption slideOperation = SearchOption.PARTIAL;
        SearchOption flipOperation = SearchOption.PARTIAL;
        SearchOption breakpointOperation = SearchOption.PARTIAL;
        SearchOption startDepotOperation = SearchOption.PARTIAL;   
        
        ParthenoGeneticAlgorithm pga = new ParthenoGeneticAlgorithm(mdmtsp, populationSize, MAX_GENERATION, mutationRate);
        pga.setSearchOption(swapOperation, slideOperation, flipOperation, breakpointOperation, startDepotOperation);
        pga.process();

        Individual bestSolution = pga.getBestSolution();
        System.out.println(bestSolution);

    }
}
