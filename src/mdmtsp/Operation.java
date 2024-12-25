package mdmtsp;

import java.util.ArrayList;

public class Operation {

    private java.util.Random random = new java.util.Random();

    public int randomBetween(int min, int max) {
        if (min > max) {
            int temp = min;
            min = max;
            max = temp;
        }
        return random.nextInt(1 + max - min) + min;
    }

    public double randomUniform() {
        return random.nextDouble();
    }

    public int[] arraycopy(int[] data) {
        int[] result = null;
        if (data != null) {
            result = new int[data.length];
            for (int i = 0; i < result.length; i++) {
                result[i] = data[i];
            }
        }
        return result;
    }

    public int[] arraycopy(int[] data, int from, int to) {
        int[] result = null;
        if (data != null
                && from >= 0
                && to >= 0
                && from < data.length
                && to < data.length) {
            int length = Math.abs(to - from) + 1;
            result = new int[length];
            for (int i = 0; i < result.length; i++) {
                result[i] = data[i + from];
            }
        }
        return result;
    }

    public int[] flip(int[] data) {
        int[] result = null;
        if (data != null) {
            result = new int[data.length];
            for (int i = 0; i < result.length; i++) {
                result[i] = data[data.length - 1 - i];
            }
        }
        return result;
    }

    public int[][] arraycopy(int[][] data) {
        int[][] result = null;
        if (data != null) {
            result = new int[data.length][];
            for (int i = 0; i < result.length; i++) {
                result[i] = new int[data[i].length];
                for (int j = 0; j < result[i].length; j++) {
                    result[i][j] = data[i][j];
                }
            }
        }
        return result;
    }

    public double[][] sortFitnessDescending(double[][] fitness) {
        // sort
        for (int i = 0; i < fitness.length - 1; i++) {
            int iMAX = i;
            double MAX_FITNESS = fitness[i][1];
            for (int j = i + 1; j < fitness.length; j++) {
                if (fitness[j][1] > MAX_FITNESS) {
                    MAX_FITNESS = fitness[j][1];
                    iMAX = j;
                }
            }
            if (iMAX > i) {
                // swap
                double temp0 = fitness[i][0];
                double temp1 = fitness[i][1];
                fitness[i][0] = fitness[iMAX][0];
                fitness[i][1] = fitness[iMAX][1];
                fitness[iMAX][0] = temp0;
                fitness[iMAX][1] = temp1;
            }
        }
        return fitness;
    }

    // OPERATIONS ==============================================================
    public boolean swapRoute(Individual individual, int index1, int index2) {
        boolean status = false;
        if (individual.getRoute() != null
                && index1 >= 0
                && index2 >= 0
                && index1 < individual.getRoute().length
                && index2 < individual.getRoute().length
                && index1 != index2) {
            int temp = individual.getRoute()[index1];
            individual.getRoute()[index1] = individual.getRoute()[index2];
            individual.getRoute()[index2] = temp;
            status = true;
        }
        return status;
    }

    public boolean[] swapSequence(Individual individual, int[][] swapOperations) {
        boolean[] status = null;
        if (swapOperations != null && swapOperations.length > 0) {
            status = new boolean[swapOperations.length];
            for (int i = 0; i < swapOperations.length; i++) {
                int index1 = swapOperations[i][0];
                int index2 = swapOperations[i][1];
                status[i] = swapRoute(individual, index1, index2);
            }
        }
        return status;
    }

    public Individual swapSequenceWithPartialSearch(Individual individual, int[][] swapOperations) {
        individual.calculateFitness();
        Individual tentative = individual.clone();
        double tentativeFitness = tentative.getFitness();
        if (swapOperations != null) {
            Individual indv = tentative.clone();
            for (int i = 0; i < swapOperations.length; i++) {
                int index1 = swapOperations[i][0];
                int index2 = swapOperations[i][1];
                swapRoute(indv, index1, index2);
                indv.calculateFitness();
                if (indv.getFitness() > tentativeFitness) {
                    tentative = indv.clone();
                    tentativeFitness = indv.getFitness();
                }
            }
        }
        return tentative;
    }

    // SMO & PSO Operation ===========================================================
    public boolean[] add(Individual individual, int[][] swapOperations) {
        return swapSequence(individual, swapOperations);
    }

    public Individual addWithPartialSearch(Individual individual, int[][] swapOperations) {
        return swapSequenceWithPartialSearch(individual, swapOperations);
    }

    public int[] swapRoute(int[] route, int index1, int index2) {
        int[] result = null;
        if (route != null
                && index1 >= 0
                && index2 >= 0
                && index1 < route.length
                && index2 < route.length
                && index1 != index2) {
            result = arraycopy(route);
            int temp = result[index1];
            result[index1] = result[index2];
            result[index2] = temp;
        }
        return result;
    }

    public int[][] subtraction(int[] route1, int[] route2) {
        int[][] swapOperations = null;
        if (route1 != null && route2 != null && route1.length == route2.length) {
            ArrayList<int[]> listSwapOperation = new ArrayList<>();
            int[] routeOperation = arraycopy(route1);
            for (int i = 0; i < route2.length; i++) {
                int value = route2[i];
                for (int j = i; j < routeOperation.length; j++) {
                    if (value == routeOperation[j] && i != j) {
                        int[] so = {i, j};
                        routeOperation = swapRoute(routeOperation, i, j);
                        listSwapOperation.add(so);
                        break;
                    }
                }
            }
            if (!listSwapOperation.isEmpty()) {
                swapOperations = new int[listSwapOperation.size()][2];
                for (int i = 0; i < swapOperations.length; i++) {
                    int[] so = listSwapOperation.get(i);
                    swapOperations[i] = so;
                }
            }
        }
        return swapOperations;
    }

    public int[][] subtraction(Individual individual1, Individual individual2) {
        int[] route1 = arraycopy(individual1.getRoute());
        int[] route2 = arraycopy(individual2.getRoute());
        return subtraction(route1, route2);
    }

    public int[][] merge(int[][] swapsequence1, int[][] swapsequence2) {
        int[][] result = null;
        ArrayList<int[]> swapSequence = new ArrayList<>();
        if (swapsequence1 != null) {
            for (int i = 0; i < swapsequence1.length; i++) {
                swapSequence.add(swapsequence1[i]);
            }
        }
        if (swapsequence2 != null) {
            for (int i = 0; i < swapsequence2.length; i++) {
                swapSequence.add(swapsequence2[i]);
            }
        }
        if (!swapSequence.isEmpty()) {
            result = new int[swapSequence.size()][2];
            for (int i = 0; i < result.length; i++) {
                result[i][0] = swapSequence.get(i)[0];
                result[i][1] = swapSequence.get(i)[1];
            }
        }
        return result;
    }

    public int[][] callBasicSwapSequence(int[][] swapOperations, int customerSize) {
        int[][] basicSS = null;
        try {
            int routeSize = customerSize;
            if (routeSize > 1 && swapOperations != null) {
                int[] route1 = new int[routeSize];
                for (int i = 0; i < route1.length; i++) {
                    route1[i] = i;
                }
                int[] route2 = route1.clone();
                for (int i = 0; i < swapOperations.length; i++) {
                    int index1 = swapOperations[i][0];
                    int index2 = swapOperations[i][1];
                    if (index1 >= 0 && index1 < route2.length && index2 >= 0 && index2 < route2.length) {
                        int temp = route2[index1];
                        route2[index1] = route2[index2];
                        route2[index2] = temp;
                    }
                }
                basicSS = subtraction(route1, route2);
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return basicSS;
    }

    // GA Operations ===========================================================
    public boolean slideRoute(Individual individual, int from, int to, int slideUnit) {
        boolean status = false;
        if (individual.getRoute() != null
                && from >= 0
                && to >= 0
                && from < individual.getRoute().length
                && to < individual.getRoute().length
                && from != to
                && slideUnit > 0
                && slideUnit <= Math.abs(to - from)) {
            int slideLength = Math.abs(to - from) + 1;
            int[] slideArray = arraycopy(individual.getRoute(), from, to);
            for (int i = 0; i < slideLength; i++) {
                int index = (i + slideUnit) % slideLength;
                individual.getRoute()[from + i] = slideArray[index];
            }
            status = true;
        }
        return status;
    }

    public Individual slideRouteWithPartialSearch(Individual individual, int from, int to, int slideUnit) {
        individual.calculateFitness();
        Individual tentative = individual.clone();
        if (slideRoute(tentative, from, to, slideUnit)) {
            tentative.calculateFitness();
            if (tentative.getFitness() > individual.getFitness()) {
                return tentative;
            } else {
                return individual.clone();
            }
        } else {
            return individual.clone();
        }
    }

    public boolean flipRoute(Individual individual, int from, int to) {
        boolean status = false;
        if (individual.getRoute() != null) {
            int[] values = arraycopy(individual.getRoute(), from, to);
            int[] flipValues = flip(values);
            if (flipValues != null
                    && from >= 0
                    && to >= 0
                    && from < individual.getRoute().length
                    && to < individual.getRoute().length) {
                int k = 0;
                for (int i = from; i <= to; i++) {
                    individual.getRoute()[i] = flipValues[k];
                    k++;
                }
                status = true;
            }
        }
        return status;
    }

    public Individual flipRouteWithPartialSearch(Individual individual, int from, int to) {
        individual.calculateFitness();
        Individual tentative = individual.clone();
        if (flipRoute(tentative, from, to)) {
            tentative.calculateFitness();
            if (tentative.getFitness() > individual.getFitness()) {
                return tentative;
            } else {
                return individual.clone();
            }
        } else {
            return individual.clone();
        }
    }

    public boolean breakpointMutation(Individual individual) {
        boolean status = false;
        individual.resetBreakpoint();
        if (individual.getBreakpoint() != null) {
            status = true;
        }
        return status;
    }

    public Individual breakpointMutationWithPartialSearch(Individual individual) {
        individual.calculateFitness();
        Individual tentative = individual.clone();
        if (breakpointMutation(tentative)) {
            tentative.calculateFitness();
            if (tentative.getFitness() > individual.getFitness()) {
                return tentative;
            } else {
                return individual.clone();
            }
        } else {
            return individual.clone();
        }
    }

    public boolean startDepotMutation(Individual individual) {
        boolean status = false;
        individual.resetStartDepot();
        if (individual.getStartDepot() != null) {
            status = true;
        }
        return status;
    }

    public Individual startDepotMutationWithPartialSearch(Individual individual) {
        individual.calculateFitness();
        Individual tentative = individual.clone();
        if (startDepotMutation(tentative)) {
            tentative.calculateFitness();
            if (tentative.getFitness() > individual.getFitness()) {
                return tentative;
            } else {
                return individual.clone();
            }
        } else {
            return individual.clone();
        }
    }
}
