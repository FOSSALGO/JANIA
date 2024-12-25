package test;

import mdmtsp.DataReader;
import mdmtsp.Individual;
import mdmtsp.MDMTSP;
import mdmtsp.SearchOption;
import pso.ParticleSwarmOptimization;

public class Test004_PSO {

    public static void main(String[] args) {
        //parameters
        String filename = "src/dataset/burma14.tsp";//"src/dataset/kro124p.atsp";//
        double[][] distanceMatrix = new DataReader().read(filename);
        int[] depots = {0,7};
        int numberOfSalesmans = 2;
        MDMTSP mdmtsp = new MDMTSP(distanceMatrix, depots, numberOfSalesmans);
        int populationSize = 100;
        int MAX_ITERATION = 100;
        double perturbationRate = 0.2;

        //Search option
        SearchOption swapOperation = SearchOption.PARTIAL;
        SearchOption slideOperation = SearchOption.PARTIAL;
        SearchOption flipOperation = SearchOption.PARTIAL;
        SearchOption breakpointOperation = SearchOption.PARTIAL;
        SearchOption startDepotOperation = SearchOption.PARTIAL;

        ParticleSwarmOptimization pso = new ParticleSwarmOptimization(mdmtsp, populationSize, MAX_ITERATION, perturbationRate);
        pso.setSearchOption(swapOperation, slideOperation, flipOperation, breakpointOperation, startDepotOperation);

        pso.process();

        Individual bestSolution = pso.getBestSolution();
        System.out.println(bestSolution);

    }
}
