package paint.model.commands;

/**
 * Command interface for the Command Pattern.
 * Each concrete command implements an action (execute)
 * and how to revert it (undo).
 *
 * Undo/Redo management is handled by CommandManager,
 * NOT inside the command itself.
 */
public interface Command {
    void execute();
    void undo();
}
