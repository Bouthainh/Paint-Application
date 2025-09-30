package paint.model;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.List;

public class CanvasManager {
    // Singleton instance
    private static CanvasManager instance = null;

    // Reference to the Canvas
    private Canvas canvas;

    // Private constructor
    private CanvasManager() {}

    // Singleton access
    public static CanvasManager getInstance() {
        if (instance == null) {
            instance = new CanvasManager();
            System.out.println("[CanvasManager] New instance created.");
        }
        return instance;
    }

    // Attach the Canvas (from FXML)
    public void setCanvas(Canvas c) {
        this.canvas = c;
        System.out.println("[CanvasManager] Canvas attached.");
    }

    // Get GraphicsContext
    public GraphicsContext getGraphicsContext() {
        if (canvas == null) {
            throw new IllegalStateException("Canvas not set! Call setCanvas() first.");
        }
        return canvas.getGraphicsContext2D();
    }

    // Clear the canvas
    public void clearCanvas() {
        if (canvas != null) {
            getGraphicsContext().setFill(Color.WHITE);
            getGraphicsContext().fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
            System.out.println("[CanvasManager] Canvas cleared.");
        }
    }

    // Redraw all shapes
    public void redraw(List<Shape> shapes) {
        if (canvas != null) {
            clearCanvas();
            for (Shape s : shapes) {
                try {
                    s.draw(canvas);
                } catch (Exception e) {
                    System.out.println("[CanvasManager] Error drawing shape: " + s.getClass().getSimpleName());
                }
            }
            System.out.println("[CanvasManager] Redraw finished. Shapes=" + shapes.size());
        }
    }
}
