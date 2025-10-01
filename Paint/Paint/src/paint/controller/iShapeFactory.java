package paint.controller;

import java.util.HashMap;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import paint.model.*;

//Factory DP: Interface with overloaded methods for creating shapes
public interface iShapeFactory {

        //overloaded methods: Same method name with different parameters
        // for GUI creation
         Shape createShape( HashMap<String,Double> m);
        // for Data file creation
         Shape createShape (Point2D start, Point2D end, Color color);  
}
   