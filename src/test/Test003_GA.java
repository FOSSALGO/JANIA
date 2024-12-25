package test;

import ga.GeneticAlgorithm;
import mdmtsp.DataReader;
import mdmtsp.Individual;
import mdmtsp.MDMTSP;
import mdmtsp.SearchOption;
import mdmtsp.SelectionOption;

public class Test003_GA {

    public static void main(String[] args) {
        //parameters
        String filename = "src/dataset/burma14.tsp";//"src/dataset/kro124p.atsp";//
        double[][] distanceMatrix = new DataReader().read(filename);
        int[] depots = {0};
        int numberOfSalesmans = 1;
        MDMTSP mdmtsp = new MDMTSP(distanceMatrix, depots, numberOfSalesmans);

        int populationSize = 10;
        int MAX_GENERATION = 100;
        SelectionOption selection = SelectionOption.TOURNAMENT;
        int numberOfIndividualsSelected = 500;
        double mutationRate = 0.1;
        
        //Search option
        SearchOption swapOperation = SearchOption.PARTIAL;
        SearchOption slideOperation = SearchOption.PARTIAL;
        SearchOption flipOperation = SearchOption.PARTIAL;
        SearchOption breakpointOperation = SearchOption.PARTIAL;
        SearchOption startDepotOperation = SearchOption.PARTIAL;   
        
        GeneticAlgorithm ga = new GeneticAlgorithm(mdmtsp, populationSize, MAX_GENERATION, selection, numberOfIndividualsSelected, mutationRate);
        ga.setSearchOption(swapOperation, slideOperation, flipOperation, breakpointOperation, startDepotOperation);
        ga.process();

        Individual bestSolution = ga.getBestSolution();
        System.out.println(bestSolution);

    }
}
