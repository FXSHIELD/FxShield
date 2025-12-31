// FILE: src/fxShield/DISK/PhysicalDiskSwitcher.java
package fx.shield.cs.DISK;

import fx.shield.cs.UI.StyleConstants;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.util.function.IntConsumer;

public final class  PhysicalDiskSwitcher {

    // (we will generate pill style dynamically to keep padding/border consistent per size)
    // ===== Accent from app palette (بدّلها لـ COLOR_AMBER إذا بدك ذهبي) =====
    private static final int[] ACCENT_RGB = hexToRgb(StyleConstants.COLOR_PRIMARY);

    private static String rgba(int r, int g, int b, double a) {
        return String.format(java.util.Locale.US, "rgba(%d,%d,%d,%.3f)", r, g, b, a);
    }
    private static String accent(double a) {
        return rgba(ACCENT_RGB[0], ACCENT_RGB[1], ACCENT_RGB[2], a);
    }
    private static int[] hexToRgb(String hex) {
        if (hex == null) return new int[]{167, 139, 250}; // fallback قريب من primary
        String h = hex.trim();
        if (h.startsWith("#")) h = h.substring(1);
        if (h.length() != 6) return new int[]{167, 139, 250};
        int r = Integer.parseInt(h.substring(0, 2), 16);
        int g = Integer.parseInt(h.substring(2, 4), 16);
        int b = Integer.parseInt(h.substring(4, 6), 16);
        return new int[]{r, g, b};
    }

    // ====== Row styles (مثل search-result-item) ======
    private static final String ROW_NORMAL =
            "-fx-background-color: rgba(11,18,36,0.55);" +
                    "-fx-background-radius: 12;" +
                    "-fx-padding: 12 12;" +
                    "-fx-cursor: hand;" +
                    "-fx-border-color: rgba(255,255,255,0.06);" +
                    "-fx-border-width: 1;" +
                    "-fx-border-radius: 12;";

    private static final String ROW_HOVER =
            "-fx-background-color: rgba(15,23,42,0.75);" +
                    "-fx-background-radius: 12;" +
                    "-fx-padding: 12 12;" +
                    "-fx-cursor: hand;" +
                    "-fx-border-color: " + accent(0.35) + ";" +
                    "-fx-border-width: 1;" +
                    "-fx-border-radius: 12;" +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.35), 26, 0.25, 0, 10);";

    private static final String ROW_SELECTED =
            "-fx-background-color: linear-gradient(to right, " + accent(0.22) + ", " + accent(0.12) + ");" +
                    "-fx-background-radius: 12;" +
                    "-fx-padding: 12 12;" +
                    "-fx-cursor: hand;" +
                    "-fx-border-color: " + accent(0.60) + ";" +
                    "-fx-border-width: 1.2;" +
                    "-fx-border-radius: 12;" +
                    "-fx-effect: dropshadow(gaussian, " + accent(0.22) + ", 14, 0.25, 0, 0);";

    private static final String TXT =
            "-fx-text-fill: " + StyleConstants.COLOR_TEXT_WHITE + ";" +
                    "-fx-font-size: 13;" +
                    "-fx-font-weight: 700;";

    private static final String CHECK_MARK =
            "-fx-text-fill: " + StyleConstants.COLOR_PRIMARY + ";" +
                    "-fx-font-size: 15;" +
                    "-fx-font-weight: 900;";

    private static final String MENU_CARD =
            "-fx-background-color: rgba(15, 23, 42, 0.96);" +
                    "-fx-background-radius: 16;" +
                    "-fx-padding: 6;" +
                    "-fx-border-color: rgba(255, 255, 255, 0.08);" +
                    "-fx-border-width: 1;" +
                    "-fx-border-radius: 16;" +
                    "-fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.5), 20, 0, 0, 10);";
    private static final String TRANSPARENT_OVERRIDE =
            "-fx-background-color: transparent;" +
                    "-fx-background-insets: 0;" +
                    "-fx-padding: 0;" +
                    "-fx-border-width: 0;" +
                    "-fx-border-color: transparent;";
    private final HBox root;
    private final Button pill;
    private final HBox pillContent;
    private final ContextMenu menu;
    private final Label title;
    private final Label arrow;

    /* ================= iOS STYLES ================= */
    private int count;
    private int selected; // -1 when no disks
    private IntConsumer onSelect = i -> {
    };
    // responsive flags (do NOT overwrite each other)
    private boolean compactWanted = false;
    private boolean veryCompactWanted = false;
    // hover/menu state to keep pill style consistent (no padding jump)
    private boolean hover = false;
    private boolean menuShowing = false;

    /* ================================================= */

    public PhysicalDiskSwitcher(int initialCount, int initialIndex, IntConsumer onSelect) {
        this.count = Math.max(0, initialCount);
        this.selected = (count > 0) ? clamp(initialIndex, 0, count - 1) : -1;
        if (onSelect != null) this.onSelect = onSelect;

        title = new Label("Disks");
        title.setFont(Font.font("Segoe UI", 13));
        title.setStyle("-fx-text-fill: rgba(240,248,255,1); -fx-font-weight: 600;");

        arrow = new Label("▾");
        arrow.setFont(Font.font("Segoe UI", 12));
        arrow.setStyle("-fx-text-fill: rgba(59,130,246,0.8); -fx-font-weight: 600;");

        pillContent = new HBox(8, title, arrow);
        pillContent.setAlignment(Pos.CENTER);

        menu = new ContextMenu();
        menu.setAutoHide(true);
        menu.setHideOnEscape(true);

        pill = new Button();
        pill.setGraphic(pillContent);
        pill.setFocusTraversable(false);

        // Pill hover/press – keep style consistent per size
        pill.setOnMouseEntered(e -> {
            hover = true;
            applyPillStyle();
        });
        pill.setOnMouseExited(e -> {
            hover = false;
            applyPillStyle();
        });

        pill.setOnMousePressed(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(60), pill);
            st.setToX(0.96);
            st.setToY(0.96);
            st.play();
        });
        pill.setOnMouseReleased(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(80), pill);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });

        menu.setOnShowing(e -> {
            menuShowing = true;
            arrow.setText("▴");
            applyPillStyle();
        });

        menu.setOnShown(e -> Platform.runLater(() -> applySkinStyles(menu)));

        menu.setOnHidden(e -> {
            menuShowing = false;
            arrow.setText("▾");
            applyPillStyle();
        });

        pill.setOnAction(e -> {
            if (!menu.isShowing()) menu.show(pill, Side.BOTTOM, 0, 10);
            else menu.hide();
        });

        root = new HBox(pill);
        root.setAlignment(Pos.CENTER_LEFT);

        // ✅ important: enable key handling
        root.setFocusTraversable(true);
        root.setOnMouseClicked(e -> root.requestFocus());

        root.setOnKeyPressed(e -> {
            if (count <= 0 || selected < 0) {
                if (e.getCode() == KeyCode.ENTER || e.getCode() == KeyCode.SPACE) pill.fire();
                if (e.getCode() == KeyCode.ESCAPE) menu.hide();
                return;
            }

            if (e.getCode() == KeyCode.LEFT) {
                setSelectedIndex(Math.max(0, selected - 1));
                e.consume();
            } else if (e.getCode() == KeyCode.RIGHT) {
                setSelectedIndex(Math.min(count - 1, selected + 1));
                e.consume();
            } else if (e.getCode().isDigitKey()) {
                int idx = digitToIndex(e.getCode());
                if (idx >= 0 && idx < count) setSelectedIndex(idx);
                e.consume();
            } else if (e.getCode() == KeyCode.ENTER || e.getCode() == KeyCode.SPACE) {
                pill.fire();
                e.consume();
            } else if (e.getCode() == KeyCode.ESCAPE) {
                menu.hide();
                e.consume();
            }
        });

        applyResponsive();     // sets fonts/padding + pill style
        rebuildMenu();
    }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    private static int digitToIndex(KeyCode code) {
        return switch (code) {
            case DIGIT0, NUMPAD0 -> 0;
            case DIGIT1, NUMPAD1 -> 1;
            case DIGIT2, NUMPAD2 -> 2;
            case DIGIT3, NUMPAD3 -> 3;
            case DIGIT4, NUMPAD4 -> 4;
            case DIGIT5, NUMPAD5 -> 5;
            case DIGIT6, NUMPAD6 -> 6;
            case DIGIT7, NUMPAD7 -> 7;
            case DIGIT8, NUMPAD8 -> 8;
            case DIGIT9, NUMPAD9 -> 9;
            default -> -1;
        };
    }

    private void applySkinStyles(ContextMenu menu) {
        Node skin = (menu.getSkin() != null) ? menu.getSkin().getNode() : null;
        if (skin == null) return;

        try {
            skin.setStyle(MENU_CARD);

            for (Node n : skin.lookupAll(
                    ".root, .popup-container, .context-menu, .menu, .menu-item, .menu-item-container," +
                            ".scroll-pane, .viewport, .content, .corner, .list-view, .context-menu-container, .custom-menu-item"
            )) {
                try {
                    n.setStyle(TRANSPARENT_OVERRIDE);
                } catch (RuntimeException ignored) {
                }
            }
        } catch (RuntimeException ignored) {
        }

        skin.setOpacity(0);
        skin.setTranslateY(-10);
        skin.setScaleX(0.97);
        skin.setScaleY(0.97);

        FadeTransition ft = new FadeTransition(Duration.millis(120), skin);
        ft.setFromValue(0);
        ft.setToValue(1);

        TranslateTransition tt = new TranslateTransition(Duration.millis(150), skin);
        tt.setFromY(-10);
        tt.setToY(0);

        ScaleTransition st = new ScaleTransition(Duration.millis(150), skin);
        st.setFromX(0.97);
        st.setFromY(0.97);
        st.setToX(1.0);
        st.setToY(1.0);

        new ParallelTransition(ft, tt, st).play();
    }

    private void rebuildMenu() {
        menu.setStyle(MENU_CARD);
        menu.getItems().clear();

        if (count <= 0) {
            MenuItem none = new MenuItem("No disks");
            none.setDisable(true);
            menu.getItems().add(none);
            return;
        }

        for (int i = 0; i < count; i++) {
            final int idx = i;

            // ✅ UI label 1-based (internal index 0-based)
            Label txt = new Label("Disk " + (idx + 1));
            txt.setStyle(TXT);
            txt.setMouseTransparent(true);
            txt.setFocusTraversable(false);

            Label check = new Label("✓");
            check.setStyle(CHECK_MARK);
            check.setOpacity(idx == selected ? 1.0 : 0.0);
            check.setMouseTransparent(true);
            check.setFocusTraversable(false);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            spacer.setMouseTransparent(true);

            HBox row = new HBox(10, txt, spacer, check);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setMinWidth(220);
            row.setMaxWidth(Double.MAX_VALUE);
            row.setStyle(idx == selected ? ROW_SELECTED : ROW_NORMAL);
            row.setPickOnBounds(true);

            CustomMenuItem item = new CustomMenuItem(row, true);
            item.getStyleClass().add("custom-menu-item");
            item.setHideOnClick(true);

            // بدل CustomMenuItem(row, true) مباشرة
            StackPane wrap = new StackPane(row);
            wrap.setPadding(new Insets(6));
            wrap.setStyle("-fx-background-color: transparent;");
            wrap.setPickOnBounds(true);
            wrap.setMinWidth(232);
            wrap.setMaxWidth(Double.MAX_VALUE);

// hover/exit على الـ wrap (مو row) + translateY بدل scale
            wrap.setOnMouseEntered(e -> {
                if (idx != selected) {
                    row.setStyle(ROW_HOVER);
                    check.setOpacity(0.4);
                }
                lift(wrap, -2);
            });

            wrap.setOnMouseExited(e -> {
                if (idx != selected) {
                    row.setStyle(ROW_NORMAL);
                    check.setOpacity(0.0);
                } else {
                    row.setStyle(ROW_SELECTED);
                    check.setOpacity(1.0);
                }
                lift(wrap, 0);
            });

            wrap.setOnMousePressed(e -> lift(wrap, -1));
            wrap.setOnMouseReleased(e -> lift(wrap, wrap.isHover() ? -2 : 0));


            row.setOnMouseEntered(e -> {
                if (idx != selected) {
                    row.setStyle(ROW_HOVER);
                    check.setOpacity(0.4);
                }
                lift(row, -2); // نفس translateY(-2px)
            });

            row.setOnMouseExited(e -> {
                if (idx != selected) {
                    row.setStyle(ROW_NORMAL);
                    check.setOpacity(0.0);
                } else {
                    row.setStyle(ROW_SELECTED);
                    check.setOpacity(1.0);
                }
                lift(row, 0);
            });

            row.setOnMousePressed(e -> {
                ScaleTransition st2 = new ScaleTransition(Duration.millis(50), row);
                st2.setToX(0.98);
                st2.setToY(0.98);
                st2.play();
            });
            row.setOnMouseReleased(e -> {
                ScaleTransition st2 = new ScaleTransition(Duration.millis(50), row);
                st2.setToX(1.0);
                st2.setToY(1.0);
                st2.play();
            });

            item.setOnAction(e -> {
                setSelectedIndex(idx);
                menu.hide();
            });

            menu.getItems().add(item);
        }
    }

    private void refresh() {
        rebuildMenu();
        if (count > 0 && selected >= 0) {
            try {
                onSelect.accept(selected);
            } catch (Exception ignored) {
            }
        }
    }

    private void applyResponsive() {
        boolean effectiveCompact = veryCompactWanted || compactWanted;

        if (effectiveCompact) {
            title.setFont(Font.font("Segoe UI", 11));
            arrow.setFont(Font.font("Segoe UI", 10));
            pillContent.setSpacing(4);
        } else {
            title.setFont(Font.font("Segoe UI", 13));
            arrow.setFont(Font.font("Segoe UI", 12));
            pillContent.setSpacing(8);
        }

        if (veryCompactWanted) {
            title.setVisible(false);
            title.setManaged(false);
        } else {
            title.setVisible(true);
            title.setManaged(true);
        }

        applyPillStyle();
    }

    private void applyPillStyle() {
        boolean effectiveCompact = veryCompactWanted || compactWanted;
        boolean on = hover || menuShowing;

        String padding = veryCompactWanted ? "3 6" : (effectiveCompact ? "3 8" : "6 12");
        String borderW = effectiveCompact ? "1" : "1.5";

        String base =
                "-fx-background-radius: 999;" +
                        "-fx-padding: " + padding + ";" +
                        "-fx-background-color: rgba(59,130,246," + (on ? "0.18" : "0.12") + ");" +
                        "-fx-border-color: rgba(59,130,246," + (on ? "0.40" : "0.25") + ");" +
                        "-fx-border-width: " + borderW + ";" +
                        "-fx-border-radius: 999;";

        if (on) {
            base += "-fx-effect: dropshadow(gaussian, rgba(59,130,246,0.25), 8, 0.3, 0, 0);";
        }

        pill.setStyle(base);
    }

    /* ================= API ================= */

    public HBox getRoot() {
        return root;
    }

    public void setOnSelect(IntConsumer c) {
        this.onSelect = (c != null) ? c : i -> {
        };
    }

    public void setCount(int c) {
        count = Math.max(0, c);
        selected = (count > 0) ? 0 : -1;
        refresh();
    }

    public void setCompact(boolean compact) {
        if (this.compactWanted == compact) return;
        this.compactWanted = compact;
        applyResponsive();
    }

    public void setVeryCompact(boolean veryCompact) {
        if (this.veryCompactWanted == veryCompact) return;
        this.veryCompactWanted = veryCompact;
        applyResponsive();
    }

    public int getSelectedIndex() {
        return selected;
    }

    public void setSelectedIndex(int idx) {
        if (count <= 0) {
            selected = -1;
            rebuildMenu();
            return;
        }
        selected = clamp(idx, 0, count - 1);
        refresh();
    }

    private static void lift(Node node, double toY) {
        Object prev = node.getProperties().get("liftTT");
        if (prev instanceof TranslateTransition tt) tt.stop();

        TranslateTransition tt = new TranslateTransition(Duration.millis(180), node);
        tt.setToY(toY);
        tt.setInterpolator(Interpolator.EASE_OUT);
        node.getProperties().put("liftTT", tt);
        tt.play();
    }

}
