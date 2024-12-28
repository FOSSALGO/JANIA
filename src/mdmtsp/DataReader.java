package mdmtsp;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DataReader {

    public double[][] distanceMatrix = null;
    public String EDGE_WEIGHT_TYPE = null;

    public double[][] read(String filename) {
        try {
            File file = new File(filename);
            Scanner sc = new Scanner(file);
            int dimension = 0;
            has_next:
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                String[] values = line.split(":");
                if (values[0].trim().equalsIgnoreCase("DIMENSION")) {
                    dimension = Integer.parseInt(values[1].trim());
                    distanceMatrix = new double[dimension][dimension];
                } else if (values[0].trim().equalsIgnoreCase("EDGE_WEIGHT_TYPE")) {
                    String type = values[1].trim().toUpperCase();
                    EDGE_WEIGHT_TYPE = type;
                } else if (values[0].trim().equalsIgnoreCase("EDGE_WEIGHT_SECTION") && EDGE_WEIGHT_TYPE.equalsIgnoreCase("EXPLICIT")) {
                    int r = 0;
                    int c = 0;
                    while (sc.hasNextLine() && r < dimension) {
                        line = sc.nextLine();
                        if (line.equalsIgnoreCase("EOF")) {
                            break has_next;
                        } else {
                            values = line.split("\s+");
                            for (String s : values) {
                                s = s.trim();
                                if (s.length() > 0) {
                                    double d = Double.parseDouble(s);
                                    distanceMatrix[r][c] = d;
                                    c++;
                                    if (c >= dimension) {
                                        c = 0;
                                        r++;
                                    }
                                }
                            }
                        }
                    }
                } else if (values[0].trim().equalsIgnoreCase("NODE_COORD_SECTION") && EDGE_WEIGHT_TYPE.equalsIgnoreCase("GEO")) {
                    int i = 0;
                    double[][] nodeCoordinate = new double[dimension][2];
                    while (sc.hasNextLine() && i < dimension) {
                        line = sc.nextLine();
                        if (line.equalsIgnoreCase("EOF")) {
                            break has_next;
                        } else {
                            values = line.split("\s+");
                            //int number = Integer.parseInt(values[0].trim());
                            double x = Double.parseDouble(values[1].trim());
                            double y = Double.parseDouble(values[2].trim());
                            nodeCoordinate[i][0] = x;
                            nodeCoordinate[i][1] = y;
                            i++;
                        }
                    }
                    //calculate distance;
                    distanceMatrix = distancesInGEO(nodeCoordinate);
                }

            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(DataReader.class.getName()).log(Level.SEVERE, null, ex);
        }
        return distanceMatrix;
    }

    static double[][] distancesInGEO(double[][] nodes) {
        int dim = nodes.length;
        double[] latitude = new double[dim];
        double[] longitude = new double[dim];

        final double PI = Math.PI;//3.141592;
        for (int i = 0; i < dim; i++) {
            int deg = (int)(nodes[i][0]);
            double min = nodes[i][0] - deg;
            latitude[i] = PI * (deg + 5 * min / 3.0) / 180;
            deg =  (int)(nodes[i][1]);
            min = nodes[i][1] - deg;
            longitude[i] = PI * (deg + 5 * min / 3.0) / 180;
        }

        double[][] d = new double[dim][dim];

        final double RRR = 6378.388;
        for (int i = 0; i < dim; i++) {
            for (int j = i + 1; j < dim; j++) {
                double q1 = Math.cos(longitude[i] - longitude[j]);
                double q2 = Math.cos(latitude[i] - latitude[j]);
                double q3 = Math.cos(latitude[i] + latitude[j]);
                //d[i][j] = (int) (RRR * Math.acos(0.5 * ((1.0 + q1) * q2 - (1.0 - q1) * q3)) + 1.0);
                d[i][j] = (int)(RRR * Math.acos(0.5 * ((1.0 + q1) * q2 - (1.0 - q1) * q3)) + 1.0);
                d[j][i] = d[i][j];
            }
        }
        return d;
    }
}
