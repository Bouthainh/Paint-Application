package paint.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.web.WebView;
import java.util.ArrayList;
import java.util.List;


import paint.model.*;
import paint.model.commands.*;

public class FXMLDocumentController implements Initializable, DrawingEngine, iModeObserver {

    /* ================== FXML UI CONTROLS ================== */
    @FXML private Button DeleteBtn, UndoBtn, RedoBtn, MoveBtn, ResizeBtn, CopyBtn;
    @FXML private Button SaveBtn, LoadBtn, ImportBtn, StartBtn, PathBtn, RecolorBtn;
    @FXML private ComboBox<String> ShapeBox;
    @FXML private ColorPicker ColorBox;
    @FXML private GridPane After;
    @FXML private Pane Before, PathPane;
    @FXML private TextField PathText;
    @FXML private Canvas CanvasBox;
    @FXML private Label Message;
    @FXML private ListView<String> ShapeList;

    @FXML private Button GroupBtn, UngroupBtn;

    @FXML private MenuButton PenMenu;
    @FXML private Slider PenSizeSlider, EraserSizeSlider;
    @FXML private Button UsePenBtn, UseEraserBtn;
    @FXML private WebView PenWebView;

    /* ================== CLASS VARIABLES ================== */
    private Point2D start;
    private Point2D end;

    private ShapeManager manager = ShapeManager.getInstance();
    private CanvasManager canvasManager = CanvasManager.getInstance();
    private iModeManager modeManager = new ModeManager();
    private CommandManager commandManager = CommandManager.getInstance();

    private boolean move = false;
    private boolean resize = false;
    private boolean copy = false;
    private boolean save = false;
    private boolean load = false;
    private boolean importt = false;

    private boolean penMode = false;
    private iPenDrawing pen;

    private ObservableList<String> shapeNames; // ← Linked to ShapeList

    /* ================== HELPER METHODS ================== */
    private boolean isShapeGroup(Shape shape) { return shape instanceof ShapeGroup; }
    private ShapeGroup getShapeGroup(Shape shape) { return (ShapeGroup) shape; }

    /* ================== BUTTON HANDLING ================== */
    @FXML
    private void handleButtonAction(ActionEvent event) throws CloneNotSupportedException {

        if (event.getSource() == StartBtn) {
            Before.setVisible(false);
            After.setVisible(true);
        }

        Message.setText("");

        if (event.getSource() == DeleteBtn) {
            if (ShapeList.getSelectionModel().isEmpty()) {
                Message.setText("Choose a shape first.");
                return;
            }
            Shape target = manager.getShapes().get(ShapeList.getSelectionModel().getSelectedIndex());
            commandManager.executeCommand(new DeleteShapeCommand(manager, target));
            refresh(CanvasBox);
        }

        if (event.getSource() == RecolorBtn) {
            if (ShapeList.getSelectionModel().isEmpty()) {
                Message.setText("Choose a shape first.");
                return;
            }
            Shape s = manager.getShapes().get(ShapeList.getSelectionModel().getSelectedIndex());
            Color oldColor = s.getFillColor();
            Color newColor = ColorBox.getValue();
            commandManager.executeCommand(new RecolorCommand(s, oldColor, newColor));
            refresh(CanvasBox);
        }

        if (event.getSource() == MoveBtn) {
            if (!ShapeList.getSelectionModel().isEmpty()) {
                move = true;
                Message.setText("Click where you want to move the shape.");
            } else Message.setText("Choose a shape first.");
        }

        if (event.getSource() == ResizeBtn) {
            if (!ShapeList.getSelectionModel().isEmpty()) {
                resize = true;
                Message.setText("Drag to resize the shape.");
            } else Message.setText("Choose a shape first.");
        }

        if (event.getSource() == CopyBtn) {
            if (!ShapeList.getSelectionModel().isEmpty()) {
                copy = true;
                Message.setText("Click where you want to place the copy.");
            } else Message.setText("Choose a shape first.");
        }

        if (event.getSource() == UndoBtn) {
            commandManager.undo();
            refresh(CanvasBox);
        }

        if (event.getSource() == RedoBtn) {
            commandManager.redo();
            refresh(CanvasBox);
        }

        if (event.getSource() == SaveBtn) { showPathPane(); save = true; }
        if (event.getSource() == LoadBtn) { showPathPane(); load = true; }
        if (event.getSource() == ImportBtn) { showPathPane(); importt = true; }

        if (event.getSource() == PathBtn) {
            if (PathText.getText().isEmpty()) {
                Message.setText("Set a path first.");
                return;
            }
            if (save)   { save   = false; save(PathText.getText()); }
            else if (load)    { load    = false; load(PathText.getText()); }
            else if (importt) { importt = false; installPluginShape(PathText.getText()); }
            hidePathPane();
        }

        if (event.getSource() == GroupBtn) {
            var selected = ShapeList.getSelectionModel().getSelectedIndices();
            if (selected.size() < 2) { Message.setText("Select 2 or more shapes."); return; }

            ShapeGroup group = new ShapeGroup("Group_" + (manager.getShapes().size() + 1));
            ArrayList<Shape> toRemove = new ArrayList<>();
            for (Object i : selected) {
                Shape s = manager.getShapes().get((int) i);
                group.addShape(s);
                toRemove.add(s);
            }
            toRemove.forEach(manager::removeShape);
            manager.addShape(group);
            refresh(CanvasBox);
            Message.setText("Grouped successfully!");
        }

        if (event.getSource() == UngroupBtn) {
            if (ShapeList.getSelectionModel().isEmpty()) { Message.setText("Choose a group."); return; }
            Shape selected = manager.getShapes().get(ShapeList.getSelectionModel().getSelectedIndex());
            if (!isShapeGroup(selected)) { Message.setText("Not a group."); return; }
            ShapeGroup group = getShapeGroup(selected);
            manager.removeShape(group);
            for (Shape s : group.getChildren()) manager.addShape(s);
            refresh(CanvasBox);
        }
    }

    /* ================== DRAG EVENTS ================== */
    public void startDrag(MouseEvent e) { start = new Point2D(e.getX(), e.getY()); Message.setText(""); }
    public void endDrag(MouseEvent e) throws CloneNotSupportedException {
        end = new Point2D(e.getX(), e.getY());
        if (end.equals(start)) clickFunction();
        else dragFunction();
    }

    public void clickFunction() throws CloneNotSupportedException {
        if (move) { move = false; executeMoveCommand(); }
        else if (copy) { copy = false; executeCopy(); }
        else if (resize) { resize = false; Message.setText("Drag instead of click for resizing."); }
    }

    private void executeMoveCommand() {
        Shape s = manager.getShapes().get(ShapeList.getSelectionModel().getSelectedIndex());
        Point2D oldPos = s.getTopLeft();
        commandManager.executeCommand(new MoveCommand(s, oldPos, start));
        refresh(CanvasBox);
    }

    private void executeCopy() throws CloneNotSupportedException {
        Shape original = manager.getShapes().get(ShapeList.getSelectionModel().getSelectedIndex());
        Shape copy = original.cloneShape();
        copy.setTopLeft(start);
        manager.addShape(copy);
        refresh(CanvasBox);
    }

    private void executeResize() {
        int index = ShapeList.getSelectionModel().getSelectedIndex();
        Shape oldShape = manager.getShapes().get(index);
        if (isShapeGroup(oldShape)) { Message.setText("Cannot resize a group."); return; }
        Shape newShape = new ShapeFactory().createShape(
                oldShape.getClass().getSimpleName(), oldShape.getTopLeft(), end, ColorBox.getValue()
        );
        commandManager.executeCommand(new ResizeCommand(manager, oldShape, newShape));
        refresh(CanvasBox);
    }

    public void dragFunction() throws CloneNotSupportedException {
        if (resize) { executeResize(); resize = false; return; }
        Shape sh;
        try { sh = new ShapeFactory().createShape(ShapeBox.getValue(), start, end, ColorBox.getValue()); }
        catch (Exception e) { Message.setText("Choose a shape first."); return; }
        addShape(sh);
        sh.draw(CanvasBox);
    }

    /* ================== UI Utility ================== */
    public void showPathPane() { Message.setVisible(false); PathPane.setVisible(true); }
    public void hidePathPane() { PathPane.setVisible(false); Message.setVisible(true); }

    /* ================== LIST UPDATE ================== */
    private void updateShapeList() {
        shapeNames.clear();
        for (Shape s : manager.getShapes()) {
            if (s instanceof ShapeGroup) {
                ShapeGroup g = (ShapeGroup) s;
                shapeNames.add("Group (" + g.getChildren().size() + ") (" +
                               (int) s.getTopLeft().getX() + "," +
                               (int) s.getTopLeft().getY() + ")");
            } else {
                shapeNames.add(s.getClass().getSimpleName() + " (" +
                               (int) s.getTopLeft().getX() + "," +
                               (int) s.getTopLeft().getY() + ")");
            }
        }
    }

    /* ================== INITIALIZE ================== */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        ShapeBox.setItems(FXCollections.observableArrayList(
                "Circle","Ellipse","Rectangle","Square","Triangle","Line"
        ));

        ColorBox.setValue(Color.BLACK);
        canvasManager.setCanvas(CanvasBox);

        shapeNames = FXCollections.observableArrayList();
        ShapeList.setItems(shapeNames);
        ShapeList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        setupPenTool();
        modeManager.addObserver(this);

        refresh(CanvasBox);
    }

    /* ================== ADAPTER / OBSERVER ================== */
    private void setupPenTool() {
        PenWebView.getEngine().load(getClass().getResource("/paint/ExternalSystem/index.html").toExternalForm());
        PenWebView.setVisible(false); PenWebView.setMouseTransparent(true);

        PenWebView.getEngine().getLoadWorker().stateProperty().addListener((obs, old, state) -> {
            if (state == javafx.concurrent.Worker.State.SUCCEEDED) {
                pen = new PenAdapter(PenWebView);
                pen.setPenSize(PenSizeSlider.getValue());
                pen.setEraserSize(EraserSizeSlider.getValue());
                pen.setColor(ColorBox.getValue());

                PenSizeSlider.valueProperty().addListener((o, ov, nv) -> pen.setPenSize(nv.doubleValue()));
                EraserSizeSlider.valueProperty().addListener((o, ov, nv) -> pen.setEraserSize(nv.doubleValue()));
                ColorBox.valueProperty().addListener((o, ov, nv) -> pen.setColor(nv));

                UsePenBtn.setOnAction(e -> { pen.usePen(); modeManager.setMode(DrawingModeEnum.PEN); });
                UseEraserBtn.setOnAction(e -> { pen.useEraser(); modeManager.setMode(DrawingModeEnum.ERASER); });
                ShapeBox.setOnAction(e -> modeManager.setMode(DrawingModeEnum.SHAPE));
            }
        });

        CanvasBox.setOnMousePressed(e -> { if (!penMode) startDrag(e); });
        CanvasBox.setOnMouseReleased(e -> { if (!penMode) try { endDrag(e); } catch (CloneNotSupportedException ex) { ex.printStackTrace(); } });
    }

    @Override
    public void onModeUpdate(DrawingModeEnum newMode) {
        switch (newMode) {
            case PEN: penMode = true; PenWebView.setVisible(true); PenWebView.setMouseTransparent(false); PenWebView.toFront(); CanvasBox.setCursor(javafx.scene.Cursor.DEFAULT); break;
            case ERASER: penMode = true; PenWebView.setVisible(true); PenWebView.setMouseTransparent(false); PenWebView.toFront(); break;
            case SHAPE: penMode = false; PenWebView.setVisible(false); PenWebView.setMouseTransparent(true); CanvasBox.toFront(); CanvasBox.setCursor(javafx.scene.Cursor.CROSSHAIR); break;
        }
    }

    /* ================== SAVE / LOAD ================== */
    @Override
    public void save(String path) {
        if (path.endsWith(".xml")) {
            SaveToXML saver = new SaveToXML(path, manager.getShapes());
            Message.setText(saver.checkSuccess() ? "File saved." : "Saving failed.");
        } else Message.setText("Use .xml file.");
    }

    @Override
    public void load(String path) {
        if (path.endsWith(".xml")) {
            try {
                LoadFromXML loader = new LoadFromXML(path);
                if (loader.checkSuccess()) {
                    manager.setShapes(loader.getList());
                    refresh(CanvasBox);
                } else Message.setText("Loading failed.");
            } catch (Exception e) { Message.setText("Error loading file."); }
        } else Message.setText("Use .xml file.");
    }

    @Override
    public List<Class<? extends Shape>> getSupportedShapes() { return null; }
    @Override
    public void installPluginShape(String jarPath) { Message.setText("Plugins not supported yet."); }

    /* ================== DrawingEngine INTERFACE ================== */
    @Override
    public void addShape(Shape shape) { manager.addShape(shape); refresh(CanvasBox); }
    @Override
    public void removeShape(Shape shape) { manager.removeShape(shape); refresh(CanvasBox); }
    @Override
    public void updateShape(Shape oldShape, Shape newShape) { manager.updateShape(oldShape, newShape); refresh(CanvasBox); }
    @Override
    public Shape[] getShapes() { return manager.getShapes().toArray(new Shape[0]); }
    @Override
    public void undo() { commandManager.undo(); refresh(CanvasBox); }
    @Override
    public void redo() { commandManager.redo(); refresh(CanvasBox); }

    @Override
    public void refresh(Object canvas) {
        canvasManager.redraw(manager.getShapes());
        updateShapeList(); //  تحديث القائمة بعد كل عملية
    }
}
