package me.ericballard.dijwebmaker.gui.handlers.button;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import me.ericballard.dijwebmaker.gui.Controller;
import me.ericballard.dijwebmaker.gui.handlers.node.NodeHandler;
import me.ericballard.dijwebmaker.gui.handlers.node.PathHandler;
import me.ericballard.dijwebmaker.web.Web;
import me.ericballard.dijwebmaker.gui.handlers.node.PathSimulator;
import me.ericballard.dijwebmaker.web.data.Data;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class ButtonHandler {

    static String btnCss;

    public static void click(Controller controller, boolean export) {
        if (export) {
            if (!PathSimulator.finding)
                // Export nodes and paths to be, flat-file optimized, web nodes after configuring neighbor nodes and distances
                Web.export(controller);
            return;
        }

        // Activate path finding simulator
        if (PathSimulator.finding) {
            // De-active
            controller.simBtn.setStyle(btnCss);
            controller.simBtn.setText("Simulate Path-Finder");

            PathSimulator.reset(controller);
            PathSimulator.finding = false;
            return;
        }

        if (PathHandler.sceneLines.isEmpty()) {
            // No paths to simulate with
            System.out.println("No paths exist to simulate.");
            return;
        }

        PathSimulator.finding = true;
        btnCss = controller.simBtn.getStyle();
        controller.simBtn.setStyle("-fx-background-color: #757575;");
        controller.simBtn.setText("Cancel");
    }

    public static void selectImage(Controller controller) {
        FileChooser fileChooser = new FileChooser();

        //Set extension filter
        FileChooser.ExtensionFilter extFilterJPG = new FileChooser.ExtensionFilter("JPG files (*.jpg)", "*.JPG");
        FileChooser.ExtensionFilter extFilterPNG = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.PNG");
        fileChooser.getExtensionFilters().addAll(extFilterJPG, extFilterPNG);

        //Show open file dialog
        File file = fileChooser.showOpenDialog(null);

        if (file == null) {
            System.out.print("Failed to get selected image.");
            return;
        }

        if (!NodeHandler.sceneNodes.isEmpty()) {
            // Ask for confirmation to clear current web
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.initStyle(StageStyle.UTILITY);
            alert.initModality(Modality.NONE);

            alert.setHeaderText("THIS WILL CLEAR ALL NODES AND PATHS!");
            alert.setContentText("Do you want to continue?");

            Optional<ButtonType> result = alert.showAndWait();

            if (!result.isPresent() || result.get() != ButtonType.OK) {
                System.out.print("Canceled setting new background!");
                return;
            }

            NodeHandler.sceneNodes.clear();
            PathHandler.sceneLines.clear();

            controller.nodeTxt.setText("Nodes: 0");
            controller.paintPane.getChildren().clear();
        }

        // Apply image to scene
        Image image = new Image(file.toURI().toString());
        Data.setBackgroundImage(controller, image);

        // Cache image for re-use
        String name = file.getName();
        Optional<String> fileFormat = Data.getExtension(name);

        if (!fileFormat.isPresent()) {
            System.out.println("Failed to cache selected image: " + name);
            return;
        }

        BufferedImage bImage = SwingFXUtils.fromFXImage(image, null);
        String format = fileFormat.get();

        try {
            ImageIO.write(bImage, format, new File(Data.path + "image." + format));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
