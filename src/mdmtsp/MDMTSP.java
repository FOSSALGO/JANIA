package mdmtsp;

import java.util.ArrayList;

public class MDMTSP {

    private double[][] distanceMatrix = null;
    private int numberOfSalesmans = 0;//NUMBER_OF_SALESMANS = m
    private ArrayList<Integer> depots = null;
    private ArrayList<Integer> customers = null;

    public MDMTSP(double[][] distanceMatrix, int[] depots, int numberOfSalesmans) {
        setDistanceMatrix(distanceMatrix);
        setDepots(depots);
        setCustomers();
        setNumberOfSalesmans(numberOfSalesmans);
    }

    public void setDistanceMatrix(double[][] distanceMatrix) {
        if (distanceMatrix != null) {
            boolean valid = true;
            for (int i = 0; i < distanceMatrix.length; i++) {
                if (distanceMatrix[i].length != distanceMatrix.length) {
                    valid = false;
                    break;
                }
            }
            if (valid) {
                this.distanceMatrix = new double[distanceMatrix.length][distanceMatrix.length];
                for (int i = 0; i < distanceMatrix.length; i++) {
                    for (int j = 0; j < distanceMatrix[i].length; j++) {
                        this.distanceMatrix[i][j] = distanceMatrix[i][j];
                    }
                }
            }
        }
    }

    public void setDepots(int[] depots) {
        if (depots != null && depots.length > 0 && distanceMatrix != null && distanceMatrix.length > 0) {
            this.depots = new ArrayList<>();
            for (int i = 0; i < depots.length; i++) {
                if (depots[i] >= 0 && depots[i] < distanceMatrix.length) {
                    this.depots.add(depots[i]);
                }
            }
        }
    }

    public void setCustomers() {
        if (distanceMatrix != null && distanceMatrix.length > 0 && this.depots != null) {
            customers = new ArrayList<>();
            for (int i = 0; i < distanceMatrix.length; i++) {
                if (!depots.contains(i)) {
                    customers.add(i);
                }
            }
        }
    }

    public void setNumberOfSalesmans(int m) {
        if (m > 0) {
            this.numberOfSalesmans = m;
        }
    }

    public boolean isvalid() {
        // validation
        boolean valid = true;
        if (this.numberOfSalesmans <= 0
                || this.distanceMatrix == null
                || this.depots == null
                || this.customers == null) {
            valid = false;
        }
        return valid;
    }
    
    public double[][] getDistanceMatrix(){
        return this.distanceMatrix;
    }

    public ArrayList<Integer> getDepots() {
        return this.depots;
    }

    public ArrayList<Integer> getCustomers() {
        return this.customers;
    }

    public int getNumberOfSalesmans() {
        return this.numberOfSalesmans;
    }
}
