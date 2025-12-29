package fx.shield.cs.UI;

import fx.shield.cs.WIN.WindowsUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class TopBarIcons {

    private static final int CIRCLE_SIZE = 38;

    private static final Font FONT_UI_BOLD = StyleConstants.FONT_EXTRA_BOLD_18;
    private static final Font FONT_EMOJI   = StyleConstants.FONT_EMOJI_18;

    private static final String ICON_BTN =
            "-fx-background-color: transparent;" +
                    "-fx-text-fill: white;" +
                    "-fx-padding: 0;";

    private static final String CIRCLE_BASE =
            "-fx-background-radius: 999;" +
                    "-fx-border-width: 1;" +
                    "-fx-border-radius: 999;" +
                    "-fx-cursor: hand;";

    private static final String CIRCLE_NORMAL =
            CIRCLE_BASE +
                    "-fx-background-color: rgba(255,255,255,0.10);" +
                    "-fx-border-color: rgba(255,255,255,0.14);";

    private static final String CIRCLE_HOVER =
            CIRCLE_BASE +
                    "-fx-background-color: rgba(255,255,255,0.14);" +
                    "-fx-border-color: rgba(147,197,253,0.50);";

    private static final String CIRCLE_PRESSED =
            CIRCLE_BASE +
                    "-fx-background-color: rgba(255,255,255,0.22);" +
                    "-fx-border-color: rgba(147,197,253,0.60);";

    private final HBox root;

    private final Button infoButton;
    private final Button settingsButton;

    private final List<Node> interactiveNodes;

    public TopBarIcons() {
        root = new HBox(12);
        root.setAlignment(Pos.CENTER_RIGHT);
        root.setPadding(new Insets(6, 0, 6, 0));

        // -------- Info --------
        infoButton = new Button("!");
        prepareInnerButton(infoButton, FONT_UI_BOLD);
        StackPane infoCircle = wrapButtonInCircle(infoButton);
        infoCircle.setOnMouseClicked(e ->
                WindowsUtils.withOwner(infoCircle, owner -> DeviceInfoDialog.show(owner))
        );

        // -------- Settings --------
        settingsButton = new Button("⚙");
        prepareInnerButton(settingsButton, FONT_EMOJI);
        StackPane settingsCircle = wrapButtonInCircle(settingsButton);
        settingsCircle.setOnMouseClicked(e ->
                WindowsUtils.withOwner(settingsCircle, owner -> SettingsDialog.show(owner))
        );

        root.getChildren().addAll(infoCircle, settingsCircle);

        ArrayList<Node> tmp = new ArrayList<>(2);
        tmp.add(infoCircle);
        tmp.add(settingsCircle);
        interactiveNodes = Collections.unmodifiableList(tmp);
    }

    private static void prepareInnerButton(Button b, Font font) {
        b.setFont(font);
        b.setTextFill(Color.WHITE);
        b.setStyle(ICON_BTN);
        b.setFocusTraversable(false);
        b.setMouseTransparent(true);
    }

    private static StackPane wrapButtonInCircle(Button inner) {
        StackPane wrap = new StackPane(inner);
        wrap.setAlignment(Pos.CENTER);
        wrap.setMinSize(CIRCLE_SIZE, CIRCLE_SIZE);
        wrap.setPrefSize(CIRCLE_SIZE, CIRCLE_SIZE);
        wrap.setMaxSize(CIRCLE_SIZE, CIRCLE_SIZE);
        wrap.setPickOnBounds(true);

        wrap.setStyle(CIRCLE_NORMAL);
        wrap.setOnMouseEntered(e -> wrap.setStyle(CIRCLE_HOVER));
        wrap.setOnMouseExited(e -> wrap.setStyle(CIRCLE_NORMAL));
        wrap.setOnMousePressed(e -> wrap.setStyle(CIRCLE_PRESSED));
        wrap.setOnMouseReleased(e -> wrap.setStyle(wrap.isHover() ? CIRCLE_HOVER : CIRCLE_NORMAL));

        return wrap;
    }

    // ---- API ----
    public Node getRoot() { return root; }

    public Node getMaximizeButton() { return null; }

    public List<Node> getInteractiveNodes() { return interactiveNodes; }
}
