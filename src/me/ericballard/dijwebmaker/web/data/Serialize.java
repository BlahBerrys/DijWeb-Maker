package me.ericballard.dijwebmaker.web.data;

import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Shape;
import javafx.util.Pair;
import me.ericballard.dijwebmaker.gui.handlers.node.NodeHandler;
import me.ericballard.dijwebmaker.gui.handlers.node.PathHandler;

import java.util.ArrayList;
import java.util.HashMap;

public class Serialize {

    public static class Graph {

        // Serialized circle aka "scene node"
        public ArrayList<String> sceneNodes;

        // Serialized line paired with from and to circles
        public HashMap<String, Pair<String, String>> sceneLines;

        public Graph(ArrayList<String> sceneNodes, HashMap<String, Pair<String, String>> sceneLines) {
            this.sceneNodes = sceneNodes;
            this.sceneLines = sceneLines;
        }
    }

    // Serialize shape to string - eg;
    // line:width,sx,sy,ex,ey
    // circle:radius,strokeWidth,x,y
    public static String shapeToString(Shape shape) {
        boolean circle = (shape instanceof Circle);
        StringBuilder sb = new StringBuilder();

        if (circle) {
            Circle c = (Circle) shape;
            sb.append("circle:");
            sb.append(c.getRadius()).append(",");
            sb.append(c.getStrokeWidth()).append(",");
            sb.append(c.getTranslateX()).append(",");
            sb.append(c.getTranslateY());
        } else {
            Line l = (Line) shape;
            sb.append("line:");
            sb.append(l.getStrokeWidth()).append(",");
            sb.append(l.getStartX()).append(",");
            sb.append(l.getStartY()).append(",");
            sb.append(l.getEndX()).append(",");
            sb.append(l.getEndY());
        }

        return sb.toString();
    }

    public static Shape stringToShape(String shape) {
        String[] args = shape.split(":");
        String type = args[0];

        String[] info = args[1].split(",");

        if (type.equals("line")) {
            // Line
            double width = Double.parseDouble(info[0]);
            double sx = Double.parseDouble(info[1]);
            double sy = Double.parseDouble(info[2]);
            double ex = Double.parseDouble(info[3]);
            double ey = Double.parseDouble(info[4]);

            return PathHandler.create(width, sx, sy, ex, ey);
        } else {
            // Circle
            double radius = Double.parseDouble(info[0]);
            double strokeWith = Double.parseDouble(info[1]);
            double x = Double.parseDouble(info[2]);
            double y = Double.parseDouble(info[3]);

            return NodeHandler.create(radius, strokeWith, x, y);
        }
    }

}
