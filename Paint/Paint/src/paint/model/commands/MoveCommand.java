package paint.model.commands;

import paint.model.Shape;
import paint.model.ShapeManager;
import javafx.geometry.Point2D;

public class MoveCommand implements Command {

    private Shape shape;
    private Point2D oldPosition;
    private Point2D newPosition;

    public MoveCommand(Shape shape, Point2D oldPosition, Point2D newPosition) {
        this.shape = shape;
        this.oldPosition = oldPosition;
        this.newPosition = newPosition;
    }

    @Override
    public void execute() {
        shape.setTopLeft(newPosition);
    }

    @Override
    public void undo() {
        shape.setTopLeft(oldPosition);
    }
}
