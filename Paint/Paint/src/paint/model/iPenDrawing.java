package paint.model;

import javafx.scene.paint.Color;

// Interface for pen drawing functionalities
//to be implemented by PenAdapter
//to be used in FXMLDocumentController, for loose coupling purposes
public interface iPenDrawing {


    void setPenSize(double size);
    void setEraserSize(double size);
    void setColor(Color color);
    void usePen();
    void useEraser();
}
