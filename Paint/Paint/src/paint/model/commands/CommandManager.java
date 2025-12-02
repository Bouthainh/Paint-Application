package paint.model.commands;

import java.util.Stack;

public class CommandManager {

    private static CommandManager instance = new CommandManager();

    private Stack<Command> undoStack = new Stack<>();
    private Stack<Command> redoStack = new Stack<>();

    public static CommandManager getInstance() {
        return instance;
    }

    // Execute a new command
    public void executeCommand(Command command) {
        command.execute();
        undoStack.push(command);
        redoStack.clear();
    }

    // Undo last action
    public void undo() {
        if (!undoStack.isEmpty()) {
            Command c = undoStack.pop();
            c.undo();
            redoStack.push(c);
        }
    }

    // Redo last undone action
    public void redo() {
        if (!redoStack.isEmpty()) {
            Command c = redoStack.pop();
            c.execute();
            undoStack.push(c);
        }
    }

    public boolean canUndo() { return !undoStack.isEmpty(); }
    public boolean canRedo() { return !redoStack.isEmpty(); }
}
