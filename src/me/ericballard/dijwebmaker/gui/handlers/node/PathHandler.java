package me.ericballard.dijwebmaker.gui.handlers.node;

import com.sun.javafx.geom.Line2D;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Shape;
import javafx.util.Pair;
import me.ericballard.dijwebmaker.gui.Controller;

import java.util.HashMap;

public class PathHandler {

    //  Used for creating new path lines from-to nodes
    static Shape selectedLineSource;

    static boolean selectedStartPoint;

    public static Line hoveredLine, selectedLine, selectedCopy;

    // Line - From Circle - To Circle
    public static HashMap<Shape, Pair<Shape, Shape>> sceneLines = new HashMap<>();

    public static void resetSelected() {
        if (selectedStartPoint) {
            selectedLine.setStartX(selectedCopy.getStartX());
            selectedLine.setStartY(selectedCopy.getStartY());
        } else {
            selectedLine.setEndX(selectedCopy.getEndX());
            selectedLine.setEndY(selectedCopy.getEndY());
        }

        selectedLineSource = null;
        selectedCopy = null;
        selectedLine = null;
    }

    public static void draw(Controller controller, double width) {
        // Draw lines
        if (NodeHandler.sceneNodes.size() <= 1)
            return;

        // Get node
        nodeLoop:
        for (Shape shape : NodeHandler.sceneNodes) {
            // Check if node has existing paths
            for (Shape lineShape : sceneLines.keySet()) {
                Pair<Shape, Shape> fromTo = sceneLines.get(lineShape);

                Shape fromNode = fromTo.getKey();
                Shape toNode = fromTo.getValue();

                boolean matchesFrom = (fromNode.getTranslateX() == shape.getTranslateX() && fromNode.getTranslateY() == shape.getTranslateY());
                boolean matchesTo = (toNode.getTranslateX() == shape.getTranslateX() && toNode.getTranslateY() == shape.getTranslateY());

                if (matchesFrom || matchesTo) {
                    continue nodeLoop;
                }
            }

            double columnW = controller.img.getImage().getWidth() / 2750;
            // 100 tiles
            double threshold = columnW * 100;

            Shape closest = NodeHandler.getClosestTo(new Point2D(shape.getTranslateX(), shape.getTranslateY()), threshold, true);

            if (closest == null) {
                //System.out.println("Failed to draw line, no closest node.");
                return;
            }

            // Draw line from this to closest
            Line line = create(width, shape.getTranslateX(), shape.getTranslateY(), closest.getTranslateX(), closest.getTranslateY());

            controller.paintPane.getChildren().add(line);
            sceneLines.put(line, new Pair<>(shape, closest));

            System.out.println("Drew new line!");
        }
    }

    public static Line create(double width, double startX, double startY, double endX, double endY) {
        Line line = new Line(startX, startY, endX, endY);
        line.setStrokeWidth(width);
        line.setVisible(true);

        line.setOnMouseEntered(e -> {
            if (PathSimulator.finding || selectedLine != null)
                return;
            line.setStroke(NodeHandler.flashColor);
            hoveredLine = line;
        });

        line.setOnMouseExited(e -> {
            if (PathSimulator.finding || selectedLine != null)
                return;
            line.setStroke(Color.BLACK);
            hoveredLine = null;
        });

        line.setOnMouseClicked(e -> {
            // Reposition line
            if (PathSimulator.finding || selectedLine != null)
                return;

            // Consume event - prevent placing new node
            e.consume();

            boolean rightClick = (e.getButton() == MouseButton.SECONDARY);

            if (rightClick) {
                ((Pane) line.getParent()).getChildren().remove(line);
                sceneLines.remove(line);
                return;
            }

            selectedLine = line;
            selectedCopy = new Line(line.getStartX(), line.getStartY(), line.getEndX(), line.getEndY());
            selectedCopy.setStrokeWidth(line.getStrokeWidth());

            // Determine which end was clicked
            Point2D mousePos = new Point2D(e.getX(), e.getY());
            Point2D startPos = new Point2D(line.getStartX(), line.getStartY());
            Point2D endPos = new Point2D(line.getEndX(), line.getEndY());

            selectedStartPoint = (mousePos.distance(startPos) < mousePos.distance(endPos));
            selectedLineSource = NodeHandler.getClosestTo((selectedStartPoint ? endPos : startPos), width * 5, selectedStartPoint);
        });

        return line;
    }


    public static void resizeLine(Controller controller, MouseEvent e, Shape closest) {
        // Re-set selected line
        boolean tempLine = (!sceneLines.containsKey(selectedLine));

        // Prevent new line from being set on to & from source node
        if (tempLine && selectedLineSource != null && closest != null) {
            if (!closest.equals(selectedLineSource)) {
                // Valid
                sceneLines.put(selectedLine, new Pair<>(selectedLineSource, closest));
            } else {
                System.out.println("Selected line cannot be attached to source node!");

                // Attempted to direct line to and from source node
                e.consume(); // Consume event - prevent placing new node
                controller.paintPane.getChildren().remove(selectedLine);
                resetSelected();
                return;
            }
        }

        if (closest != null) {
            double x = closest.getTranslateX(), y = closest.getTranslateY();

            // Prevent re-directed line from being set on source node
            if ((x == selectedCopy.getEndX() && y == selectedCopy.getEndY()) || (x == selectedCopy.getStartX() && y == selectedCopy.getStartY())) {
                resetSelected();
                return;
            }

            // Set line to closest node to mouse
            if (selectedStartPoint) {
                selectedLine.setStartX(x);
                selectedLine.setStartY(y);
            } else {
                selectedLine.setEndX(x);
                selectedLine.setEndY(y);
            }

            // Check if line intersects another - cancel if so
            double sx = selectedLine.getStartX(), ex = selectedLine.getEndX(), sy = selectedLine.getStartY(), ey = selectedLine.getEndY();
            Line2D l2d = new Line2D((float) sx, (float) sy, (float) ex, (float) ey);

            // Ignore checking for intersecting lines as the current method is too aggressive
            // Additionally the end-user should be able to validate their own paths
            int intersects = 0;//getIntersectingLines(l2d, closest);

            if (intersects == 0) {
                // Successful re-attach
                System.out.println("Attached line to new node!");
                closest.setFill(Color.LIMEGREEN);
                selectedLineSource = null;
                selectedCopy = null;
                selectedLine = null;
                return;
            } else
                System.out.println("Intersecting Lines: " + intersects);
        }

        // Failed to find node near mouse - remove temporary line
        if (tempLine) {
            System.out.println("Deleted temporary line");
            controller.paintPane.getChildren().remove(selectedLine);
            sceneLines.remove(selectedLine);
            selectedLineSource = null;
            selectedCopy = null;
            selectedLine = null;
            return;
        }

        // No nodes near click - reset line to previous position
        System.out.println("Resetting line position.");
        resetSelected();
    }

    private static int getIntersectingLines(Line2D l2d, Shape closest) {
        int intersects = 0;

        // Compare scene lines to find any intersecting paths
        for (Shape shape : sceneLines.keySet()) {
            selectedLineSource.setFill(Color.GOLD);

            if (shape.equals(selectedLine))
                continue;

            // Line to compare
            Line line = (Line) shape;
            double lsx = line.getStartX(), lex = line.getEndX(), lsy = line.getStartY(), ley = line.getEndY();
            if (l2d.intersectsLine(new Line2D((float) lsx, (float) lsy, (float) lex, (float) ley))) {

                // Check if intersecting line is neighbor  - if so ignore
                Pair<Shape, Shape> fromTo = sceneLines.get(shape);
                Shape fromNode = fromTo.getKey(), toNode = fromTo.getValue();

                if ((fromNode.equals(closest) && toNode.equals(selectedLineSource))
                        || (toNode.equals(closest) && fromNode.equals(selectedLineSource))) {
                    // Prevent
                } else {
                    if (fromNode.equals(selectedLineSource) || toNode.equals(selectedLineSource)
                            || fromNode.equals(closest) || toNode.equals(closest)) {
                        continue;
                    }
                }

                // Line is intersecting another path
                //shape.setStroke(Color.GOLD);
                intersects++;
            }
        }
        return intersects;
    }
}
