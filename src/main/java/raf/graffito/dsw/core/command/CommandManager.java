package raf.graffito.dsw.core.command;

import lombok.Setter;

import java.util.ArrayDeque;
import java.util.Deque;

public class CommandManager {

    @Setter
    private Runnable onHistoryChanged;
    private final Deque<Command> undoStack = new ArrayDeque<>();
    private final Deque<Command> redoStack = new ArrayDeque<>();

    public void executeCommand(Command cmd) {
        if (cmd == null) return;

        cmd.execute();

        undoStack.push(cmd);
        redoStack.clear();

        fireHistoryChanged();   // ✅ after stacks updated
    }

    public void undo() {
        if (undoStack.isEmpty()) return;

        Command cmd = undoStack.pop();
        cmd.undo();
        redoStack.push(cmd);

        fireHistoryChanged();   // ✅ after stacks updated
    }

    public void redo() {
        if (redoStack.isEmpty()) return;

        Command cmd = redoStack.pop();
        cmd.execute();
        undoStack.push(cmd);

        fireHistoryChanged();   // ✅ after stacks updated
    }


    private void fireHistoryChanged() {
        if (onHistoryChanged != null) onHistoryChanged.run();
    }

    public boolean canUndo(){
        return !undoStack.isEmpty();
    }

    public boolean canRedo(){
        return !redoStack.isEmpty();
    }

    public void clear() {
        undoStack.clear();
        redoStack.clear();
    }
}
