package raf.graffito.dsw.core;

import lombok.Getter;
import raf.graffito.dsw.core.serializer.ProjectJsonSerializer;
import raf.graffito.dsw.core.serializer.TemplateJsonSerializer;
import raf.graffito.dsw.core.state.ToolStateManager;
import raf.graffito.dsw.core.state.ToolType;
import raf.graffito.dsw.core.state.tools.*;
import raf.graffito.dsw.core.message.MessageGenerator;
import raf.graffito.dsw.core.model.logger.Logger;
import raf.graffito.dsw.core.model.logger.LoggerFactory;
import raf.graffito.dsw.core.model.repository.GraffRepoImplementation;
import raf.graffito.dsw.core.strategy.MatrixOccupancyStrategy;
import raf.graffito.dsw.core.strategy.SlideSpacePolicy;
import raf.graffito.dsw.gui.swing.views.main.MainFrame;


@Getter
public class ApplicationFramework {
    /// bootstrap klasa koja inicijalizuje i povezuje sve ključne delove sistema
    private static ApplicationFramework instance;

    protected GraffRepoImplementation graffRepoImplementation;
    private MessageGenerator mg;

    // ✅ Tool state pattern manager (single source of truth)
    private ToolStateManager toolStateManager;
    private TemplateJsonSerializer templateJsonSerializer;
    private ProjectJsonSerializer projectJsonSerializer;
    private final SlideSpacePolicy slideSpacePolicy =
            new SlideSpacePolicy(new MatrixOccupancyStrategy());

    public static ApplicationFramework getInstance() {
        if (instance == null)
            instance = new ApplicationFramework();
        return instance;
    }

    private ApplicationFramework() {
        instance = this;
        initialize();
    }

    public void initialize() {
        graffRepoImplementation = new GraffRepoImplementation();
        templateJsonSerializer = new TemplateJsonSerializer();
        projectJsonSerializer = new ProjectJsonSerializer();

        mg = new MessageGenerator();

        LoggerFactory lf = new LoggerFactory();
        Logger log1 = lf.createLogger("console");
        Logger log2 = lf.createLogger("file");
        mg.addSubscriber(log1);
        mg.addSubscriber(log2);

        // ✅ init tool manager once
        toolStateManager = new ToolStateManager();

        toolStateManager.register(ToolType.SELECT, new SelectToolState());
        toolStateManager.register(ToolType.MOVE, new MoveToolState());
        toolStateManager.register(ToolType.ADD_TEXT, new AddTextToolState());
        toolStateManager.register(ToolType.RESIZE, new ResizeToolState());
        toolStateManager.register(ToolType.MOVE, new MoveToolState());
        toolStateManager.register(ToolType.ROTATE, new RotateToolState());
        toolStateManager.register(ToolType.DELETE, new DeleteToolState());
        toolStateManager.register(ToolType.ADD_IMAGE, new AddImageToolState());
        toolStateManager.register(ToolType.ADD_LOGO, new AddLogoToolState());
        toolStateManager.register(ToolType.ZOOM, new ZoomToolState());
        toolStateManager.register(ToolType.CLONE, new CloneToolState());
        toolStateManager.setActiveTool(ToolType.SELECT);



        MainFrame mainFrame = MainFrame.getInstance();
        mainFrame.setVisible(true);
        mg.addSubscriber(mainFrame);
    }
}
