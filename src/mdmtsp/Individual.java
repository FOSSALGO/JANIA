package mdmtsp;

import java.util.ArrayList;
import java.util.Arrays;

public class Individual {

    private MDMTSP mdmtsp = null;
    private double distance = 0;
    private double fitness = 0;

    //Matrix Representation
    private int[][] path = null;// Path matrix representation.   

    //Route Breakpoint Representation
    private int[] route = null;
    private int[] breakpoint = null;
    private int[] startDepot = null;

    Operation operation = new Operation();

    public Individual(MDMTSP mdmtsp) {
        this.mdmtsp = mdmtsp;
    }

    @Override
    public Individual clone() {
        Individual cloneIndividu = null;
        if (mdmtsp != null) {
            cloneIndividu = new Individual(mdmtsp);
            cloneIndividu.route = operation.arraycopy(route);
            cloneIndividu.breakpoint = operation.arraycopy(breakpoint);
            cloneIndividu.startDepot = operation.arraycopy(startDepot);
            cloneIndividu.path = operation.arraycopy(path);
            cloneIndividu.distance = this.distance;
            cloneIndividu.fitness = this.fitness;
        }
        return cloneIndividu;
    }

    public void setMDMTSP(MDMTSP mdmtsp) {
        this.mdmtsp = mdmtsp;
    }

    public void generateRandomChromosome() {
        if (mdmtsp.isvalid()) {
            // Reset Route
            route = resetRoute();

            // Reset Breakpoints
            breakpoint = resetBreakpoint();

            // Reset Start Depots
            startDepot = resetStartDepot();

            // construct the paths
            setMatrixRepresentation();
        }
    }

    public int[] resetRoute() {
        route = null;
        if (mdmtsp.isvalid()) {
            ArrayList<Integer> customers = mdmtsp.getCustomers();

            ArrayList<Integer> unsetcustomers = new ArrayList<>();
            for (int i = 0; i < customers.size(); i++) {
                unsetcustomers.add(customers.get(i));
            }
            route = new int[customers.size()];
            //set gens for chromosome
            for (int i = 0; i < route.length; i++) {
                //random customers
                int randomIndex = operation.randomBetween(0, unsetcustomers.size() - 1);
                int alele = unsetcustomers.get(randomIndex);
                route[i] = alele;
                unsetcustomers.remove(randomIndex);
            }
        }
        return route;
    }

    public int[] resetBreakpoint() {
        breakpoint = null;
        if (route != null && mdmtsp.isvalid()) {
            //random unique position for salesman
            ArrayList<Integer> listBreakpoint = new ArrayList<>();
            listBreakpoint.add(route.length);// the last breakpoint = length of route
            int m = 1;
            int numberOfSalesmenUsed = operation.randomBetween(1, mdmtsp.getNumberOfSalesmans());
            //System.out.println("Number of Salesmen Used: "+numberOfSalesmenUsed);
            while (m < numberOfSalesmenUsed && m < route.length) {
                int r = operation.randomBetween(1, route.length - 1);
                while (listBreakpoint.contains(r)) {
                    r = operation.randomBetween(1, route.length - 1);
                }
                listBreakpoint.add(r);
                m++;
            }
            //copy breakpoint and random depot
            breakpoint = new int[listBreakpoint.size()];
            for (int i = 0; i < breakpoint.length; i++) {
                breakpoint[i] = listBreakpoint.get(i);
            }
            //sort breakpoint
            Arrays.sort(breakpoint);

            // normalize start depots
            int[] newStartDepot = new int[breakpoint.length];
            int k = 0;
            while (startDepot != null && k < startDepot.length && k < breakpoint.length) {
                newStartDepot[k] = startDepot[k];
                k++;
            }
            ArrayList<Integer> depots = mdmtsp.getDepots();
            while (k < breakpoint.length) {
                int randomDepot = operation.randomBetween(0, depots.size() - 1);
                int depot = depots.get(randomDepot);// set origin depot for salesman
                newStartDepot[k] = depot;
                k++;
            }
            startDepot = newStartDepot;
        }
        return breakpoint;
    }

    public int[] resetStartDepot() {
        startDepot = null;
        if (route != null && breakpoint != null && mdmtsp.isvalid()) {
            ArrayList<Integer> depots = mdmtsp.getDepots();
            startDepot = new int[breakpoint.length];
            for (int i = 0; i < breakpoint.length; i++) {
                int randomDepot = operation.randomBetween(0, depots.size() - 1);
                int depot = depots.get(randomDepot);// set origin depot for salesman
                startDepot[i] = depot;
            }
        }
        return startDepot;
    }

    public void setMatrixRepresentation() {
        // from route breakpoint representation to matrix representation
        if (route != null
                && breakpoint != null
                && startDepot != null
                && breakpoint.length == startDepot.length
                && route.length >= breakpoint[breakpoint.length - 1]) {
            path = new int[breakpoint.length][];
            int start = 0;
            for (int i = 0; i < path.length; i++) {
                int end = breakpoint[i];
                int size = end - start + 1;
                path[i] = new int[size];
                path[i][0] = startDepot[i];
                int k = 1;
                for (int j = start; j < end; j++) {
                    path[i][k] = route[j];
                    k++;
                }
                start = end;
            }
        }
    }

    public void setRouteBreakpointRepresentation() {
        // from matrix representation to route breakpoint representation
        route = null;
        breakpoint = null;
        startDepot = null;
        if (path != null) {
            ArrayList<Integer> cities = new ArrayList<>();
            ArrayList<Integer> depots = new ArrayList<>();
            ArrayList<Integer> breakpoints = new ArrayList<>();
            int k = 0;
            for (int i = 0; i < path.length; i++) {
                if (path[i].length > 1) {
                    depots.add(path[i][0]);
                    for (int j = 1; j < path[i].length; j++) {
                        cities.add(path[i][j]);
                        k++;
                    }
                    breakpoints.add(k);
                }
            }
            // set route, start depot, and breakpoint
            if (!cities.isEmpty()) {
                route = new int[cities.size()];
                for (int i = 0; i < route.length; i++) {
                    route[i] = cities.get(i);
                }
            }
            if (!depots.isEmpty()) {
                startDepot = new int[depots.size()];
                breakpoint = new int[breakpoints.size()];
                for (int i = 0; i < startDepot.length; i++) {
                    startDepot[i] = depots.get(i);
                    breakpoint[i] = breakpoints.get(i);
                }
            }
        }
    }
    
    public void setPath(int[][]path){
        if(this.mdmtsp!=null&&path!=null){
            this.path = operation.arraycopy(path);
            setRouteBreakpointRepresentation();
        }
    }

    public double calculateDistance() {
        distance = 0;
        double[][] distanceMatrix = mdmtsp.getDistanceMatrix();
        setMatrixRepresentation();
        if (path != null && distanceMatrix != null) {
            distance = 0;
            for (int i = 0; i < path.length; i++) {
                if (path[i].length > 1) {
                    int origin = path[i][0];
                    for (int j = 1; j < path[i].length; j++) {
                        int destination = path[i][j];
                        distance += distanceMatrix[origin][destination];
                        origin = destination;
                    }
                    //back to start depot
                    int destination = path[i][0];
                    distance += distanceMatrix[origin][destination];
                }
            }
        }
        return distance;
    }

    public double calculateFitness() {
        fitness = 0;
        calculateDistance();
        if (distance > 0) {
            fitness = 1.0 / distance;
        }
        return fitness;
    }

    public double getDistance() {
        return this.distance;
    }

    public double getFitness() {
        return this.fitness;
    }

    public int[] getRoute() {
        return route;
    }

    public int[] getBreakpoint() {
        return breakpoint;
    }

    public int[] getStartDepot() {
        return startDepot;
    }

    public void setRoute(int[] route, int[] breakpoint, int[] startDepot) {
        this.route = route;
        this.breakpoint = breakpoint;
        this.startDepot = startDepot;
        setMatrixRepresentation();
    }

    public void setPath(ArrayList<ArrayList<Integer>> bestRoutes) {
        this.path = null;
        if (bestRoutes != null) {
            this.path = new int[bestRoutes.size()][];
            for (int i = 0; i < path.length; i++) {
                ArrayList<Integer> routesOfSalesman = bestRoutes.get(i);
                if(!routesOfSalesman.isEmpty()){
                    this.path[i]=new int[routesOfSalesman.size()];
                    for (int j = 0; j < routesOfSalesman.size(); j++) {
                        this.path[i][j]=routesOfSalesman.get(j);
                    }
                }
            }
        }
    }

    /**
     *
     * @return value of path
     */
    @Override
    public String toString() {
        String result = null;
        if (path != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("----------------------------------------------------\n");
            for (int i = 0; i < path.length; i++) {
                sb.append("Salesman-" + (i + 1) + " ");
                sb.append(Arrays.toString(path[i]));
                sb.append("\n");
            }
            sb.append("----------------------------------------------------\n");
            sb.append("Route Breakpoint Representation\n");
            sb.append("Route      : " + Arrays.toString(route) + "\n");
            sb.append("Start Depot: " + Arrays.toString(startDepot) + "\n");
            sb.append("Breakpoint : " + Arrays.toString(breakpoint) + "\n");
            sb.append("----------------------------------------------------\n");
            sb.append("Distance   : " + distance + "\n");
            sb.append("Fitness    : " + fitness + "\n");
            result = sb.toString();
        }
        return result;
    }
}
