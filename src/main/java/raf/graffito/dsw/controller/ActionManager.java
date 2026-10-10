package raf.graffito.dsw.controller;
import lombok.Getter;
import lombok.Setter;
import raf.graffito.dsw.controller.actions.commandActions.UndoAction;
import raf.graffito.dsw.controller.actions.serializerActions.*;
import raf.graffito.dsw.controller.actions.commandActions.RedoAction;
import raf.graffito.dsw.controller.actions.stateAction.*;
import raf.graffito.dsw.controller.actions.treeActions.AddElementAction;
import raf.graffito.dsw.controller.actions.treeActions.DeleteElementAction;
import raf.graffito.dsw.controller.actions.treeActions.EditProjectAction;
import raf.graffito.dsw.controller.actions.uiActions.AboutUsAction;
import raf.graffito.dsw.controller.actions.uiActions.ExitAction;

/*
    Action manager klasa u kojoj cuvamo svaki Action u potreban za program
*/
@Getter@Setter
public class ActionManager {
    private final ExitAction exitAction;
    private final AboutUsAction aboutUsAction;
    private final AddElementAction addElementAction;
    private final EditProjectAction editProjectAction;
    private final DeleteElementAction deleteElementAction;
    private final SelectToolAction selectToolAction;
    private final MoveToolAction moveToolAction;
    private final ResizeToolAction resizeToolAction;
    private final RotateToolAction rotateToolAction;
    private final AddTextToolAction addTextToolAction;
    private final AddLogoToolAction addLogoToolAction;
    private final AddImageToolAction addImageToolAction;
    private final DeleteToolAction deleteToolAction;
    private final UndoAction undoAction;
    private final RedoAction redoAction;
    private final ZoomToolAction zoomToolAction;
    private final CloneToolAction cloneToolAction;
    private final SaveAsProjectAction saveAsProjectAction;
    private final LoadProjectAction loadProjectAction;
    private final SaveProjectAction saveProjectAction;
    private final SaveAsTemplateAction saveAsTemplateAction;
    private final LoadImagesAction loadImagesAction;


    public ActionManager() {
        this.aboutUsAction = new AboutUsAction();
        this.exitAction = new ExitAction();
        this.addElementAction = new AddElementAction();
        this.editProjectAction = new EditProjectAction();
        this.deleteElementAction = new DeleteElementAction();
        this.selectToolAction = new SelectToolAction();
        this.moveToolAction = new MoveToolAction();
        this.resizeToolAction = new ResizeToolAction();
        this.rotateToolAction = new RotateToolAction();
        this.addTextToolAction = new AddTextToolAction();
        this.addLogoToolAction = new AddLogoToolAction();
        this.addImageToolAction = new AddImageToolAction();
        this.deleteToolAction = new DeleteToolAction();
        this.undoAction = new UndoAction();
        this.redoAction = new RedoAction();
        this.zoomToolAction = new ZoomToolAction();
        this.cloneToolAction = new CloneToolAction();
        this.saveAsProjectAction = new SaveAsProjectAction();
        this.loadProjectAction = new LoadProjectAction();
        this.saveProjectAction = new SaveProjectAction();
        this.saveAsTemplateAction = new SaveAsTemplateAction();
        this.loadImagesAction = new LoadImagesAction();
    }
}
