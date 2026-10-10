package raf.graffito.dsw.controller.actions.serializerActions;

import raf.graffito.dsw.controller.actions.AbstractGraffAction;
import raf.graffito.dsw.core.model.images.ImageSource;
import raf.graffito.dsw.core.model.images.PathImageProxy;
import raf.graffito.dsw.gui.swing.views.main.MainFrame;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static raf.graffito.dsw.gui.swing.views.sidebar.ImagePalettePanel.thumbH;
import static raf.graffito.dsw.gui.swing.views.sidebar.ImagePalettePanel.thumbW;

public class LoadImagesAction extends AbstractGraffAction {

    public LoadImagesAction() {
        putValue(NAME, "Load Images");
        putValue(SHORT_DESCRIPTION, "Load images into the right palette");
        putValue(SMALL_ICON, loadIcon("/images/loadImageIcon.png"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Load Images");
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fc.setMultiSelectionEnabled(true);
        fc.setAcceptAllFileFilterUsed(true);
        fc.setFileFilter(new FileNameExtensionFilter("Images (*.png, *.jpg, *.jpeg)", "png", "jpg", "jpeg"));

        int choice = fc.showOpenDialog(MainFrame.getInstance());
        if (choice != JFileChooser.APPROVE_OPTION) return;

        File[] files = fc.getSelectedFiles();
        if (files == null || files.length == 0) return;

        List<ImageSource> loaded = new ArrayList<>();
        List<String> failed = new ArrayList<>();

        for (File f : files) {
            try {
                // ✅ proxy: only thumb now, full loads later
                loaded.add(PathImageProxy.fromFile(f.toPath(), thumbW(), thumbH()));
            } catch (Exception ex) {
                failed.add(f.getName());
            }
        }

        if (!loaded.isEmpty()) {
            MainFrame.getInstance()
                    .getProjectView()
                    .getRightSidebarPanel()
                    .getImagePalettePanel()
                    .addImageSources(loaded);
        }

        if (!failed.isEmpty()) {
            JOptionPane.showMessageDialog(MainFrame.getInstance(),
                    "Some files could not be loaded:\n" + String.join("\n", failed),
                    "Load Images",
                    JOptionPane.WARNING_MESSAGE);
        }
    }
}
