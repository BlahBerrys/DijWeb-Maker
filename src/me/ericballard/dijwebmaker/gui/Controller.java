package me.ericballard.dijwebmaker.gui;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import me.ericballard.dijwebmaker.gui.handlers.button.ButtonHandler;
import me.ericballard.dijwebmaker.gui.handlers.node.NodeHandler;
import me.ericballard.dijwebmaker.web.data.Data;

import java.net.URL;
import java.util.*;

public class Controller implements Initializable {

    @FXML
    public ImageView img;

    @FXML
    public Pane paintPane;

    @FXML
    public Button simBtn, expBtn;

    @FXML
    public Text xTxt, yTxt, nodeTxt;

    @FXML
    public GridPane mainGrid, cordGrid;

    @FXML
    public ScrollPane imgPane, cordPane;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        /*
         * Scroll Panes
         */

        // Set scroll panes to size of image
        double h = img.getImage().getHeight();
        double w = img.getImage().getWidth();

        cordGrid.setMinSize(w, h);
        cordGrid.setMaxSize(w, h);

        paintPane.setMinSize(w, h);
        paintPane.setMaxSize(w, h);

        // Resize image pane when cord pane is resized
        cordPane.hvalueProperty().addListener(e -> imgPane.setHvalue(cordPane.getHvalue()));
        cordPane.vvalueProperty().addListener(e -> imgPane.setVvalue(cordPane.getVvalue()));

        // Prevent scrolling by mouse wheel
        cordPane.addEventFilter(ScrollEvent.SCROLL, Event::consume);

        cordPane.setOnMouseEntered(e -> cordPane.setCursor(Cursor.HAND));
        cordPane.setOnMouseExited(e -> cordPane.setCursor(Cursor.DEFAULT));

        // Track mouse and related x/y
        cordGrid.setOnMouseMoved(NodeHandler.trackMouse(this));

        // Graph x/y
        cordGrid.setOnMouseClicked(NodeHandler.click(this));

        /*
         * Buttons
         */
        simBtn.setOnAction(e -> ButtonHandler.click(this, false));

        expBtn.setOnAction(e -> ButtonHandler.click(this, true));

        Data.load(this);

        nodeTxt.setText("Nodes: " + NodeHandler.sceneNodes.size());
    }


    public void resize() {

    }
}
