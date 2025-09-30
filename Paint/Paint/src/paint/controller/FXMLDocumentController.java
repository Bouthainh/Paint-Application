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

public class FXMLDocumentController implements Initializable, DrawingEngine {

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

    /*** CLASS VARIABLES ***/
    private Point2D start;
    private Point2D end;
    private ShapeManager manager = ShapeManager.getInstance();
    private CanvasManager canvasManager = CanvasManager.getInstance();

    private boolean move = false;
    private boolean copy = false;
    private boolean resize = false;
    private boolean save = false;
    private boolean load = false;
    private boolean importt = false;

    // Undo/Redo stacks
    private Stack<ArrayList<Shape>> primary = new Stack<>();
    private Stack<ArrayList<Shape>> secondary = new Stack<>();

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
                removeShape(manager.getShapes().get(index));
            } else {
                Message.setText("You need to pick a shape first to delete it.");
            }
        }

        if(event.getSource() == RecolorBtn){
            if(!ShapeList.getSelectionModel().isEmpty()){
                int index = ShapeList.getSelectionModel().getSelectedIndex();
                manager.getShapes().get(index).setFillColor(ColorBox.getValue());
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
            undo();
        }

        if(event.getSource() == RedoBtn){
            if(secondary.isEmpty()){
                Message.setText("There is no more history for me to get .. Go search history books.");
                return;
            }
            redo();
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
        int index = ShapeList.getSelectionModel().getSelectedIndex();
        manager.getShapes().get(index).setTopLeft(start);
        refresh(CanvasBox);
    }

    public void copyFunction() throws CloneNotSupportedException{
        int index = ShapeList.getSelectionModel().getSelectedIndex();
        Shape temp = manager.getShapes().get(index).cloneShape();
        if(temp == null){ System.out.println("Error cloning failed!"); }
        else {
            manager.addShape(temp);
            manager.getShapes().get(manager.getShapes().size() - 1).setTopLeft(start);
            refresh(CanvasBox);
        }
    }

    public void resizeFunction(){
        int index = ShapeList.getSelectionModel().getSelectedIndex();
        Color c = manager.getShapes().get(index).getFillColor();
        start = manager.getShapes().get(index).getTopLeft();

        Shape temp = new ShapeFactory().createShape(manager.getShapes().get(index).getClass().getSimpleName(), start, end, ColorBox.getValue());
        if(temp.getClass().getSimpleName().equals("Line")){ Message.setText("Line doesn't support this command. Sorry :("); return; }

        manager.removeShape(manager.getShapes().get(index));
        temp.setFillColor(c);
        manager.addShape(temp);
        refresh(CanvasBox);
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

    // Observer DP
    public ObservableList getStringList(){
        ObservableList l = FXCollections.observableArrayList();
        for(int i = 0; i < manager.getShapes().size(); i++){
            String temp = manager.getShapes().get(i).getClass().getSimpleName() + "  (" +
                          (int) manager.getShapes().get(i).getTopLeft().getX() + "," +
                          (int) manager.getShapes().get(i).getTopLeft().getY() + ")";
            l.add(temp);
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
}
