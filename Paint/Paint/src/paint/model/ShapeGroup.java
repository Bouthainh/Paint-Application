package paint.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

public class ShapeGroup extends Shape {
  private ArrayList<Shape> shapes;
    private String name;

    public ShapeGroup() {
        shapes = new ArrayList<>();
        this.name = "Group_" + System.currentTimeMillis(); 
    }

    public ShapeGroup(String name) {
        this();
        this.name = name;
    }

    public void addShape(Shape shape) {
        shapes.add(shape);
    }

    public void removeShape(Shape shape) {
        shapes.remove(shape);
    }

    public ArrayList<Shape> getChildren() {
        return shapes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void draw(Canvas canvas) {
        for (Shape s : shapes) {
            s.draw(canvas);
        }
    }

    @Override
    public Shape cloneShape() throws CloneNotSupportedException {
        ShapeGroup clone = new ShapeGroup(this.name + "_Copy");
        for (Shape s : shapes) {
            clone.addShape(s.cloneShape());
        }
        return clone;
    }

    @Override
    public void setTopLeft(Point2D pos) {
        Point2D currentTopLeft = getTopLeft();
        double dx = pos.getX() - currentTopLeft.getX();
        double dy = pos.getY() - currentTopLeft.getY();

        for (Shape s : shapes) {
            Point2D newPos = new Point2D(s.getTopLeft().getX() + dx, s.getTopLeft().getY() + dy);
            s.setTopLeft(newPos);
        }
        super.setTopLeft(pos);
    }

    @Override
    public void setColor(Color color) {
        for (Shape s : shapes) {
            s.setColor(color);
        }
    }

    @Override
    public void setFillColor(Color color) {
        for (Shape s : shapes) {
            s.setFillColor(color);
        }
    }

    @Override
    public Shape createShape(Point2D start, Point2D end, Color color) {
        return null; // Not needed for grouping
    }

    @Override
    public Shape createShape(HashMap<String, Double> properties) {
        return null; // Not needed for grouping
    }

    // الحصول على أعلى نقطة يسارية للمجموعة
    @Override
    public Point2D getTopLeft() {
        if (shapes.isEmpty()) {
            return new Point2D(0, 0);
        }
        
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        
        for (Shape s : shapes) {
            Point2D topLeft = s.getTopLeft();
            minX = Math.min(minX, topLeft.getX());
            minY = Math.min(minY, topLeft.getY());
        }
        
        return new Point2D(minX, minY);
    }
}
