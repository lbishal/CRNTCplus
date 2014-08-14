package de.uni_passau.fim.esl.crn_toolbox_center.view;

/**
 * Represents a two-dimensional coordinate on a chart graph.
 * The x and y values are absolute values and do not match the position
 * of the coordinate on the display.
 * 
 * @author Jakob Weigert <weigert@fim.uni-passau.de> (University of Passau)
 *
 */
public class Coordinate {
    private double x;
    private double y;


    /**
     * Creates a new coordinate.
     * 
     * @param x
     * @param y
     */
    public Coordinate(double x, double y) {
        super();
        this.x = x;
        this.y = y;
    }

    /**
     * @return the x
     */
    public double getX() {
        return x;
    }

    /**
     * @return the y
     */
    public double getY() {
        return y;
    }
    
    

}
