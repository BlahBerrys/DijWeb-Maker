package me.ericballard.dijwebmaker.gui.handlers.node;

import javafx.geometry.Point2D;
import javafx.scene.shape.Line;
import javafx.scene.shape.Shape;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Pair;
import me.ericballard.dijwebmaker.gui.Controller;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;

public class PathSimulator {

    public static boolean finding;

    // Set 2 waypoints (from/to)
    static Circle from, to;

    public static Line startPath, endPath;

    // Simulate path between waypoints
    public static ArrayList<Shape> paths = new ArrayList<>(), nodes = new ArrayList<>();

    public static void find(Controller controller) {
        double w = controller.img.getImage().getWidth();
        double columnW = w / 2750;
        boolean fromSet = (from != null);
        boolean toSet = (to != null);

        // Set FROM-TO waypoints
        if (!fromSet || !toSet) {
            Circle node = NodeHandler.create(columnW * 3, columnW * 1.5, NodeHandler.X, NodeHandler.Y);
            node.setFill(fromSet ? Color.LIMEGREEN : Color.GOLD);
            controller.paintPane.getChildren().add(node);

            if (!fromSet) {
                from = node;
                return;
            } else
                to = node;
        }

        // Previous path built - lets reset
        if (!paths.isEmpty()) {
            reset(controller);
            return;
        }


        // Calculate path between points
        long start = Instant.now().toEpochMilli();

        // Find closest nodes relative to waypoints
        Point2D fromPoint = new Point2D(from.getTranslateX(), from.getTranslateY());
        Point2D toPoint = new Point2D(to.getTranslateX(), to.getTranslateY());

        double threshold = columnW * 50;
        Shape closestStartNode = NodeHandler.getClosestTo(fromPoint, threshold, false);
        Shape closestEndNode = NodeHandler.getClosestTo(toPoint, threshold, false);

        if (closestStartNode == null || closestEndNode == null) {
            System.out.println("Failed to find start or end node for path. (Start: " + closestStartNode + " | End: " + closestEndNode + ")");
            reset(controller);
            return;
        }

        ArrayList<Shape> deadEnds = new ArrayList<>();
        HashMap<Shape, Integer> pathingNodes = new HashMap<>();

        // Calculate number of paths deriving from each node
        PathHandler.sceneLines.forEach((path, fromTo) -> {
            Shape from = fromTo.getKey();
            Shape to = fromTo.getValue();
            boolean foundFrom = false, foundTo = false;

            for (Shape node : pathingNodes.keySet()) {
                boolean matchesFrom = (from.getTranslateX() == node.getTranslateX() && from.getTranslateY() == node.getTranslateY());
                boolean matchesTo = (to.getTranslateX() == node.getTranslateX() && to.getTranslateY() == node.getTranslateY());

                if (matchesFrom) {
                    foundFrom = true;
                    pathingNodes.put(node, pathingNodes.get(node) + 1);
                } else if (matchesTo) {
                    foundTo = true;
                    pathingNodes.put(node, pathingNodes.get(node) + 1);
                }
            }

            if (!foundFrom)
                pathingNodes.put(from, 1);

            if (!foundTo)
                pathingNodes.put(to, 1);
        });


        // Node - Distance to End Node
        HashMap<Shape, Double> nodeDistances = new HashMap<>();
        Point2D endPoint = new Point2D(closestEndNode.getTranslateX(), closestEndNode.getTranslateY());

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
            if (deadEndNode)
                deadEnds.add(node);

            // Cache every node's distance to our end node
            nodeDistances.put(node, endPoint.distance(new Point2D(node.getTranslateX(), node.getTranslateY())));
        });

        System.out.println("Dead Ends: " + deadEnds.size());
        deadEnds.forEach(de -> de.setFill(Color.BLACK));

        // Cache dead-end nodes
        nodes.add(closestEndNode);
        nodes.add(closestStartNode);
        closestEndNode.setFill(NodeHandler.flashColor);
        closestStartNode.setFill(NodeHandler.flashColor);

        // Set start/end paths
        startPath = PathHandler.create(columnW * 1.5, from.getTranslateX(), from.getTranslateY(), closestStartNode.getTranslateX(), closestStartNode.getTranslateY());
        endPath = PathHandler.create(columnW * 1.5, to.getTranslateX(), to.getTranslateY(), closestEndNode.getTranslateX(), closestEndNode.getTranslateY());
        startPath.getStrokeDashArray().addAll(25d, 10d);
        endPath.getStrokeDashArray().addAll(25d, 10d);

        paths.add(endPath);
        paths.add(startPath);
        controller.paintPane.getChildren().add(endPath);
        controller.paintPane.getChildren().add(startPath);

        // Create a path by iteratively find the closest node from start to end
        System.out.println("Building Path...");

        ArrayList<Shape> dijPath = new ArrayList<>();
        ArrayList<Shape> blackList = new ArrayList<>();
        dijPath.add(closestStartNode);

        Shape nextNode = closestStartNode;
        HashMap<Shape, Shape> optionalPaths = getNext(dijPath, deadEnds, blackList, nextNode, endPoint);

        int pathsFound = 1;

        loop:
        while (true) {
            // Find paths linked to this node

            // Node - Path  | Find path-connected nodes to/from comparableNode
            if (optionalPaths.isEmpty()) {
                if (pathsFound > 0) {
                    // Path is dead-end, back track previous nodes and look for other routes
                    System.out.println(pathsFound + " | Back-Tracking...");
                    int index = dijPath.size() - 1;
                    Shape prevNode = dijPath.get(index);

                    int pIndex = paths.size() - 1;
                    if (pIndex >= 0) {
                        Shape path = paths.get(pIndex);

                        paths.remove(path);
                        path.setStroke(Color.BLACK);
                    }

                    nodes.remove(prevNode);
                    prevNode.setFill(Color.BLACK);

                    dijPath.remove(prevNode);
                    blackList.add(prevNode);
                    pathsFound--;

                    if (index > 0) {
                        // Rebuild path with updated black-list
                        nextNode = dijPath.get(index - 1);
                        optionalPaths = getNext(dijPath, deadEnds, blackList, nextNode, endPoint);
                        continue;
                    }
                }

                System.out.println("Failed to find optional paths for next node.");
                break;
            }

            // Sort results and return optimal node
            double dis = -1;
            Shape closest = null, path = null;

            for (Shape node : optionalPaths.keySet()) {
                Shape line = optionalPaths.get(node);
                boolean matchesEndNode = (node.getTranslateX() == closestEndNode.getTranslateX() && node.getTranslateY() == closestEndNode.getTranslateY());

                if (matchesEndNode) {
                    line.setStroke(NodeHandler.flashColor);
                    paths.add(line);
                    break loop;
                }

                double d = -1;

                for (Shape s : nodeDistances.keySet()) {
                    boolean matchesNode = (node.getTranslateX() == s.getTranslateX() && node.getTranslateY() == s.getTranslateY());
                    if (matchesNode) {
                        d = nodeDistances.get(s);
                        break;
                    }
                }

                if (dis == -1 || d < dis) {
                    closest = node;
                    path = line;
                    dis = d;
                }
            }


            // Found next optimal node
            nextNode = closest;

            paths.add(path);
            nodes.add(closest);

            path.setStroke(NodeHandler.flashColor);
            closest.setFill(NodeHandler.flashColor);

            dijPath.add(closest);
            pathsFound++;

            optionalPaths = getNext(dijPath, deadEnds, blackList, nextNode, endPoint);
        }

        long end = Instant.now().toEpochMilli();
        System.out.println("Calculations concluded in " + ((double) (end - start) / 1000.0) + "s!");

    }

    public static void reset(Controller controller) {
        nodes.forEach(node -> node.setFill(NodeHandler.flashColor));
        paths.forEach(path -> path.setStroke(Color.BLACK));

        if (from != null) {
            controller.paintPane.getChildren().remove(from);
            from = null;
        }
        if (to != null) {
            controller.paintPane.getChildren().remove(to);
            to = null;
        }

        if (startPath != null) {
            controller.paintPane.getChildren().remove(startPath);
            startPath = null;
        }

        if (endPath != null) {
            controller.paintPane.getChildren().remove(endPath);
            endPath = null;
        }

        nodes.clear();
        paths.clear();
    }

    public static HashMap<Shape, Shape> getNext(ArrayList<Shape> dijPath, ArrayList<Shape> deadEnds, ArrayList<Shape> blackList, Shape comparableNode, Point2D endPoint) {
        // Shape - Connected Node | Shape - Connecting Path
        HashMap<Shape, Shape> optionalPaths = new HashMap<>();

        // Check if path is connected to our current node
        pathLoop:
        for (Shape path : PathHandler.sceneLines.keySet()) {
            Pair<Shape, Shape> fromTo = PathHandler.sceneLines.get(path);
            Shape from = fromTo.getKey();
            Shape to = fromTo.getValue();

            boolean matchesFrom = (from.getTranslateX() == comparableNode.getTranslateX() && from.getTranslateY() == comparableNode.getTranslateY());
            boolean matchesTo = (to.getTranslateX() == comparableNode.getTranslateX() && to.getTranslateY() == comparableNode.getTranslateY());

            // This path IS connected to our current node
            if (matchesFrom || matchesTo) {
                Shape matchingNode = (matchesFrom ? to : from);

                boolean matchesEndNode = (matchingNode.getTranslateX() == endPoint.getX() && matchingNode.getTranslateY() == endPoint.getY());

                if (matchesEndNode) {
                    optionalPaths.put(matchingNode, path);
                    break;
                }

                // Check if node is black-listed (part of dead-end path)
                for (Shape blackNode : blackList) {
                    if (blackNode.getTranslateX() == matchingNode.getTranslateX() && blackNode.getTranslateY() == matchingNode.getTranslateY()) {
                        // Node is black-listed do not use
                        continue pathLoop;
                    }
                }

                // Check if node is dead-end node
                for (Shape deadEnd : deadEnds) {
                    if (deadEnd.getTranslateX() == matchingNode.getTranslateX() && deadEnd.getTranslateY() == matchingNode.getTranslateY()) {
                        // Used dead-end if it's close enough to end point
                        // if (dePoint.distance(endPoint) > threshold)
                        continue pathLoop;
                    }
                }

                // Check if match is already used in path
                for (Shape prevNode : dijPath) {
                    double x = (matchesFrom ? to.getTranslateX() : from.getTranslateX());
                    double y = (matchesFrom ? to.getTranslateY() : from.getTranslateY());
                    if (prevNode.getTranslateX() == x && prevNode.getTranslateY() == y)
                        continue pathLoop;
                }

                // This path is connected to our comparableNode and usable
                if (!optionalPaths.containsValue(path))
                    optionalPaths.put(matchingNode, path);
            }
        }
        return optionalPaths;
    }
}
