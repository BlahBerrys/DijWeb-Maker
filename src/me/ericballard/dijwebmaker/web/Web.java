package me.ericballard.dijwebmaker.web;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.scene.shape.Shape;
import me.ericballard.dijwebmaker.gui.Controller;
import me.ericballard.dijwebmaker.gui.handlers.node.NodeHandler;
import me.ericballard.dijwebmaker.gui.handlers.node.PathHandler;
import me.ericballard.dijwebmaker.web.data.Data;
import me.ericballard.dijwebmaker.web.data.Node;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;

public class Web {

    Integer size;

    ArrayList<Node> nodes;

    ArrayList<Node> deadEndNodes;

    Web(ArrayList<Node> nodes, ArrayList<Node> deadEndNodes) {
        this.size = nodes.size();
        this.nodes = nodes;
        this.deadEndNodes = deadEndNodes;
    }


    public static void export(Controller controller) {
        // Register x-y reflecting offsets as grid x,y instead of scene x,y
        double h = controller.img.getImage().getHeight();
        double w = controller.img.getImage().getWidth();

        // Grid offsets
        int startX = controller.offXSpinner.getValue(), startY = controller.offYSpinner.getValue();

        // Grid
        double columnW = w / controller.columnSpinner.getValue();
        double rowW = h / controller.rowSpinner.getValue();

        // Export Nodes to flat-file
        ArrayList<Node> nodes = new ArrayList<>();

        for (Shape webNode : NodeHandler.sceneNodes) {
            // Cache neighboring nodes
            HashMap<Shape, Shape> neighborNodes = NodeHandler.getNeighbors(webNode);
            ArrayList<Node.Neighbor> neighbors = new ArrayList<>();

            for (Shape node : neighborNodes.keySet()) {
                // Define column/row (aka grid x,y) based on position divided by possible rows/columns
                int x = startX + (int) (node.getTranslateX() / columnW), y = startY + ((int) ((h - node.getTranslateY()) / rowW));
                neighbors.add(new Node.Neighbor(x, y));
            }

            // Store node with collected data
            int x = startX + (int) (webNode.getTranslateX() / columnW), y = startY + ((int) ((h - webNode.getTranslateY()) / rowW));
            nodes.add(new Node(x, y, neighbors));
        }

        // Get dead end nodes
        ArrayList<Node> deadEnds = new ArrayList<>();
        HashMap<Shape, Integer> pathingNodes = new HashMap<>();

        // Calculate number of paths deriving from each node
        PathHandler.sceneLines.forEach((path, fromTo) -> {
            Shape from = fromTo.getKey();
            Shape to = fromTo.getValue();
            boolean foundFrom = false, foundTo = false;

            for (Shape node : pathingNodes.keySet()) {
                boolean matchesFrom = (from.getTranslateX() == node.getTranslateX() && from.getTranslateY() == node.getTranslateY());
                boolean matchesTo = (to.getTranslateX() == node.getTranslateX() && to.getTranslateY() == node.getTranslateY());

                if (matchesFrom || matchesTo) {
                    if (matchesFrom)
                        foundFrom = true;
                    else if (matchesTo)
                        foundTo = true;

                    pathingNodes.put(node, pathingNodes.get(node) + 1);
                }
            }

            if (!foundFrom)
                pathingNodes.put(from, 1);
            if (!foundTo)
                pathingNodes.put(to, 1);
        });

        NodeHandler.sceneNodes.forEach(node -> {
            boolean deadEndNode = true;
            for (Shape pathNode : pathingNodes.keySet()) {
                if (pathNode.getTranslateX() == node.getTranslateX() && pathNode.getTranslateY() == node.getTranslateY()) {
                    // Set node as "dead end" if it has less than 2 paths deriving to/from itself
                    int paths = pathingNodes.get(pathNode);
                    deadEndNode = paths < 2;
                    break;
                }
            }

            // Cache dead-end nodes
            if (deadEndNode) {
                int x = startX + (int) (node.getTranslateX() / columnW), y = startY + ((int) ((h - node.getTranslateY()) / rowW));
                deadEnds.add(new Node(x, y, null));
            }
        });

        // Save to flat-file
        try {
            GsonBuilder gsonBuilder = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting();
            Gson gson = gsonBuilder.create();

            Writer writer = new FileWriter(Data.path + "dij_web.json");
            gson.toJson(new Web(nodes, deadEnds), writer);

            writer.flush();
            writer.close();

            System.out.println("Exported nodes as web!");

            // Open containing folder
            Desktop.getDesktop().open(new File(Data.path));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
