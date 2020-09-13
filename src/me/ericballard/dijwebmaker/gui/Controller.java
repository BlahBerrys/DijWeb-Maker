package me.ericballard.dijwebmaker.gui;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.*;
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
    public Text xTxt, yTxt, nodeTxt;

    @FXML
    public GridPane mainGrid, cordGrid;

    @FXML
    public Button simBtn, expBtn, bgBtn;

    @FXML
    public ScrollPane imgPane, cordPane;

    @FXML
    public Spinner<Integer> offXSpinner, offYSpinner, columnSpinner, rowSpinner;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // X-Offset Spinner
        SpinnerValueFactory.IntegerSpinnerValueFactory offXFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(-9999, 9999, 0);
        TextFormatter offXF = new TextFormatter(offXFactory.getConverter(), offXFactory.getValue());
        offXSpinner.getEditor().setTextFormatter(offXF);

        offXSpinner.setValueFactory(offXFactory);
        offXFactory.valueProperty().bindBidirectional(offXF.valueProperty());
        offXSpinner.valueProperty().addListener(e -> Data.graph.offX = offXFactory.getValue());

        // X-Offset Spinner
        SpinnerValueFactory.IntegerSpinnerValueFactory offYFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(-9999, 9999, 0);
        TextFormatter offYF = new TextFormatter(offYFactory.getConverter(), offYFactory.getValue());
        offYSpinner.getEditor().setTextFormatter(offYF);

        offYSpinner.setValueFactory(offYFactory);
        offYFactory.valueProperty().bindBidirectional(offYF.valueProperty());
        offYSpinner.valueProperty().addListener(e -> Data.graph.offY = offYFactory.getValue());

        // Rows Spinner
        SpinnerValueFactory.IntegerSpinnerValueFactory rowFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(-9999, 9999, 0);
        TextFormatter rowF = new TextFormatter(rowFactory.getConverter(), rowFactory.getValue());
        rowSpinner.getEditor().setTextFormatter(rowF);

        rowSpinner.setValueFactory(rowFactory);
        rowFactory.valueProperty().bindBidirectional(rowF.valueProperty());
        rowSpinner.valueProperty().addListener(e -> Data.graph.rows = rowFactory.getValue());

        // Columns Spinner
        SpinnerValueFactory.IntegerSpinnerValueFactory columnFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(-9999, 9999, 0);
        TextFormatter columnF = new TextFormatter(columnFactory.getConverter(), columnFactory.getValue());
        columnSpinner.getEditor().setTextFormatter(columnF);

        columnSpinner.setValueFactory(columnFactory);
        columnFactory.valueProperty().bindBidirectional(columnF.valueProperty());
        columnSpinner.valueProperty().addListener(e -> Data.graph.columns = columnFactory.getValue());

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

        bgBtn.setOnAction(e -> ButtonHandler.selectImage(this));

        Data.load(this);

        nodeTxt.setText("Nodes: " + NodeHandler.sceneNodes.size());
    }


    public void resize() {

    }
}
