package paint.model;

//Observer Pattern: Subject Interface
// Interface for managing drawing modes
public interface iModeManager {
    void setMode(DrawingModeEnum mode);
    DrawingModeEnum getMode();  
    void addObserver(iModeObserver o);
    void removeObserver(iModeObserver o);
    void notifyObservers();
   
}
