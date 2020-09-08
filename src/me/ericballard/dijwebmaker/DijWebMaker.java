package me.ericballard.dijwebmaker;

import javafx.animation.RotateTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;
import javafx.util.Duration;
import me.ericballard.dijwebmaker.gui.handlers.node.NodeHandler;
import me.ericballard.dijwebmaker.web.data.Data;
import me.ericballard.dijwebmaker.gui.Controller;
import me.ericballard.dijwebmaker.gui.handlers.node.PathHandler;
import me.ericballard.dijwebmaker.gui.handlers.node.PathSimulator;

import java.io.IOException;

import static java.lang.Thread.sleep;


public class DijWebMaker extends Application {

    // Cached instance of UI controller
    Controller controller;

    // Start application
    public static void main(String[] args) {
        launch(args);
        Data.save();
    }

    @Override
    public void start(Stage stage) {
        // Load fxml file to initialize UI
        FXMLLoader loader = new FXMLLoader(getClass().getResource("gui/resources/Interface.fxml"));
        loader.setController((controller = new Controller()));
        Parent root;

        try {
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // Set icon and titile
        stage.getIcons().add(new Image(getClass().getResourceAsStream("gui/resources/icon.png")));
        stage.setTitle("DijWebMaker | v1.0");

        // Configure window functionality
        stage.setScene(new Scene(root, 1200, 800));
        stage.setResizable(true);
        stage.show();

        // Register resize listeners for scene
        controller.mainGrid.getScene().heightProperty().addListener(e -> controller.resize());
        controller.mainGrid.getScene().widthProperty().addListener(e -> controller.resize());

        // Create a new thread to handle updating visual effects
        new Thread(() -> {
            while (stage.isShowing()) {
                // Color and rotate nodes
                Platform.runLater(() -> {
                    if (!NodeHandler.sceneNodes.isEmpty()) {
                        // Determine color to flash
                        boolean simulatingPath = PathSimulator.finding;
                        Color prev = (Color) NodeHandler.sceneNodes.get(0).getFill();

                        if (simulatingPath) {
                            if (!PathSimulator.nodes.isEmpty())
                                prev = (Color) PathSimulator.nodes.get(0).getFill();
                        }

                        Color neutral = (simulatingPath ? Color.GOLD : Color.BLACK);
                        Color color = (prev == neutral ? Color.RED : prev == Color.RED ? Color.PURPLE : neutral);
                        NodeHandler.flashColor = color;

                        // Apply color to fx nodes
                        if (simulatingPath) {
                            PathSimulator.paths.forEach(path -> path.setStroke(color));
                        } else {
                            if (PathHandler.selectedLine != null)
                                PathHandler.selectedLine.setStroke(color);
                            else if (PathHandler.hoveredLine != null)
                                PathHandler.hoveredLine.setStroke(color);
                        }

                        NodeHandler.sceneNodes.forEach(shape -> {
                            if (simulatingPath) {
                                boolean isPartOfSim = false;
                                for (Shape node : PathSimulator.nodes) {
                                    if (node.getTranslateX() == shape.getTranslateX() && node.getTranslateY() == shape.getTranslateY()) {
                                        isPartOfSim = true;
                                        break;
                                    }
                                }
                                if (!isPartOfSim) {
                                    shape.setFill(Color.BLACK);
                                    return;
                                }
                            }

                            shape.setFill(color);
                            RotateTransition rt = new RotateTransition(Duration.millis(800), shape);
                            rt.setByAngle(360);
                            rt.setCycleCount(1);
                            rt.play();
                        });
                    }
                });

                try {
                    sleep(1000);
                } catch (Exception e) {
                    break;
                }
            }
        }).start();
    }
}
