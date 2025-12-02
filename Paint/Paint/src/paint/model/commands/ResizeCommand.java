package paint.model.commands;

import paint.model.Shape;
import paint.model.ShapeManager;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;

public class ResizeCommand implements Command {

    private ShapeManager manager;
    private Shape oldShape;
    private Shape newShape;

    public ResizeCommand(ShapeManager manager, Shape oldShape, Shape newShape) {
        this.manager = manager;
        this.oldShape = oldShape;
        this.newShape = newShape;
    }

    @Override
    public void execute() {
        manager.removeShape(oldShape);
        manager.addShape(newShape);
    }

    @Override
    public void undo() {
        manager.removeShape(newShape);
        manager.addShape(oldShape);
    }
}
