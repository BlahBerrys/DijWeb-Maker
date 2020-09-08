package me.ericballard.dijwebmaker.gui.handlers.button;

import me.ericballard.dijwebmaker.gui.Controller;
import me.ericballard.dijwebmaker.gui.handlers.node.PathHandler;
import me.ericballard.dijwebmaker.web.Web;
import me.ericballard.dijwebmaker.gui.handlers.node.PathSimulator;

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
}
