package raf.graffito.dsw.core.command.slide;

import raf.graffito.dsw.core.command.Command;
import raf.graffito.dsw.core.model.structures.TextBoxElement;

public class EditTextCommand implements Command {

    private final TextBoxElement element;
    private final String oldText;
    private final String newText;

    public EditTextCommand(TextBoxElement element, String oldText, String newText) {
        this.element = element;
        this.oldText = (oldText == null) ? "" : oldText;
        this.newText = (newText == null) ? "" : newText;
    }

    @Override
    public void execute() {
        if (element != null) element.setText(newText);
    }

    @Override
    public void undo() {
        if (element != null) element.setText(oldText);
    }
}
