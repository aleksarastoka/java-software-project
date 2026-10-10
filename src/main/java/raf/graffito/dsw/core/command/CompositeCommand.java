package raf.graffito.dsw.core.command;

import java.util.ArrayList;
import java.util.List;

public class CompositeCommand implements Command {

    private final String name;
    private final List<Command> commands = new ArrayList<>();

    public CompositeCommand(String name) {
        this.name = name;
    }

    public CompositeCommand add(Command c) {
        if (c != null) commands.add(c);
        return this;
    }

    public boolean isEmpty() {
        return commands.isEmpty();
    }

    @Override
    public void execute() {
        for (Command c : commands) c.execute();
    }

    @Override
    public void undo() {
        for (int i = commands.size() - 1; i >= 0; i--) {
            commands.get(i).undo();
        }
    }

    @Override
    public String name() {
        return name;
    }
}
