package com.ettrema.web.image;


public class Dimensions {
    private double x;
    private double y;

    public Dimensions(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    public Dimensions() {
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }
    
    public void scaleToFitWithin( int length ) {
        scaleToFitWithin((double)length);
    }
    
    /** If the image size would fall outside of a square with sides length, the 
     *  dimensions are scaled so that that it just fits within the square
     *
     */
    public void scaleToFitWithin( double length ) {
        double ratio;
        if( x > length) {
            ratio = length/x;
        } else if( y > length ) {
            ratio = length/y;
        } else {
            return ;    // already fits
        }
        x = x * ratio;
        y = y * ratio;
        
    }
}
