package paint.model;

import java.util.ArrayList;
import java.util.List;

public class ShapeManager {

    // Singleton instance
    private static ShapeManager instance =null;

    // Store all shapes
    private List<Shape> shapeList;

    // Private constructor
    private ShapeManager() {
        shapeList = new ArrayList<>();
    }

    // Access method
    public static ShapeManager getInstance() {
        if (instance == null) {
            instance = new ShapeManager();
        }
        return instance;
    }

    // Manipulate shapes
    public void addShape(Shape shape) {
        shapeList.add(shape);
    }

    public void removeShape(Shape shape) {
        shapeList.remove(shape);
    }

    public List<Shape> getShapes() {
        return shapeList;
    }

    public void clearShapes() {
        shapeList.clear();
    }

    // Corrected setShapes for undo/redo
    public void setShapes(ArrayList<Shape> newShapes){
        shapeList.clear();
        shapeList.addAll(newShapes);
    }
}
