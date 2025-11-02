package paint.model;

import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

// Adapter class implementing iPenDrawing interface
// this class translates method calls from java into JavaScript commands (the two incompatible systems)
public class PenAdapter implements iPenDrawing {

    private final WebEngine engine; // WebEngine to execute JavaScript commands on the WebView(where the pen tool is implemented)

    public PenAdapter(WebView webView) {
        this.engine = webView.getEngine(); //initialize the WebEngine from the provided WebView
    }


    //Pen size is set (slider) ==> directly modifying the lineWidth variable in JS
    @Override
    public void setPenSize(double size) {
        engine.executeScript("document.getElementById('lineWidth').value = " + size);
    }

    //Eraser size is set (slider) ==> directly modifying the eraseSize variable in JS
    @Override
    public void setEraserSize(double size) {
        engine.executeScript("eraseSize = " + (int) size + ";");

    }

    //set the color of the pen by converting JavaFX Color to hex string and passing it to JS
    @Override
    public void setColor(Color color) {
        String hex = String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
        engine.executeScript("document.getElementById('color').value = '" + hex + "'");
    }

    //pen mode on
    @Override
    public void usePen() { 
        engine.executeScript("orderDrawing = true; orderErasing = false;");
    }

    //eraser mode on
    @Override
    public void useEraser() {
        engine.executeScript("orderDrawing = false; orderErasing = true;");
    }
}
