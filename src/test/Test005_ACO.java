package test;

import aco.AntColonyOptimization;
import mdmtsp.DataReader;
import mdmtsp.Individual;
import mdmtsp.MDMTSP;

public class Test005_ACO {

    public static void main(String[] args) {
        //parameters
        String filename = "src/dataset/burma14.tsp";//"src/dataset/kro124p.atsp";//
        double[][] distanceMatrix = new DataReader().read(filename);
        int[] depots = {0,7};
        int numberOfSalesmans = 2;
        MDMTSP mdmtsp = new MDMTSP(distanceMatrix, depots, numberOfSalesmans);
        int numberOfAnts = 1000;
        int maxIterations = 1000;
        double alpha = 0.5;
        double beta = 0.5;
        double evaporation = 0.5;

        AntColonyOptimization aco = new AntColonyOptimization(mdmtsp, numberOfAnts, maxIterations, alpha, beta, evaporation);
        aco.process();

        Individual bestSolution = aco.getBestSolution();
        System.out.println(bestSolution);

    }
}
