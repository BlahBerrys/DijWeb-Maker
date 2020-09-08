package me.ericballard.dijwebmaker.web.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Shape;
import javafx.util.Pair;
import me.ericballard.dijwebmaker.gui.handlers.node.NodeHandler;
import me.ericballard.dijwebmaker.gui.Controller;
import me.ericballard.dijwebmaker.gui.handlers.node.PathHandler;
import me.ericballard.dijwebmaker.web.data.Serialize;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Data {

    public static final String path = new JFileChooser().getFileSystemView().getDefaultDirectory().toString() + File.separator + "DijWebMaker" + File.separator;

    public static Serialize.Graph graph;

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

                System.out.println("Successfully loaded user data!");
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        graph = new Serialize.Graph(new ArrayList<>(), new HashMap<>());
    }
}
