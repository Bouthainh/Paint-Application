package paint.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import paint.model.*;
import javafx.scene.web.WebView; //Adapter: import WebView bridge
import paint.model.commands.*;   // Command Pattern



public class FXMLDocumentController implements Initializable, DrawingEngine, iModeObserver {

    /*** FXML VARIABLES ***/
    @FXML private Button DeleteBtn;
    @FXML private ComboBox<String> ShapeBox;
    @FXML private Button UndoBtn;
    @FXML private Button RedoBtn;
    @FXML private ColorPicker ColorBox;
    @FXML private Button SaveBtn;
    @FXML private Button MoveBtn;
    @FXML private Button RecolorBtn;
    @FXML private Button LoadBtn;
    @FXML private Button ImportBtn;
    @FXML private GridPane After;
    @FXML private Pane Before;
    @FXML private Pane PathPane;
    @FXML private TextField PathText;
    @FXML private Button StartBtn;
    @FXML private Button ResizeBtn;
    @FXML private Button PathBtn;
    @FXML private Canvas CanvasBox;
    @FXML private Button CopyBtn;
    @FXML private Label Message;
    @FXML private ListView ShapeList;
    //Group/Ungroup features Buttons
    @FXML private Button GroupBtn;
    @FXML private Button UngroupBtn;
    // Pen Tool FXML Components => UI elements
    @FXML private MenuButton PenMenu;
    @FXML private Slider PenSizeSlider;
    @FXML private Slider EraserSizeSlider;
    @FXML private Button UsePenBtn;
    @FXML private Button UseEraserBtn;
    @FXML private WebView PenWebView; //the WebView that hosts the pen tool implemented in JS

    //Pen Tool Variables
    private boolean penMode = false;
    private iPenDrawing pen; //interface reference for Pen, loose coupling

    //CLASS VARIABLES 
    private Point2D start;
    private Point2D end;
    private ShapeManager manager = ShapeManager.getInstance();
    private CanvasManager canvasManager = CanvasManager.getInstance();

    //Observer Pattern: Mode Manager
    private ModeManager modeManager = new ModeManager();

    // Action flags
    private boolean move = false;
    private boolean copy = false;
    private boolean resize = false;
    private boolean save = false;
    private boolean load = false;
    private boolean importt = false;

    // Undo/Redo stacks
    private Stack<ArrayList<Shape>> primary = new Stack<>();
    private Stack<ArrayList<Shape>> secondary = new Stack<>();

    // Command Pattern Manager
    private CommandManager commandManager = CommandManager.getInstance();

    // **Helper Methods for Composite Pattern**
      private boolean isShapeGroup(Shape shape) {
          return shape instanceof ShapeGroup;
     }

     private ShapeGroup getShapeGroup(Shape shape) {
         return (ShapeGroup) shape;
      }

     @FXML
      private void handleButtonAction(ActionEvent event) throws CloneNotSupportedException {
           if(event.getSource() == StartBtn){
            Before.setVisible(false);
            After.setVisible(true);
           }

             Message.setText("");

         if(event.getSource() == DeleteBtn){
            if(!ShapeList.getSelectionModel().isEmpty()){
                int index = ShapeList.getSelectionModel().getSelectedIndex();
                Shape target = manager.getShapes().get(index);
                commandManager.executeCommand(new DeleteShapeCommand(manager, target));
                refresh(CanvasBox);

            } else {
                Message.setText("You need to pick a shape first to delete it.");
            }
        }

        if(event.getSource() == RecolorBtn){
            if(!ShapeList.getSelectionModel().isEmpty()){
                int index = ShapeList.getSelectionModel().getSelectedIndex();
Shape s = manager.getShapes().get(index);
Color oldColor = s.getFillColor();
Color newColor = ColorBox.getValue();

commandManager.executeCommand(new RecolorCommand(s, oldColor, newColor));
refresh(CanvasBox);

            } else {
                Message.setText("You need to pick a shape first to recolor it.");
            }
        }

        if(event.getSource() == MoveBtn){
            if(!ShapeList.getSelectionModel().isEmpty()){
                move = true;
                Message.setText("Click on the new top-left position below to move the selected shape.");
            } else {
                Message.setText("You need to pick a shape first to move it.");
            }
        }

        if(event.getSource() == CopyBtn){
            if(!ShapeList.getSelectionModel().isEmpty()){
                copy = true;
                Message.setText("Click on the new top-left position below to copy the selected shape.");
            } else {
                Message.setText("You need to pick a shape first to copy it.");
            }
        }

        if(event.getSource() == ResizeBtn){
            if(!ShapeList.getSelectionModel().isEmpty()){
                resize = true;
                Message.setText("Click on the new right-button position below to resize the selected shape.");
            } else {
                Message.setText("You need to pick a shape first to resize it.");
            }
        }

        if(event.getSource() == UndoBtn){
            if(primary.isEmpty()){
                Message.setText("We are back to zero point! .. Can Undo nothing more!");
                return;
            }
commandManager.undo();
refresh(CanvasBox);
        }

        if(event.getSource() == RedoBtn){
            if(secondary.isEmpty()){
                Message.setText("There is no more history for me to get .. Go search history books.");
                return;
            }
commandManager.redo();
refresh(CanvasBox);
        }

        if(event.getSource() == SaveBtn){
            showPathPane();
            save = true;
        }

        if(event.getSource() == LoadBtn){
            showPathPane();
            load = true;
        }

        if(event.getSource() == ImportBtn){
            showPathPane();
            importt = true;
        }

        if(event.getSource() == PathBtn){
            if(PathText.getText().isEmpty()){ PathText.setText("You need to set the path of the file."); return; }
            if(save){ save = false; save(PathText.getText()); }
            else if(load){ load = false; load(PathText.getText()); }
            else if(importt){ importt = false; installPluginShape(PathText.getText()); }
            hidePathPane();
        }


 // **Composite Design Pattern support**
     if (event.getSource() == GroupBtn) {
     var selectedItems = ShapeList.getSelectionModel().getSelectedIndices();

     if (selectedItems.isEmpty() || selectedItems.size() < 2) {
        Message.setText("Please select at least two shapes to group.");
        return;
      }

 //create new groups
    ShapeGroup group = new ShapeGroup("Group_" + (manager.getShapes().size() + 1));

    ArrayList<Shape> toRemove = new ArrayList<>();
    for (Object indexObj : selectedItems) {
        int index = (int) indexObj;
        Shape s = manager.getShapes().get(index);
        group.addShape(s);
        toRemove.add(s);
    }

    //remove single shapes
    for (Shape s : toRemove) {
        manager.removeShape(s);
    }

    //add the group
    manager.addShape(group);
    Message.setText("Shapes grouped successfully!");
    refresh(CanvasBox);
}


    // group recolorBtn:
    if(event.getSource() == RecolorBtn){
    if(!ShapeList.getSelectionModel().isEmpty()){
        int index = ShapeList.getSelectionModel().getSelectedIndex();
        Shape selectedShape = manager.getShapes().get(index);
        selectedShape.setFillColor(ColorBox.getValue());
        refresh(CanvasBox);
    } else {
        Message.setText("You need to pick a shape first to recolor it.");
    }
}
if(event.getSource() == UngroupBtn){
    if(!ShapeList.getSelectionModel().isEmpty()){
        int index = ShapeList.getSelectionModel().getSelectedIndex();
        Shape selectedShape = manager.getShapes().get(index);
        
        if(isShapeGroup(selectedShape)){
            ShapeGroup group = getShapeGroup(selectedShape);
            

            //save shapes, before ungroup
            ArrayList<Shape> shapesToAdd = new ArrayList<>();
            for(Shape shape : group.getChildren()){
                shapesToAdd.add(shape);
            }
            
            //ungroup
            manager.removeShape(group);
            
            //add the single shapes
            for(Shape shape : shapesToAdd){
                manager.addShape(shape);
            }
            
            Message.setText("Group ungrouped successfully!");
            refresh(CanvasBox);
        } else {
            Message.setText("Selected shape is not a group.");
        }
    } else {
        Message.setText("Please select a group to ungroup.");
    }
}

    }

    public void showPathPane(){
        Message.setVisible(false);
        PathPane.setVisible(true);
    }

    public void hidePathPane(){
        PathPane.setVisible(false);
        Message.setVisible(true);
    }

    public void startDrag(MouseEvent event){
        start = new Point2D(event.getX(), event.getY());
        Message.setText("");
    }

    public void endDrag(MouseEvent event) throws CloneNotSupportedException{
        end = new Point2D(event.getX(), event.getY());
        if(end.equals(start)){ clickFunction(); } else { dragFunction(); }
    }

    public void clickFunction() throws CloneNotSupportedException{
        if(move){ move = false; moveFunction(); }
        else if(copy){ copy = false; copyFunction(); }
        else if(resize){ resize = false; resizeFunction(); }
    }
public void moveFunction(){
    executeMoveCommand();
    return;
}

public void copyFunction() throws CloneNotSupportedException{
    int index = ShapeList.getSelectionModel().getSelectedIndex();
    Shape selectedShape = manager.getShapes().get(index);
    Shape temp = selectedShape.cloneShape();
    
    if(temp == null){ 
        System.out.println("Error cloning failed!"); 
    } else {
        manager.addShape(temp);
        
        if (isShapeGroup(temp)) {
            ShapeGroup group = getShapeGroup(temp);
            group.setTopLeft(start);
            // update the name in the side list, after grouping
            group.setName(group.getName() + "_Copy");
        } else {
            temp.setTopLeft(start);
        }
        refresh(CanvasBox);
    }
}

public void resizeFunction(){
    executeResizeCommand();
return;

}

    public void dragFunction() throws CloneNotSupportedException{
        String type = ShapeBox.getValue();
        Shape sh;
        try{
            sh = new ShapeFactory().createShape(type, start, end, ColorBox.getValue());
        } catch(Exception e){
            Message.setText("Don't be in a hurry! Choose a shape first :'D");
            return;
        }
        addShape(sh);
        sh.draw(CanvasBox);
    }
        
// **Composite Design Pattern support**
   public ObservableList getStringList(){
    ObservableList l = FXCollections.observableArrayList();
    for(int i = 0; i < manager.getShapes().size(); i++){
        Shape shape = manager.getShapes().get(i);
        if(shape instanceof ShapeGroup){
            ShapeGroup group = (ShapeGroup) shape;
            String temp = "Group (" + group.getChildren().size() + " shapes)  (" +
                         (int) shape.getTopLeft().getX() + "," +
                         (int) shape.getTopLeft().getY() + ")";
            l.add(temp);
        } else {
            String temp = shape.getClass().getSimpleName() + "  (" +
                         (int) shape.getTopLeft().getX() + "," +
                         (int) shape.getTopLeft().getY() + ")";
            l.add(temp);
        }
    }
    return l;
}

    public ArrayList<Shape> cloneList(List<Shape> l) throws CloneNotSupportedException{
        ArrayList<Shape> temp = new ArrayList<>();
        for(Shape s : l){
            temp.add(s.cloneShape());
        }
        return temp;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        ObservableList<String> shapeList = FXCollections.observableArrayList("Circle","Ellipse","Rectangle","Square","Triangle","Line");
        ShapeBox.setItems(shapeList);
        ColorBox.setValue(Color.BLACK);
        canvasManager.setCanvas(CanvasBox);

        // **Composite Design Pattern support**
         ShapeList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

         // **Pen Adapter setup**
         setupPenTool();

         // **Observer Pattern: register as observer**
         modeManager.addObserver(this);

    }

    //** Pen Tool Adapter Setup**
    private void setupPenTool() {
    
    //loading the external system via the host "webview"
     PenWebView.getEngine().load(
        getClass().getResource("/paint/ExternalSystem/index.html").toExternalForm()
     );
     PenWebView.setVisible(false);
     PenWebView.setMouseTransparent(true); 
     PenWebView.getEngine().getLoadWorker().stateProperty().addListener((obs, old, state) -> {
        if (state == javafx.concurrent.Worker.State.SUCCEEDED) {

            // Create adapter obj using the loaded WebView
            pen = new PenAdapter(PenWebView);

            // bined with UI values (from silders & colorbox):
            pen.setPenSize(PenSizeSlider.getValue());
            pen.setEraserSize(EraserSizeSlider.getValue());
            pen.setColor(ColorBox.getValue());

            // Sliders & color UI Elements bindings:
            PenSizeSlider.valueProperty().addListener((o, ov, nv) -> pen.setPenSize(nv.doubleValue()));
            EraserSizeSlider.valueProperty().addListener((o, ov, nv) -> pen.setEraserSize(nv.doubleValue()));
            ColorBox.valueProperty().addListener((o, ov, nv) -> pen.setColor(nv));

            // Pen button:
       UsePenBtn.setOnAction(e -> {
       pen.usePen();
       modeManager.setMode(DrawingModeEnum.PEN);
     });
       // Eraser button:
       UseEraserBtn.setOnAction(e -> {
       pen.useEraser();
      modeManager.setMode(DrawingModeEnum.ERASER);
     });

      // Shape mode selection from ComboBox:
      ShapeBox.setOnAction(e -> {
      modeManager.setMode(DrawingModeEnum.SHAPE);
      });
        }
    });


    //These event handlers control for the CanvasBox layer
    //prevent canvabox ineraction when the  pen is active

       CanvasBox.setOnMousePressed(e -> {
        if (penMode) return;  
        startDrag(e);
    });

        CanvasBox.setOnMouseDragged(e -> {
        if (penMode) return;  
    });

         CanvasBox.setOnMouseReleased(e -> {
        if (penMode) return;  
            try { endDrag(e); } catch (CloneNotSupportedException ex) { ex.printStackTrace(); }

    });
}

    // **Observer Pattern: update mode on change**
    @Override
    public void onModeUpdate(DrawingModeEnum newMode) {

    switch (newMode) {

        case PEN:
            penMode = true;
            PenWebView.setVisible(true);
            PenWebView.setMouseTransparent(false);
            PenWebView.toFront();
            Message.setText("Pen mode: draw freely.");
            System.out.println("ModeObserver: Pen mode activated");
            break;

        case ERASER:
            penMode = true;
            PenWebView.setVisible(true);
            PenWebView.setMouseTransparent(false);
            PenWebView.toFront();
            Message.setText("Eraser mode.");
            System.out.println("ModeObserver: Eraser mode activated");
            break;

        case SHAPE:
            penMode = false;
            PenWebView.setVisible(false);
            PenWebView.setMouseTransparent(true);
            CanvasBox.toFront();
            CanvasBox.requestFocus();
            System.out.println("ModeObserver: Shape mode activated");
            CanvasBox.setCursor(javafx.scene.Cursor.CROSSHAIR);
            Message.setText("Shape mode: " + ShapeBox.getValue());
            break;
    }
}



    // ================== CORE METHODS ===================
    @Override
    public void refresh(Object canvas) { refresh(canvas, true); }

    public void refresh(Object canvas, boolean saveHistory){
        if(saveHistory){
            try {
                primary.push(new ArrayList<>(cloneList(manager.getShapes())));
                secondary.clear();
            } catch (CloneNotSupportedException ex) {
                Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        canvasManager.redraw(manager.getShapes());
        ShapeList.setItems(getStringList());
    }

    @Override
    public void addShape(Shape shape){
        manager.addShape(shape);
        refresh(CanvasBox);
    }

    @Override
    public void removeShape(Shape shape){
        manager.removeShape(shape);
        refresh(CanvasBox);
    }

    @Override
    public void updateShape(Shape oldShape, Shape newShape){
        manager.removeShape(oldShape);
        manager.addShape(newShape);
        refresh(CanvasBox);
    }

    @Override
    public Shape[] getShapes(){
        return manager.getShapes().toArray(new Shape[0]);
    }

    // ================== UNDO / REDO ===================
    @Override
    public void undo(){
        if(primary.isEmpty()){ Message.setText("We are back to zero point! .. Can Undo nothing more!"); return; }
        ArrayList<Shape> temp = primary.pop();
        secondary.push(temp);

        if(primary.isEmpty()){ manager.setShapes(new ArrayList<>()); }
        else { manager.setShapes(primary.peek()); }

        canvasManager.redraw(manager.getShapes());
        ShapeList.setItems(getStringList());
    }

    @Override
    public void redo(){
        if(secondary.isEmpty()){ Message.setText("There is no more history for me to get .. Go search history books."); return; }
        ArrayList<Shape> temp = secondary.pop();
        primary.push(temp);

        manager.setShapes(temp);
        canvasManager.redraw(manager.getShapes());
        ShapeList.setItems(getStringList());
    }

    // ================== SAVE / LOAD ===================
    @Override
    public void save(String path){
     List<Shape> shapeList = manager.getShapes();
        if(path.endsWith(".xml")){
            SaveToXML x = new SaveToXML(path, shapeList);
            if(x.checkSuccess()){ Message.setText("File Saved Successfully"); }
            else{ Message.setText("Error happened while saving, please check the path and try again!"); }
        } else {
            Message.setText("Wrong file format .. save to either .xml or .json");
        }
    }

    @Override
    public void load(String path){
        if(path.endsWith(".xml")){
            try {
                LoadFromXML l = new LoadFromXML(path);
                if(l.checkSuccess()){
                    manager.setShapes(l.getList());
                    refresh(CanvasBox, false); // لا تحفظ حالة جديدة عند التحميل
                    Message.setText("File loaded successfully");
                } else {
                    Message.setText("Error loading the file .. check the file path and try again!");
                }
            } catch(SAXException | ParserConfigurationException | IOException ex){
                Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            Message.setText("Wrong file format .. load from either .xml or .json");
        }
    }

    @Override
    public List<Class<? extends Shape>> getSupportedShapes() { return null; }

    @Override
    public void installPluginShape(String jarPath){ Message.setText("Not supported yet."); }

    // Command Pattern MOVE
private void executeMoveCommand() {

    int index = ShapeList.getSelectionModel().getSelectedIndex();
    Shape selected = manager.getShapes().get(index);
    Point2D oldPos = selected.getTopLeft();

    commandManager.executeCommand(
        new MoveCommand(selected, oldPos, start)
    );

    refresh(CanvasBox);
}

private void executeResizeCommand(){

    int index = ShapeList.getSelectionModel().getSelectedIndex();
    Shape oldShape = manager.getShapes().get(index);

    if (isShapeGroup(oldShape)) {
        Message.setText("Cannot resize a group.");
        return;
    }

    Shape newShape = new ShapeFactory().createShape(
        oldShape.getClass().getSimpleName(),
        oldShape.getTopLeft(),
        end,
        ColorBox.getValue()
    );

    commandManager.executeCommand(
        new ResizeCommand(manager, oldShape, newShape)
    );

    refresh(CanvasBox);
}


}
