package paint.model.commands;

import paint.model.Shape;
import paint.model.ShapeManager;

public class DeleteShapeCommand implements Command {

    private ShapeManager manager;
    private Shape deletedShape;

    public DeleteShapeCommand(ShapeManager manager, Shape shape) {
        this.manager = manager;
        this.deletedShape = shape;
    }

    @Override
    public void execute() {
        manager.removeShape(deletedShape);
    }

    @Override
    public void undo() {
        manager.addShape(deletedShape);
    }
}
