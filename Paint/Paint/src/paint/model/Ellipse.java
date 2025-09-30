
package paint.model;

import java.util.HashMap;

import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import paint.controller.iShapeFactory;


public class Ellipse extends Shape implements iShapeFactory{
    private double hRadius;
    private double vRadius;

    public Ellipse() {
        
    }

    public void sethRadius(double hRadius) {
        this.hRadius = hRadius;
    }

    public void setvRadius(double vRadius) {
        this.vRadius = vRadius;
    }

    public double gethRadius() {
        return hRadius;
    }

    public double getvRadius() {
        return vRadius;
    }
    
    
    public Ellipse(Point2D startPos, Point2D endPos, Color strockColor) {
        super(startPos, endPos, strockColor);
        hRadius = Math.abs(startPos.getX() - endPos.getX())/2;
        vRadius = Math.abs(startPos.getY() - endPos.getY())/2;    
    }
    
    @Override
    protected void getPropertiesToMap(){
        super.getPropertiesToMap();
        super.addToProperties("hRadius", hRadius);
        super.addToProperties("vRadius", vRadius);
    }
    
    @Override
    protected void setPropertiesToVariables(){
        super.setPropertiesToVariables();
        hRadius = super.getFromMap("hRadius");
        vRadius = super.getFromMap("vRadius");
    }
    
    @Override
    public void draw(Canvas canvas){
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setStroke(super.getColor());
        gc.strokeOval(super.getTopLeft().getX(), super.getTopLeft().getY(), hRadius*2, vRadius*2);
        gc.setFill(super.getFillColor());
        gc.fillOval(super.getTopLeft().getX(), super.getTopLeft().getY(), hRadius*2, vRadius*2);
    }
    
    @Override
        public Shape createShape(Point2D start, Point2D end, Color color) {
        return new Ellipse(start, end, color);
    }

    @Override
    public Shape createShape(HashMap<String, Double> m) {
        Shape ellipse = new Ellipse();
        ellipse.setProperties(m);
        return ellipse;
    }
    
}
