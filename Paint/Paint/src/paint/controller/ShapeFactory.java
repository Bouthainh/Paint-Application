
package paint.controller;

import java.util.HashMap;
import java.util.Map;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import paint.model.*;


//Factory DP
public class ShapeFactory  {
    
private static final Map<String, iShapeFactory> factories = new HashMap<>();

    static {
        factories.put("Ellipse", new Ellipse());
        factories.put("Rectangle", new Rectangle());
        factories.put("Line", new Line());
        factories.put("Triangle", new Triangle());
        factories.put("Square", new Square());
        factories.put("Circle", new Circle());   
    }

    public static Shape createShape(String type, Point2D start, Point2D end, Color color) {
        iShapeFactory factory = factories.get(type);
        return factory != null ? factory.createShape(start, end, color) : null;
    }

    public static Shape createShape(String type, HashMap<String, Double> m) {
        iShapeFactory factory = factories.get(type);
        return factory != null ? factory.createShape(m) : null;
    }
}