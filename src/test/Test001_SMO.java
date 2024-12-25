package test;

import mdmtsp.DataReader;
import mdmtsp.Individual;
import mdmtsp.MDMTSP;
import mdmtsp.SearchOption;
import smo.SpiderMonkeyOptimization;

public class Test001_SMO {

    public static void main(String[] args) {
        //parameters
        String filename = "src/dataset/burma14.tsp";//"src/dataset/kro124p.atsp";//
        double[][] distanceMatrix = new DataReader().read(filename);
        int[] depots = {0};
        int numberOfSalesmans = 1;
        MDMTSP mdmtsp = new MDMTSP(distanceMatrix, depots, numberOfSalesmans);

        int populationSize = 10;
        int MAX_ITERATION = 100;
        int ALLOWED_MAX_GROUP = 50;
        double perturbationRate = 0.5;
        int localLeaderLimit = 10;
        int globalLeaderLimit = 10;
        //Search option
        SearchOption swapOperation = SearchOption.PARTIAL;
        SearchOption slideOperation = SearchOption.NONE;
        SearchOption flipOperation = SearchOption.NONE;
        SearchOption breakpointOperation = SearchOption.NONE;
        SearchOption startDepotOperation = SearchOption.NONE;      
        
        SpiderMonkeyOptimization smo = new SpiderMonkeyOptimization(mdmtsp, populationSize, MAX_ITERATION, ALLOWED_MAX_GROUP, perturbationRate, localLeaderLimit, globalLeaderLimit);
        smo.setSearchOption(swapOperation, slideOperation, flipOperation, breakpointOperation, startDepotOperation);
        // Partial Search
        smo.process();

        Individual bestSolution = smo.getBestSolution();
        System.out.println(bestSolution);

    }
}
