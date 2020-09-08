package me.ericballard.dijwebmaker.web.data;

import java.util.ArrayList;

public class Node {

    public static class Neighbor {
        int x, y;

        public Neighbor(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    // Position of Node
    public int x, y;

    // Neighboring nodes - Pathing to/from node
    public ArrayList<Neighbor> neighbors;

    // Create new node
    public Node(int x, int y, ArrayList<Neighbor> neighbors) {
        this.x = x;
        this.y = y;
        this.neighbors = neighbors;
    }
}
