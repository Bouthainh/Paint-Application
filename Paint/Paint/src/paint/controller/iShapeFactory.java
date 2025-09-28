package paint.controller;

import java.util.HashMap;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import paint.model.*;


public interface iShapeFactory {

         Shape createShape( HashMap<String,Double> m);
         Shape createShape (Point2D start, Point2D end, Color color);  
}
   