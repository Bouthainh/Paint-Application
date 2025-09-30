
package paint.model;

import java.util.HashMap;

import javafx.geometry.Point2D;
import javafx.scene.paint.Color;


public class Circle extends Ellipse {
    
    public Circle(Point2D startPos, Point2D endPos, Color strockColor) {
        super(startPos, endPos, strockColor);
        if(super.gethRadius()<super.getvRadius()){
            super.setvRadius(super.gethRadius());
        }else{
            super.sethRadius(super.getvRadius());
        }
        
    }

    public Circle() {
        
    }
    
    @Override
    public Shape createShape(Point2D start, Point2D end, Color color) {
        return new Circle(start, end, color);
    }

    @Override
    public Shape createShape(HashMap<String, Double> m) {
        Shape Circle = new Circle();
        Circle.setProperties(m);
        return Circle;
    }
    
}
