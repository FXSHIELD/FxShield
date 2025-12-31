package fx.shield.cs.UI;

import fx.shield.cs.DB.RemoteConfig;
import fx.shield.cs.DB.RemoteConfigService;
import fx.shield.cs.UX.DashBoardPage;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.util.Locale;
import java.util.function.Consumer;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;

public final class PowerModeDialog {

    private PowerModeDialog() {}

    private static final Pattern EAP_BAD =
            Pattern.compile("(?im)^\\s*\\$ErrorActionPreference\\s*=\\s*(SilentlyContinue|Continue|Stop|Inquire)\\s*;?\\s*$");

    private final RemoteConfigService configService = new RemoteConfigService();

    private enum PowerMode { PERFORMANCE, BALANCED, QUIET }

    private static final String PREF_KEY_POWER_MODE = "powerMode";

    /** owner stage (used by Loading/Maintenance dialogs) */
    private Stage primaryStage;

    // ===== Fonts (avoid CSS font-weight bugs) =====
    private static final Font FONT_TITLE = Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 18);
    private static final Font FONT_SUB   = Font.font("Segoe UI", FontWeight.NORMAL, 12);
    private static final Font FONT_BTN   = Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12);

    private static final Font FONT_CARD_TITLE = Font.font("Segoe UI", FontWeight.BOLD, 14);
    private static final Font FONT_CARD_DESC  = Font.font("Segoe UI", FontWeight.NORMAL, 11);
    private static final Font FONT_RADIO      = Font.font("Segoe UI", FontWeight.BOLD, 16);

    // ===== Dialog Styles =====
    private static final String DIALOG_ROOT_STYLE =
            "-fx-background-color: rgba(2,6,23,0.98);" +
                    "-fx-background-radius: 18;" +
                    "-fx-border-radius: 18;" +
                    "-fx-border-color: rgba(255,255,255,0.10);" +
                    "-fx-border-width: 1;" +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.65), 26, 0.22, 0, 12);";

    private static final String BTN_CANCEL_NORMAL =
            "-fx-background-color: transparent;" +
                    "-fx-text-fill: #9ca3af;" +
                    "-fx-border-color: #4b5563;" +
                    "-fx-border-width: 1.2;" +
                    "-fx-background-radius: 999;" +
                    "-fx-border-radius: 999;" +
                    "-fx-padding: 4 18 4 18;" +
                    "-fx-cursor: hand;";

    private static final String BTN_CANCEL_HOVER =
            "-fx-background-color: rgba(255,255,255,0.06);" +
                    "-fx-text-fill: #e5e7eb;" +
                    "-fx-border-color: #6b7280;" +
                    "-fx-border-width: 1.2;" +
                    "-fx-background-radius: 999;" +
                    "-fx-border-radius: 999;" +
                    "-fx-padding: 4 18 4 18;" +
                    "-fx-cursor: hand;";

    private static final String BTN_APPLY_NORMAL =
            "-fx-background-color: #2563eb;" +
                    "-fx-text-fill: white;" +
                    "-fx-background-radius: 999;" +
                    "-fx-padding: 4 22 4 22;" +
                    "-fx-cursor: hand;" +
                    "-fx-effect: dropshadow(gaussian, rgba(37,99,235,0.30), 16, 0.18, 0, 0);";

    private static final String BTN_APPLY_HOVER =
            "-fx-background-color: #1d4ed8;" +
                    "-fx-text-fill: white;" +
                    "-fx-background-radius: 999;" +
                    "-fx-padding: 4 22 4 22;" +
                    "-fx-cursor: hand;" +
                    "-fx-effect: dropshadow(gaussian, rgba(37,99,235,0.40), 18, 0.20, 0, 0);";

    // ===== Cards =====
    private static final String CARD_BASE =
            "-fx-background-color: rgba(255,255,255,0.03);" +
                    "-fx-background-radius: 16;" +
                    "-fx-border-radius: 16;" +
                    "-fx-border-color: rgba(255,255,255,0.06);" +
                    "-fx-border-width: 1;";

    private static final String CARD_HOVER =
            "-fx-background-color: rgba(255,255,255,0.05);" +
                    "-fx-background-radius: 16;" +
                    "-fx-border-radius: 16;" +
                    "-fx-border-color: rgba(255,255,255,0.10);" +
                    "-fx-border-width: 1;";

    private static final String CARD_SELECTED =
            "-fx-background-color: rgba(255,255,255,0.04);" +
                    "-fx-background-radius: 16;" +
                    "-fx-border-radius: 16;" +
                    "-fx-border-color: rgba(59,130,246,0.95);" +
                    "-fx-border-width: 1.6;" +
                    "-fx-effect: dropshadow(gaussian, rgba(59,130,246,0.20), 16, 0.18, 0, 0);";

    private static final String ICON_STYLE =
            "-fx-fill: none; -fx-stroke: rgba(147,197,253,0.90); -fx-stroke-width: 2;" +
                    "-fx-stroke-linecap: round; -fx-stroke-linejoin: round;";

    private static final String ICON_SELECTED_STYLE =
            "-fx-fill: none; -fx-stroke: #3b82f6; -fx-stroke-width: 2.2;" +
                    "-fx-stroke-linecap: round; -fx-stroke-linejoin: round;";

    public static void show(Stage owner) {
        // last saved (we will revert to this if applying fails)
        final PowerMode[] lastGoodMode = { loadSavedMode() };

        // instance for DB/script execution
        PowerModeDialog self = new PowerModeDialog();
        self.primaryStage = owner;

        Stage dialog = new Stage();
        if (owner != null) dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.TRANSPARENT);
        dialog.setResizable(false);
        dialog.setTitle("Power Mode Setting");

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(16, 24, 20, 24));
        root.setStyle(DIALOG_ROOT_STYLE);

        Rectangle clip = new Rectangle();
        clip.setArcWidth(24);
        clip.setArcHeight(24);
        clip.widthProperty().bind(root.widthProperty());
        clip.heightProperty().bind(root.heightProperty());
        root.setClip(clip);

        // ===== Header =====
        Label title = new Label("Power Mode Setting");
        title.setFont(FONT_TITLE);
        title.setTextFill(Color.web("#e5e7eb"));

        Label sub = new Label("Choose the mode you want to use.");
        sub.setFont(FONT_SUB);
        sub.setTextFill(Color.web("#9ca3af"));

        VBox titleBox = new VBox(4, title, sub);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setFont(FONT_BTN);
        cancelBtn.setCancelButton(true);
        cancelBtn.setStyle(BTN_CANCEL_NORMAL);
        cancelBtn.setOnMouseEntered(e -> cancelBtn.setStyle(BTN_CANCEL_HOVER));
        cancelBtn.setOnMouseExited(e -> cancelBtn.setStyle(BTN_CANCEL_NORMAL));
        cancelBtn.setOnAction(e -> dialog.close());

        Button applyBtn = new Button("Apply");
        applyBtn.setFont(FONT_BTN);
        applyBtn.setDefaultButton(true);
        applyBtn.setStyle(BTN_APPLY_NORMAL);
        applyBtn.setOnMouseEntered(e -> applyBtn.setStyle(BTN_APPLY_HOVER));
        applyBtn.setOnMouseExited(e -> applyBtn.setStyle(BTN_APPLY_NORMAL));

        HBox header = new HBox(10, titleBox, spacer, cancelBtn, applyBtn);
        header.setAlignment(Pos.CENTER_LEFT);
        root.setTop(header);

        // ===== Cards =====
        ModeCard performance = new ModeCard(
                "Performance Mode",
                "Boost your computer performance with higher power consumption.",
                PowerMode.PERFORMANCE
        );

        ModeCard balanced = new ModeCard(
                "Balanced Mode",
                "Automatically adjust performance and power usage.",
                PowerMode.BALANCED
        );

        ModeCard quiet = new ModeCard(
                "Quiet Mode",
                "Reduce noise and power usage with lower performance.",
                PowerMode.QUIET
        );

        ModeCard[] cards = { performance, balanced, quiet };

        // unified selector (keyboard + mouse)
        Consumer<ModeCard> select = sel -> setSelected(cards, sel);

        for (ModeCard c : cards) {
            c.setOnSelect(select);

            c.setOnMouseClicked(e -> {
                select.accept(c);
                c.requestFocus();
            });

            c.setOnMouseEntered(e -> {
                if (!c.isSelected()) c.setStyle(CARD_HOVER);
            });

            c.setOnMouseExited(e -> {
                if (!c.isSelected()) c.setStyle(CARD_BASE);
            });
        }

        // initial selection = last saved
        ModeCard initial = findCardByMode(cards, lastGoodMode[0]);
        if (initial != null) select.accept(initial);

        VBox modesBox = new VBox(12, performance, balanced, quiet);
        modesBox.setPadding(new Insets(20, 0, 10, 0));
        root.setCenter(modesBox);

        // Apply: run script -> if OK save+close, else revert selection and DON'T save
        applyBtn.setOnAction(e -> {
            ModeCard selected = getSelected(cards);
            if (selected == null) return;

            PowerMode requested = selected.mode;

            // if same as last good, just close
            if (requested == lastGoodMode[0]) {
                dialog.close();
                return;
            }

            setBusy(cancelBtn, applyBtn, true);

            self.runDbScriptAsync(requested, "power-mode", ok -> {
                // back to UI thread
                Platform.runLater(() -> {
                    setBusy(cancelBtn, applyBtn, false);

                    if (ok) {
                        saveMode(requested);
                        lastGoodMode[0] = requested;
                        dialog.close();
                    } else {
                        // revert UI selection back to previous
                        ModeCard prev = findCardByMode(cards, lastGoodMode[0]);
                        if (prev != null) select.accept(prev);

                        // optional: update subtitle to indicate failure (keeps dialog open)
                        sub.setText("Failed to apply. Reverted to previous mode.");
                    }
                });
            });
        });

        // ===== Scene + keyboard =====
        Scene scene = new Scene(root, 700, 390);
        scene.setFill(Color.TRANSPARENT);

        scene.setOnKeyPressed(k -> {
            if (k.getCode() == KeyCode.ESCAPE) dialog.close();
            if (k.getCode() == KeyCode.ENTER) applyBtn.fire();

            if (k.getCode() == KeyCode.DIGIT1 || k.getCode() == KeyCode.NUMPAD1) select.accept(performance);
            if (k.getCode() == KeyCode.DIGIT2 || k.getCode() == KeyCode.NUMPAD2) select.accept(balanced);
            if (k.getCode() == KeyCode.DIGIT3 || k.getCode() == KeyCode.NUMPAD3) select.accept(quiet);
        });

        dialog.setScene(scene);
        dialog.centerOnScreen();

        // ===== Pop-in animation =====
        root.setOpacity(0);
        root.setScaleX(0.95);
        root.setScaleY(0.95);
        dialog.show();

        ParallelTransition popIn = new ParallelTransition(
                fade(root, 0, 1, 220),
                scale(root, 0.95, 1, 220)
        );
        popIn.setInterpolator(Interpolator.EASE_OUT);
        popIn.play();
    }

    private static void setBusy(Button cancelBtn, Button applyBtn, boolean busy) {
        cancelBtn.setDisable(busy);
        applyBtn.setDisable(busy);
    }

    private static ModeCard findCardByMode(ModeCard[] cards, PowerMode mode) {
        for (ModeCard c : cards) {
            if (c.mode == mode) return c;
        }
        return null;
    }

    private static void setSelected(ModeCard[] cards, ModeCard selected) {
        for (ModeCard c : cards) c.setSelected(c == selected);
    }

    private static ModeCard getSelected(ModeCard[] cards) {
        for (ModeCard c : cards) if (c.isSelected()) return c;
        return null;
    }

    private static Preferences prefs() {
        return Preferences.userNodeForPackage(PowerModeDialog.class);
    }

    private static PowerMode loadSavedMode() {
        String val = prefs().get(PREF_KEY_POWER_MODE, PowerMode.BALANCED.name());
        try { return PowerMode.valueOf(val); }
        catch (IllegalArgumentException ex) { return PowerMode.BALANCED; }
    }

    private static void saveMode(PowerMode mode) {
        prefs().put(PREF_KEY_POWER_MODE, mode.name());
    }

    private static FadeTransition fade(Region node, double from, double to, int ms) {
        FadeTransition ft = new FadeTransition(Duration.millis(ms), node);
        ft.setFromValue(from);
        ft.setToValue(to);
        return ft;
    }

    private static ScaleTransition scale(Region node, double from, double to, int ms) {
        ScaleTransition st = new ScaleTransition(Duration.millis(ms), node);
        st.setFromX(from);
        st.setFromY(from);
        st.setToX(to);
        st.setToY(to);
        return st;
    }

    private static String modeIconPath(PowerMode mode) {
        return switch (mode) {
            case PERFORMANCE -> "M13 10V3L4 14h7v7l9-11h-7z";
            case BALANCED -> "M12 2v20M2 12h20";
            case QUIET -> "M21 12.5c0-4.5-3.5-8-8-8s-8 3.5-8 8 3.5 8 8 8 8-3.5 8-8zm-9 2.5c0 1.7 1.3 3 3 3s3-1.3 3-3-1.3-3-3-3-3 1.3-3 3z";
        };
    }

    private static final class ModeCard extends VBox {
        private final Label radio = new Label("○");
        private final Label title = new Label();
        private final Label desc  = new Label();
        private final SVGPath icon;
        private boolean selected = false;

        private Consumer<ModeCard> onSelect;
        private final PowerMode mode;

        ModeCard(String titleText, String descText, PowerMode mode) {
            this.mode = mode;

            setSpacing(8);
            setPadding(new Insets(14));
            setAlignment(Pos.TOP_LEFT);
            setStyle(CARD_BASE);
            setFocusTraversable(true);

            radio.setFont(FONT_RADIO);
            radio.setTextFill(Color.web("#9ca3af"));

            title.setText(titleText);
            title.setFont(FONT_CARD_TITLE);
            title.setTextFill(Color.web("#e5e7eb"));

            desc.setText(descText);
            desc.setFont(FONT_CARD_DESC);
            desc.setTextFill(Color.web("#9ca3af"));
            desc.setWrapText(true);

            this.icon = new SVGPath();
            this.icon.setContent(modeIconPath(mode));
            this.icon.setStyle(ICON_STYLE);

            StackPane iconPane = new StackPane(icon);
            iconPane.setPrefSize(36, 36);
            iconPane.setMaxSize(36, 36);
            iconPane.setPadding(new Insets(4));

            HBox header = new HBox(12, iconPane, radio, title);
            header.setAlignment(Pos.CENTER_LEFT);

            getChildren().addAll(header, desc);

            setOnKeyPressed(e -> {
                if (e.getCode() == KeyCode.SPACE || e.getCode() == KeyCode.ENTER) {
                    if (onSelect != null) onSelect.accept(this);
                    e.consume();
                }
            });
        }

        void setOnSelect(Consumer<ModeCard> onSelect) {
            this.onSelect = onSelect;
        }

        void setSelected(boolean sel) {
            selected = sel;

            if (sel) {
                radio.setText("●");
                radio.setTextFill(Color.web("#3b82f6"));
                icon.setStyle(ICON_SELECTED_STYLE);
                setStyle(CARD_SELECTED);
            } else {
                radio.setText("○");
                radio.setTextFill(Color.web("#9ca3af"));
                icon.setStyle(ICON_STYLE);
                setStyle(CARD_BASE);
            }
        }

        boolean isSelected() { return selected; }
    }

    // ===== Script handling =====

    private String getScriptFromConfig(RemoteConfig cfg, PowerMode mode) {
        if (cfg == null || mode == null) return null;

        String s = switch (mode) {
            case PERFORMANCE -> cfg.getPerformanceModeScript();
            case BALANCED -> cfg.getBalancedModeScript();
            case QUIET -> cfg.getQuietModeScript();
        };
        return normalizeScript(s);
    }

    private String normalizeScript(String s) {
        if (s == null) return null;

        String t = s.trim();
        if (t.isEmpty()) return null;

        // fix common format mistake
        t = EAP_BAD.matcher(t).replaceAll("$ErrorActionPreference = '$1'");

        String low = t.toLowerCase(Locale.ROOT);
        if (!low.contains("$erroractionpreference")) {
            t = "$ErrorActionPreference = 'SilentlyContinue'\n" + t;
        }
        return t;
    }

    /** Runs DB script async and calls callback with success/fail on background completion */
    private void runDbScriptAsync(PowerMode mode, String logTag, Consumer<Boolean> onFinished) {
        // show loading on FX thread
        LoadingDialog loading = LoadingDialog.show(
                primaryStage,
                "Applying Power Mode",
                "Fetching latest script from server...",
                false
        );

        Thread t = new Thread(() -> {
            boolean ok = false;

            try {
                RemoteConfig cfg = fetchLatestConfigSafe();
                if (cfg == null) {
                    loading.setFailed("Can't reach server. Check your connection.");
                    finish(onFinished, false);
                    return;
                }

                if (cfg.isMaintenance()) {
                    loading.setFailed("Service is under maintenance.");
                    Platform.runLater(() ->
                            MaintenanceDialog.show(primaryStage, cfg, configService::fetchConfig, okCfg -> {})
                    );
                    finish(onFinished, false);
                    return;
                }

                String script = getScriptFromConfig(cfg, mode);
                if (script == null) {
                    loading.setFailed("No script found for " + mode.name() + " mode.");
                    finish(onFinished, false);
                    return;
                }

                loading.setMessageText("Applying " + mode.name().toLowerCase(Locale.ROOT) + " mode...");

                ok = DashBoardPage.runPowerShellSync(script, logTag);

                if (ok) loading.setDone("Power mode applied successfully.");
                else loading.setFailed("Failed to apply power mode.");

            } catch (Exception ex) {
                loading.setFailed("Unexpected error: " + ex.getMessage());
                ok = false;
            }

            finish(onFinished, ok);

        }, "fx.shield.cs-db-" + mode.name());

        t.setDaemon(true);
        t.start();
    }

    private static void finish(Consumer<Boolean> cb, boolean ok) {
        if (cb == null) return;
        // ensure callback runs safely
        Platform.runLater(() -> cb.accept(ok));
    }

    private RemoteConfig fetchLatestConfigSafe() {
        try {
            return configService.fetchConfig();
        } catch (Exception ignored) {
            return null;
        }
    }
}
