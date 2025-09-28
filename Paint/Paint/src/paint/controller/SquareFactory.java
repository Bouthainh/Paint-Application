package paint.controller;

import java.util.HashMap;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import paint.model.*;

public class SquareFactory implements iShapeFactory {

    @Override
    public Shape createShape(Point2D start, Point2D end, Color color) {
        // enforce square by taking min(width, height)
        double side = Math.min(
            Math.abs(end.getX() - start.getX()), 
            Math.abs(end.getY() - start.getY())
        );

        // adjust end point so width=height
        Point2D adjustedEnd = new Point2D(start.getX() + side, start.getY() + side);

        return new Square(start, adjustedEnd, color); 
    }

    @Override
    public Shape createShape(HashMap<String, Double> m) {
        Shape square = new Square();
        square.setProperties(m);
        return square;
    }
}
