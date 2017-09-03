package tbsc.dutchhelper;

import com.sun.javafx.scene.control.skin.LabeledText;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import tbsc.dutchhelper.eventhandlers.PlayAudioEventHandler;
import tbsc.dutchhelper.eventhandlers.SearchEventHandler;
import tbsc.dutchhelper.util.DebugHelper;
import tbsc.dutchhelper.util.Log;
import tbsc.dutchhelper.util.wikt.WiktionaryHelper;

/**
 * Valid arguments:
 * -h, --help: Show help
 * -d: Enable all debug modes
 * --debug: Enable specific debug modes (explained further down)
 * -p, --database-path: Set a custom path to the Wiktionary database
 *
 * Debug modes are explained in {@link DebugHelper}.
 *
 * Exit codes:
 * 0: no error
 * 1: invalid database path
 *
 * Small note to anyone, really: Null typically shouldn't be used.
 * Null safety can be achieved by not using null, so try to not return null when possible.
 *
 * Created on 09/08/2017
 * @author tbsc
 * @since 2.0.0
 */
public class DutchHelperApplication extends Application {

    /**
     * Instance of this class, for other classes to access controls
     */
    public static DutchHelperApplication instance;

    public Stage stage;
    public GridPane grid;
    public TextField wordField;
    public Button searchBtn;
    public Button playAudioBtn;
    public ListView<String> definitionListView;
    public Label errorLabel;

    private static Log log = new Log(DutchHelperApplication.class);

    /**
     * What file should {@link PlayAudioEventHandler} should play.
     * This is a property so that the play audio button will automatically update its text
     */
    public static StringProperty currentAudioFileProperty = new SimpleStringProperty();

    @Override
    public void start(Stage stage) throws Exception {
        WiktionaryHelper.load();

        instance = this;
        this.stage = stage;
        stage.setTitle("DutchHelper");

        setupControls();
        log.i("Added controls");

        // let JavaFX calculate dimension
        stage.setScene(new Scene(grid, -1, -1));

        stage.show();
        resetMinSize();
        log.i("Finished startup");
    }

    /* GUI setup */

    private void setupControls() {
        grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(7.5D);
        grid.setVgap(6.0D);
        // not needed anymore due to center alignment and no resizing
        // grid.setPadding(new Insets(4, 4, 4, 4));

        // show grid lines if enabled
        if (DebugHelper.isModeActive(DebugHelper.Modes.GRID_LINES)) {
            grid.setGridLinesVisible(true);
        }

        // begin adding controls

        grid.add(new Label("Enter Dutch word:"), 0, 0, 1, 1);
        log.d("Added 'Enter Dutch word:' label");

        errorLabel = new Label("");
        errorLabel.setTextFill(Paint.valueOf(Constants.ERROR_LABEL_COLOR));
        errorLabel.setMaxWidth(Double.MAX_VALUE);
        errorLabel.setAlignment(Pos.CENTER_RIGHT);
        grid.add(errorLabel, 1, 0, 2, 1);
        // not adding this node, because it should only be added when an error needs to be shown

        wordField = new TextField();
        wordField.setMaxWidth(Double.MAX_VALUE);
        wordField.setOnAction(new SearchEventHandler());
        grid.add(wordField, 0, 1, 2, 1);
        log.d("Added word text field");

        searchBtn = new Button("Search");
        searchBtn.setMinWidth(searchBtn.getPrefWidth());
        searchBtn.setOnAction(wordField.getOnAction());
        grid.add(searchBtn, 2, 1);
        log.d("Added search button");

        setupDefinitionsList();

        playAudioBtn = new Button("Play Audio");
        currentAudioFileProperty.addListener(((observable, oldValue, newValue) -> {
            playAudioBtn.setText("Play Audio" + ("".equals(newValue) ? "" : ": " + newValue));
        }));
        // so the value wouldn't be null
        currentAudioFileProperty.set("");
        // playAudioBtn.textProperty().bind(Bindings.format("Play Audio%s", ("".equals(CURRENT_AUDIO_FILE) ? "" : ": " + CURRENT_AUDIO_FILE)));
        playAudioBtn.setMaxWidth(Double.MAX_VALUE);
        playAudioBtn.setOnAction(new PlayAudioEventHandler());
        grid.add(playAudioBtn, 0, 3, 3, 1);
        log.d("Added play audio button");
    }

    private void setupDefinitionsList() {
        definitionListView = new ListView<>();
        definitionListView.setPrefSize(100, 250);
        // enable text wrapping
        definitionListView.setCellFactory(list -> new ListCell<String>() {
            {
                LabeledText text = new LabeledText(new Label());
                text.wrappingWidthProperty().bind(list.widthProperty().subtract(25));
                text.textProperty().bind(itemProperty());
                // text.setText(getItem());

                setPrefWidth(0);
                setGraphic(text);
                // setStyle("-fx-selection-bar: transparent");
            }
        });

        // prevents selecting cells
        definitionListView.getSelectionModel().selectedItemProperty().addListener(((observable, oldValue, newValue) -> {
            Platform.runLater(definitionListView.getSelectionModel()::clearSelection);
        }));

        // definitionListView.addEventFilter(MouseEvent.MOUSE_PRESSED, Event::consume);
        grid.add(definitionListView, 0, 2, 3, 1);
        log.d("Added definition scroll pane");
    }

    /**
     * Changes minimum size to the size that fits components of the window.
     */
    private void resetMinSize() {
        // enabling resizability beforehand is needed to keep the padding, for some reason
        stage.setResizable(true);
        stage.sizeToScene();
        // remove everything after decimal point without casting
        log.d("Limiting window size to %.0fx%.0f", stage.getWidth(), stage.getHeight());
        stage.setResizable(false);
    }

    /**
     * Sets the error label's text to the given error message.
     * It also adds a tooltip to the label, in case the message is longer than the label can show.
     * Anything that may display an error should blank this label out (by calling this with an empty string).
     *
     * @param errorMessage The message to show
     */
    public static void showError(String errorMessage) {
        // wrapped in a runnable to let other threads show errors
        Platform.runLater(() -> {
            get().errorLabel.setText(errorMessage);

            // show tooltip only if a message was supplied
            if (!errorMessage.isEmpty()) {
                Tooltip tooltip = new Tooltip(errorMessage);
                tooltip.setFont(Font.font(13));
                get().errorLabel.setTooltip(tooltip);
            }
        });
    }

    /**
     * Sets the error label's text to the given error message, and also sets a tooltip.
     *
     * @param errorMessage The message to show
     * @param tooltip Which tooltip message to show
     */
    public static void showError(String errorMessage, String tooltip) {
        // wrapped in a runnable to let other threads show errors
        Platform.runLater(() -> {
            get().errorLabel.setText(errorMessage);

            get().errorLabel.setTooltip(new Tooltip(tooltip));
        });
    }

    /**
     * Shorter way to get the instance of this class.
     * (because writing .instance is so much longer than .get())
     * @return instance of the application (this class)
     */
    public static DutchHelperApplication get() {
        return instance;
    }

    public static void main(String[] args) {
        ArgumentHandler.handle(args);
        log.i("Starting DutchHelper version " + Constants.VERSION);
        launch(args);
    }

}
