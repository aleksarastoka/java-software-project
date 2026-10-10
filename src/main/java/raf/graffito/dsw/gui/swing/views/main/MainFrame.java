package raf.graffito.dsw.gui.swing.views.main;

import lombok.Getter;
import lombok.Setter;
import raf.graffito.dsw.controller.ActionManager;
import raf.graffito.dsw.controller.mediator.Colleague;
import raf.graffito.dsw.controller.mediator.ScreenMode;
import raf.graffito.dsw.controller.mediator.ScreenSizeMediator;
import raf.graffito.dsw.core.ApplicationFramework;
import raf.graffito.dsw.core.message.ISubscriber;
import raf.graffito.dsw.core.message.Message;
import raf.graffito.dsw.core.model.repository.composite.GraffNode;
import raf.graffito.dsw.core.model.repository.implementation.Project;
import raf.graffito.dsw.gui.swing.tree.GraffTreeImplementation;
import raf.graffito.dsw.gui.swing.tree.dragndrop.SlideReorderTransferHandler;
import raf.graffito.dsw.gui.swing.tree.model.GraffTreeItem;
import raf.graffito.dsw.gui.swing.views.editor.ProjectView;

import javax.swing.*;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static raf.graffito.dsw.core.model.decorator.Decorators.unwrap;

@Getter @Setter
public class MainFrame extends JFrame implements ISubscriber<Message>, Colleague {

    private static MainFrame instance;

    private ActionManager actionManager;
    private GraffTreeImplementation graffTree;

    private ProjectView projectView;
    private JSplitPane split;

    private ScreenSizeMediator screenSizeMediator;

    // store once (used for SMALL divider scaling)
    private int normalDividerLocation = 220;

    public static synchronized MainFrame getInstance() {
        if (instance == null) instance = new MainFrame();
        return instance;
    }

    private MainFrame() {
        initialize();
    }

    private void initialize() {
        Toolkit kit = Toolkit.getDefaultToolkit();
        Dimension screenSize = kit.getScreenSize();

        setSize(screenSize.width / 2, screenSize.height / 2);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Graffito");
        setLayout(new BorderLayout());

        actionManager = new ActionManager();

        screenSizeMediator = new ScreenSizeMediator(this);
        screenSizeMediator.setUiRoot(getContentPane());

        // ✅ MainFrame is a Colleague: divider/layout adjustments
        screenSizeMediator.register(this);

        setJMenuBar(new MyMenuBar(actionManager));
        add(new MyToolBar(actionManager, screenSizeMediator), BorderLayout.NORTH);

        graffTree = new GraffTreeImplementation(
                ApplicationFramework.getInstance().getGraffRepoImplementation()
        );

        JTree jTree = graffTree.generateTree(
                ApplicationFramework.getInstance().getGraffRepoImplementation().getWorkspace()
        );
        jTree.setDragEnabled(true);
        jTree.setDropMode(DropMode.INSERT);
        jTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        jTree.setTransferHandler(new SlideReorderTransferHandler(jTree));

        jTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 2) openProjectIfDoubleClicked(jTree, e.getPoint());
            }
        });

        addWindowStateListener(e -> screenSizeMediator.syncFromFrameState());

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                screenSizeMediator.syncFromFrameState();
            }
        });


        JScrollPane leftScroll = new JScrollPane(jTree);
        leftScroll.setMinimumSize(new Dimension(180, 0));
        leftScroll.setPreferredSize(new Dimension(220, 0));

        projectView = new ProjectView(actionManager);
        graffTree.addTreeChangeListener(projectView);

        split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftScroll, projectView);
        split.setDividerLocation(normalDividerLocation);
        split.setOneTouchExpandable(true);
        split.setResizeWeight(0.0);
        split.setContinuousLayout(true);
        split.setBorder(BorderFactory.createEmptyBorder());

        normalDividerLocation = split.getDividerLocation();

        add(split, BorderLayout.CENTER);
    }

    public ScreenSizeMediator getScreenSizeMediator() {
        return screenSizeMediator;
    }

    @Override
    public void update(Message notification) {
        if (notification.getType().equals(raf.graffito.dsw.core.message.Type.ERROR) ||
                notification.getType().equals(raf.graffito.dsw.core.message.Type.WARNING)) {

            JOptionPane.showMessageDialog(
                    this,
                    notification.getContent(),
                    notification.getType().toString(),
                    notification.getType().equals(raf.graffito.dsw.core.message.Type.ERROR)
                            ? JOptionPane.ERROR_MESSAGE : JOptionPane.WARNING_MESSAGE
            );
        }
    }

    private void openProjectIfDoubleClicked(JTree tree, Point p) {
        TreePath path = tree.getPathForLocation(p.x, p.y);
        if (path == null) return;

        Object last = path.getLastPathComponent();
        if (!(last instanceof GraffTreeItem gti)) return;

        Object dom = gti.getGraffNode();
        if (!(dom instanceof GraffNode gn)) return;

        GraffNode core = unwrap(gn);
        if (!(core instanceof Project project)) return;

        projectView.showProject(project);
    }

    @Override
    public void onScreenModeChanged(ScreenMode mode, double uiScale) {
        if (split != null) {
            if (mode == ScreenMode.SMALL) split.setDividerLocation(Math.max(140, normalDividerLocation / 2));
            else split.setDividerLocation(normalDividerLocation);
        }
        revalidate();
        repaint();
    }
}
