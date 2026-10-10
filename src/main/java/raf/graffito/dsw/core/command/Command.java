package raf.graffito.dsw.core.command;

public interface Command {
    void execute();
    void undo();

    // Optional but super useful for debugging / UI labels
    default String name() { return getClass().getSimpleName(); }
}
