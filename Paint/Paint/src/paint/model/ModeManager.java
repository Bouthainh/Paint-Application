package paint.model;

import java.util.ArrayList;
import java.util.List;

public class ModeManager implements iModeManager {
    
    private DrawingModeEnum currentMode = DrawingModeEnum.SHAPE;   
    private final List<iModeObserver> observers = new ArrayList<>();

    @Override
    public void setMode(DrawingModeEnum mode) {
        this.currentMode = mode;
        notifyObservers();
    }

    @Override
    public DrawingModeEnum getMode() {
        return currentMode;
    }

    @Override
    public void addObserver(iModeObserver o) {
        observers.add(o);
    }

   @Override
    public void removeObserver(iModeObserver o) {
        observers.remove(o);
    }

    @Override
    public void notifyObservers() {
        for (iModeObserver o : observers) {
            o.onModeUpdate(currentMode);
        }
    }
    
}
