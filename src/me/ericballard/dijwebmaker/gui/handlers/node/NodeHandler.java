package me.ericballard.dijwebmaker.gui.handlers.node;

import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeType;
import javafx.util.Pair;
import me.ericballard.dijwebmaker.gui.Controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class NodeHandler {

    // Mouse pos (reflecting tiles)
    static double X, Y;

    public static Color flashColor;

    // Placed nodes (pos relative to scene)
    public static ArrayList<Shape> sceneNodes = new ArrayList<>();

    public static Shape getClosestTo(Point2D pos, double threshold, boolean ignoreSelf) {
        threshold = (threshold < 15 ? 15 : threshold);
        HashMap<Shape, Double> acceptableNodes = new HashMap<>();

        // Find nodes near click
        for (Shape shape : sceneNodes) {
            Point2D p = new Point2D(shape.getTranslateX(), shape.getTranslateY());
            if (ignoreSelf && p.equals(pos))
                continue;

            double dis = p.distance(pos);
            if (dis < threshold)
                acceptableNodes.put(shape, dis);
        }

        // Check if any are near click, if so find closest
        if (!acceptableNodes.isEmpty()) {
            Shape closest = null;
            double dis = -1;

            for (Shape shape : acceptableNodes.keySet()) {
                double d = acceptableNodes.get(shape);
                if (dis == -1 || d < dis) {
                    closest = shape;
                    dis = d;
                }
            }
            return closest;
        }
        return null;
    }

    // Returns nodes that have connected paths to/from comparableNode
    public static HashMap<Shape, Shape> getNeighbors(Shape comparableNode) {
        // Shape - Connected Node | Shape - Connecting Path
        HashMap<Shape, Shape> optionalPaths = new HashMap<>();
        PathHandler.sceneLines.forEach((path, fromTo) -> {
            // Check if path is connected to our current node
            Shape from = fromTo.getKey();
            Shape to = fromTo.getValue();

            boolean matchesFrom = (from.getTranslateX() == comparableNode.getTranslateX() && from.getTranslateY() == comparableNode.getTranslateY());
            boolean matchesTo = (to.getTranslateX() == comparableNode.getTranslateX() && to.getTranslateY() == comparableNode.getTranslateY());

            if (matchesFrom || matchesTo) {
                // This path is connected to our comparableNode
                if (!optionalPaths.containsValue(path))
                    optionalPaths.put((matchesFrom ? to : from), path);
            }
        });
        return optionalPaths;
    }


    public static EventHandler<? super MouseEvent> trackMouse(Controller controller) {
        return e -> {
            if (PathHandler.selectedLine != null) {
                // Re-positioning line
                if (PathHandler.selectedStartPoint) {
                    PathHandler.selectedLine.setStartX(e.getX());
                    PathHandler.selectedLine.setStartY(e.getY());
                } else {
                    PathHandler.selectedLine.setEndX(e.getX());
                    PathHandler.selectedLine.setEndY(e.getY());
                }
                return;
            }

            // Update info text of mouse x/y
            // Column -> ####
            //    Row -> #
            //           #

            // Mouse position
            X = e.getX();
            Y = e.getY();

            Image i = controller.img.getImage();

            if (i == null)
                return;

            double h = i.getHeight();
            double w = i.getWidth();

            // Grid offsets
            int startX = controller.offXSpinner.getValue(), startY = controller.offYSpinner.getValue();

            // Grid
            double columnW = w / controller.columnSpinner.getValue();
            double rowW = h / controller.rowSpinner.getValue();

            // Define column/row based on mouse position divided by possible rows/columns
            int column = startX + (int) (X / columnW);
            int row = startY + ((int) ((h - Y) / rowW));

            // Update info txt
            controller.xTxt.setText("X: " + column);
            controller.yTxt.setText("Y: " + row);
        };
    }

    public static EventHandler<? super MouseEvent> click(Controller controller) {
        return e -> {
            boolean rightClick = (e.getButton() == MouseButton.SECONDARY);

            if (PathSimulator.finding) {
                if (!rightClick)
                    return;

                // Simulating Path-Finder
                PathSimulator.find(controller);
                return;
            }

            // Determine tile margins base on image size
            double w = controller.img.getImage().getWidth();

            double columnW = w / controller.columnSpinner.getValue();
            double threshold = columnW * 5;

            // Find nearest node near click
            Shape closest = getClosestTo(new Point2D(e.getX(), e.getY()), threshold, false);

            /*
             * LINE MANAGEMENT
             * (Resize, Replace, Create, Delete)
             */
            Line selectedLine = PathHandler.selectedLine;

            if (selectedLine != null) {
                PathHandler.resizeLine(controller, e, closest);
                return;
            }

            /*
             * NODE MANAGEMENT
             * (Create, Delete, Path-from)
             */

            // Clicked existing node
            if (closest != null) {
                if (rightClick) {
                    // (Right-Click) Delete existing node
                    // Remove connected paths
                    Iterator<Shape> itr = new ArrayList<>(PathHandler.sceneLines.keySet()).iterator();

                    while (itr.hasNext()) {
                        Shape line = itr.next();
                        Pair<Shape, Shape> toFrom = PathHandler.sceneLines.get(line);
                        //TODO
                        Shape from = toFrom.getKey();
                        Shape to = toFrom.getValue();

                        boolean matchesFrom = (from.getTranslateX() == closest.getTranslateX() && from.getTranslateY() == closest.getTranslateY());
                        boolean matchesTo = (to.getTranslateX() == closest.getTranslateX() && to.getTranslateY() == closest.getTranslateY());

                        if (matchesFrom || matchesTo) {
                            controller.paintPane.getChildren().remove(line);
                            PathHandler.sceneLines.remove(line);
                        }
                        itr.remove();
                    }

                    sceneNodes.remove(closest);
                    controller.paintPane.getChildren().remove(closest);
                    controller.nodeTxt.setText("Nodes: " + sceneNodes.size());
                } else {
                    // (Left-Click) Create new line from node as source
                    Line line = PathHandler.create(columnW * 1.5, closest.getTranslateX(), closest.getTranslateY(), e.getX(), e.getY());
                    PathHandler.selectedLineSource = closest;
                    PathHandler.selectedStartPoint = false;
                    PathHandler.selectedLine = line;

                    PathHandler.selectedCopy = new Line(line.getStartX(), line.getStartY(), line.getEndX(), line.getEndY());
                    PathHandler.selectedCopy.setStrokeWidth(line.getStrokeWidth());

                    controller.paintPane.getChildren().add(line);
                }
                return;
            }

            if (!rightClick)
                return;

            // Clicked empty space
            // (Right-Click) Create and style new node
            Circle node = create(columnW * 3, columnW * 1.5, X, Y);

            sceneNodes.add(node);
            controller.paintPane.getChildren().add(node);
            controller.nodeTxt.setText("Nodes: " + sceneNodes.size());

            PathHandler.draw(controller, columnW * 1.5);

        };
    }

    // Node styling - Linear gradient
    static final Stop[] stops = new Stop[]{new Stop(0, Color.BLACK), new Stop(0.5, Color.WHITE), new Stop(1, Color.BLACK)};
    static final LinearGradient lg = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE, stops);

    // Hover
    static final Stop[] hStops = new Stop[]{new Stop(0, Color.BLACK), new Stop(0.1, Color.WHITE), new Stop(0.5, Color.BLACK), new Stop(0.9, Color.WHITE), new Stop(1, Color.BLACK)};
    static final LinearGradient hlg = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE, hStops);

    public static Circle create(double radius, double strokeWidth, double x, double y) {
        radius = (radius < 6 ? 6 : radius);
        strokeWidth = (strokeWidth < 3 ? 3 : strokeWidth);

        Circle c = new Circle(radius);
        c.setFill(Color.BLACK);
        c.setTranslateX(x);
        c.setTranslateY(y);
        c.setVisible(true);

        c.setStroke(lg);
        c.setStrokeWidth(strokeWidth);
        c.setStrokeType(StrokeType.OUTSIDE);

        c.setOnMouseEntered(ee -> c.setStroke(hlg));
        c.setOnMouseExited(ee -> c.setStroke(lg));
        return c;
    }

}
