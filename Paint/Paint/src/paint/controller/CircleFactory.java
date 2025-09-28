package paint.controller;

import java.util.HashMap;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import paint.model.*;



public class CircleFactory implements iShapeFactory {

    @Override
    public Shape createShape(Point2D start, Point2D end, Color color) {
        // compute radius as min of width and height
        double radius = Math.min(
            Math.abs(end.getX() - start.getX()), 
            Math.abs(end.getY() - start.getY())
        );

        // adjust end point so width=height
        Point2D adjustedEnd = new Point2D(start.getX() + radius, start.getY() + radius);

        return new Circle(start, adjustedEnd, color); 
    }

    @Override
    public Shape createShape(HashMap<String, Double> m) {
        Shape circle = new Circle();
        circle.setProperties(m);
        return circle;
    }
}
