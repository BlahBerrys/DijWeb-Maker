package me.ericballard.dijwebmaker.web.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.scene.image.Image;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Shape;
import javafx.util.Pair;
import me.ericballard.dijwebmaker.gui.handlers.node.NodeHandler;
import me.ericballard.dijwebmaker.gui.Controller;
import me.ericballard.dijwebmaker.gui.handlers.node.PathHandler;

import javax.swing.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Data {

    public static final String path = new JFileChooser().getFileSystemView().getDefaultDirectory().toString() + File.separator + "DijWebMaker" + File.separator;

    public static Serialize.Graph graph;

    public static Optional<String> getExtension(String filename) {
        return Optional.ofNullable(filename)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(filename.lastIndexOf(".") + 1));
    }

    public static void setBackgroundImage(Controller controller, Image image) {
        System.out.println("Applying image to scene...");
        controller.img.setImage(image);

        double h = image.getHeight();
        double w = image.getWidth();

        controller.cordGrid.setMinSize(w, h);
        controller.cordGrid.setMaxSize(w, h);

        controller.paintPane.setMinSize(w, h);
        controller.paintPane.setMaxSize(w, h);
    }

    public static void save() {
        try {
            GsonBuilder gsonBuilder = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting();
            Gson gson = gsonBuilder.create();

            Writer writer = new FileWriter(path + "data.json");

            graph.sceneLines.clear();
            graph.sceneNodes.clear();

            // Serialize Nodes & Paths
            NodeHandler.sceneNodes.forEach(node -> graph.sceneNodes.add(Serialize.shapeToString(node)));
            PathHandler.sceneLines.forEach((line, fromTo) -> {
                String sLine = Serialize.shapeToString(line);
                String sFrom = Serialize.shapeToString(fromTo.getKey());
                String sTo = Serialize.shapeToString(fromTo.getValue());
                graph.sceneLines.put(sLine, new Pair<>(sFrom, sTo));
            });

            gson.toJson(graph, writer);
            writer.flush();
            writer.close();

            System.out.println("Saved data!");
        } catch (Exception e) {
            System.out.println("Failed to save data due to: " + e.getMessage());
        }
    }

    public static void load(Controller controller) {
        try {
            File file = new File(path + "data.json");

            if (!file.exists()) {
                System.out.println("Creating directory for data file...");
                new File(path).mkdirs();

                graph = new Serialize.Graph(new ArrayList<>(), new HashMap<>());
                return;
            }

            // Load background image from cache
            for (Path p : Files.walk(Paths.get(Data.path))
                    .filter(Files::isRegularFile).collect(Collectors.toList())) {

                Optional<String> fileFormat = getExtension(p.toString());
                if (!fileFormat.isPresent())
                    return;

                String format = fileFormat.get();

                if (format.equals("png") || format.equals("jpg")) {
                    Image image = new Image(p.toFile().toURI().toString());
                    Data.setBackgroundImage(controller, image);
                    break;
                }
            }

            Gson gson = new GsonBuilder().create();
            FileReader reader = new FileReader(file);
            graph = gson.fromJson(new BufferedReader(reader), Serialize.Graph.class);
            reader.close();

            if (graph != null) {
                // De-serialize circles
                graph.sceneNodes.forEach(sNode -> {
                    Circle node = (Circle) Serialize.stringToShape(sNode);
                    controller.paintPane.getChildren().add(node);
                    NodeHandler.sceneNodes.add(node);
                });

                // De-serialize lines
                graph.sceneLines.forEach((sLine, sPair) -> {
                    Line line = (Line) Serialize.stringToShape(sLine);
                    Shape from = Serialize.stringToShape(sPair.getKey());
                    Shape to = Serialize.stringToShape(sPair.getValue());
                    PathHandler.sceneLines.put(line, new Pair<>(from, to));
                    controller.paintPane.getChildren().add(line);
                });

                // Apply spinner data
                controller.rowSpinner.getValueFactory().setValue(graph.rows);
                controller.columnSpinner.getValueFactory().setValue(graph.columns);

                controller.offXSpinner.getValueFactory().setValue(graph.offX);
                controller.offYSpinner.getValueFactory().setValue(graph.offY);

                System.out.println("Successfully loaded user data!");
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        graph = new Serialize.Graph(new ArrayList<>(), new HashMap<>());
    }
}
