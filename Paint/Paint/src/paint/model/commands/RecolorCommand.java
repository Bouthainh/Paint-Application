package paint.model.commands;

import javafx.scene.paint.Color;
import paint.model.Shape;

public class RecolorCommand implements Command {

    private Shape shape;
    private Color oldColor;
    private Color newColor;

    public RecolorCommand(Shape shape, Color oldColor, Color newColor) {
        this.shape = shape;
        this.oldColor = oldColor;
        this.newColor = newColor;
    }

    @Override
    public void execute() {
        shape.setFillColor(newColor);
    }

    @Override
    public void undo() {
        shape.setFillColor(oldColor);
    }
}
