package paint.controller;

import java.util.HashMap;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import paint.model.*; 

public class RectangleFactory implements iShapeFactory {
    
     @Override
    public Shape createShape(Point2D start, Point2D end, Color color) {
        return new Rectangle(start, end, color);
    }

    @Override
    public Shape createShape(HashMap<String, Double> m) {
        Shape rectangle = new Rectangle();
        rectangle.setProperties(m);
        return rectangle;
    }
    
}
