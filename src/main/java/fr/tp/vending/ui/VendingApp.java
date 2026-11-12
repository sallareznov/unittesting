package fr.tp.vending.ui;

import fr.tp.vending.model.CardTerminal;
import fr.tp.vending.model.ChangeCalculator;
import fr.tp.vending.model.Coin;
import fr.tp.vending.model.CoinDispenser;
import fr.tp.vending.model.Display;
import fr.tp.vending.model.Pricing;
import fr.tp.vending.model.Product;
import fr.tp.vending.model.ProductDispenser;
import fr.tp.vending.model.Slot;
import fr.tp.vending.model.VendingMachine;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * The machine on screen. The window plays the part of the hardware: it implements
 * Display, CoinDispenser, ProductDispenser and CardTerminal for the model, exactly as mocks do in the tests.
 */
public class VendingApp extends Application implements Display, CoinDispenser, ProductDispenser, CardTerminal {

    private static final String[][] CATALOGUE = {
        {"Eau", "90"}, {"Cola", "130"}, {"Orange", "130"}, {"Thé glacé", "150"},
        {"Eau gazeuse", "100"}, {"Cola zéro", "130"}, {"Citron", "130"}, {"Café froid", "180"},
        {"Chips", "120"}, {"Bretzels", "110"}, {"Cookies", "160"}, {"Popcorn", "140"},
        {"Barre choco", "140"}, {"Gaufre", "150"}, {"Barre céréales", "120"}, {"Nougat", "170"},
    };

    private final Label line1 = new Label();
    private final Label line2 = new Label();
    private final Label codeLabel = new Label("--");
    private final FlowPane changeTray = new FlowPane(4, 4);
    private final Canvas pickup = new Canvas(330, 80);
    private final Map<String, SlotView> slotViews = new HashMap<>();
    private VendingMachine machine;
    private final SimulatedClock clock = new SimulatedClock();
    private final Pricing pricing = new Pricing(clock);
    private MaintenancePanel maintenance;
    private String typed = "";
    private final javafx.scene.control.ToggleButton cardButton = new javafx.scene.control.ToggleButton("Payer par carte");

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        List<Slot> slots = new ArrayList<>();
        for (int i = 0; i < CATALOGUE.length; i++) {
            String code = "" + (char) ('A' + i / 4) + (i % 4 + 1);
            int quantity = code.equals("C3") ? 0 : 3 + i % 5;
            slots.add(new Slot(code, new Product(CATALOGUE[i][0], Integer.parseInt(CATALOGUE[i][1])), quantity));
        }
        Map<Coin, Integer> float_ = new EnumMap<>(Coin.class);
        for (Coin c : Coin.values()) {
            float_.put(c, 10);
        }
        StackPane front = buildRoot(slots);
        machine = new VendingMachine(slots, float_, new ChangeCalculator(), pricing, this, this, this, this);
        maintenance = new MaintenancePanel(machine, clock, pricing, this::refreshSlots, this::choose);
        maintenance.getStylesheets().add("data:text/css,.radio-button,.label{-fx-text-fill:%23e6e6e6;}");
        boolean open = Boolean.getBoolean("maintenance") || System.getProperty("snapshot") != null;
        maintenance.setVisible(open);
        maintenance.setManaged(open);
        HBox.setHgrow(front, Priority.ALWAYS);
        HBox outer = new HBox(front, maintenance);
        Scene scene = new Scene(outer, open ? 1330 : 1000, 860);
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.M, KeyCombination.SHORTCUT_DOWN), () -> {
            boolean show = !maintenance.isVisible();
            maintenance.setVisible(show);
            maintenance.setManaged(show);
            maintenance.refresh();
            stage.setWidth(stage.getWidth() + (show ? 330 : -330));
        });
        stage.setTitle("Distributeur");
        stage.setScene(scene);
        stage.show();

        String snapshot = System.getProperty("snapshot");
        if (snapshot != null) {
            maintenance.snapshotDemo();
            machine.insert(Coin.TWO_EUROS);
            press("B");
            press("2");
            PauseTransition wait = new PauseTransition(Duration.millis(800));
            wait.setOnFinished(e -> {
                Snapshot.save(scene, snapshot);
                Platform.exit();
            });
            wait.play();
        }
    }

    private StackPane buildRoot(List<Slot> slots) {
        // --- glass front with the products ---
        GridPane grid = new GridPane();
        grid.setHgap(6);
        grid.setVgap(4);
        for (int i = 0; i < slots.size(); i++) {
            SlotView view = new SlotView(slots.get(i));
            slotViews.put(slots.get(i).code(), view);
            grid.add(view, i % 4, i / 4);
        }
        grid.setPadding(new Insets(14));
        grid.setStyle("-fx-background-color: linear-gradient(to bottom, #2c3440, #161b22);"
                + "-fx-background-radius: 8; -fx-border-color: #5a6470; -fx-border-width: 4; -fx-border-radius: 8;");
        Region glare = new Region();
        glare.setMouseTransparent(true);
        glare.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 100%,"
                + " rgba(255,255,255,0.10) 0%, rgba(255,255,255,0.02) 40%, rgba(255,255,255,0.0) 41%,"
                + " rgba(255,255,255,0.06) 60%, rgba(255,255,255,0.0) 61%); -fx-background-radius: 8;");
        StackPane glass = new StackPane(grid, glare);

        // pickup flap below the glass
        Label pushLabel = new Label("POUSSER");
        pushLabel.setStyle("-fx-text-fill: #8b939e; -fx-font-size: 11; -fx-font-weight: bold;");
        StackPane flap = new StackPane(pickup, pushLabel);
        StackPane.setAlignment(pushLabel, Pos.TOP_CENTER);
        flap.setPadding(new Insets(4));
        flap.setStyle("-fx-background-color: #0d1014; -fx-background-radius: 6; -fx-border-color: #3a414b;"
                + "-fx-border-width: 3; -fx-border-radius: 6;");
        flap.setMaxWidth(360);
        VBox left = new VBox(18, glass, flap);
        left.setAlignment(Pos.TOP_CENTER);

        // --- control panel ---
        Label brand = new Label("SNACK & DRINK");
        brand.setFont(Font.font("Inter", FontWeight.EXTRA_BOLD, 20));
        brand.setTextFill(Color.WHITE);
        for (Label l : List.of(line1, line2)) {
            l.setFont(Font.font("DejaVu Sans Mono", FontWeight.BOLD, 15));
            l.setTextFill(Color.web("#62f5c8"));
        }
        codeLabel.setFont(Font.font("DejaVu Sans Mono", FontWeight.BOLD, 13));
        codeLabel.setTextFill(Color.web("#3aa58a"));
        VBox screen = new VBox(4, line1, line2, codeLabel);
        screen.setPadding(new Insets(12));
        screen.setPrefHeight(96);
        screen.setStyle("-fx-background-color: #04241d; -fx-background-radius: 6; -fx-border-color: #111;"
                + "-fx-border-width: 3; -fx-border-radius: 6;");

        GridPane keypad = new GridPane();
        keypad.setHgap(8);
        keypad.setVgap(8);
        String[] keys = {"A", "B", "C", "D", "1", "2", "3", "4"};
        for (int i = 0; i < keys.length; i++) {
            String k = keys[i];
            Button b = new Button(k);
            b.setPrefSize(52, 42);
            b.setStyle("-fx-background-color: linear-gradient(#f2f2f2, #c8ccd2); -fx-background-radius: 6;"
                    + "-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #222;");
            b.setOnAction(e -> press(k));
            keypad.add(b, i % 4, i / 4);
        }

        Label coinTitle = small("INSÉRER DES PIÈCES");
        FlowPane coinButtons = new FlowPane(8, 8);
        for (Coin c : Coin.values()) {
            CoinView v = new CoinView(c, 1);
            v.setOnMouseClicked(e -> machine.insert(c));
            v.setStyle("-fx-cursor: hand;");
            coinButtons.getChildren().add(v);
        }
        coinButtons.setPrefWrapLength(240);

        Region slot = new Region();
        slot.setPrefSize(10, 46);
        slot.setMaxSize(10, 46);
        slot.setStyle("-fx-background-color: #000; -fx-background-radius: 4; -fx-border-color: #888; -fx-border-radius: 4;");
        HBox coinRow = new HBox(16, coinButtons, slot);
        coinRow.setAlignment(Pos.CENTER_LEFT);

        Button cancel = new Button("Rendre la monnaie");
        cancel.setMaxWidth(Double.MAX_VALUE);
        cancel.setStyle("-fx-background-color: #c62828; -fx-text-fill: white; -fx-font-weight: bold;"
                + "-fx-font-size: 14; -fx-background-radius: 20; -fx-padding: 9 16 9 16;");
        cancel.setOnAction(e -> {
            changeTray.getChildren().clear();
            machine.cancel();
        });

        cardButton.setMaxWidth(Double.MAX_VALUE);
        cardButton.setStyle("-fx-background-color: #1e88e5; -fx-text-fill: white; -fx-font-weight: bold;"
                + "-fx-font-size: 14; -fx-background-radius: 20; -fx-padding: 9 16 9 16;");
        cardButton.setOnAction(e -> {
            if (cardButton.isSelected()) {
                show("Carte bancaire", "Tapez le code");
            }
        });

        Label trayTitle = small("MONNAIE");
        changeTray.setPadding(new Insets(10));
        changeTray.setPrefHeight(70);
        changeTray.setAlignment(Pos.CENTER_LEFT);
        changeTray.setStyle("-fx-background-color: #0d1014; -fx-background-radius: 6 6 22 22; -fx-border-color: #3a414b;"
                + "-fx-border-width: 3; -fx-border-radius: 6 6 22 22;");
        changeTray.setOnMouseClicked(e -> changeTray.getChildren().clear());

        Region grow = new Region();
        VBox.setVgrow(grow, Priority.ALWAYS);
        VBox panel = new VBox(14, brand, screen, small("CHOISIR UN PRODUIT"), keypad, coinTitle, coinRow, cancel, cardButton,
                grow, trayTitle, changeTray);
        panel.setPadding(new Insets(18));
        panel.setPrefWidth(290);
        panel.setStyle("-fx-background-color: linear-gradient(#3a414b, #252a31); -fx-background-radius: 8;");

        HBox cabinet = new HBox(18, left, panel);
        cabinet.setPadding(new Insets(20));
        cabinet.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        cabinet.setStyle("-fx-background-color: linear-gradient(to right, #9e1b1b, #c62828 40%, #8e1515);"
                + "-fx-background-radius: 18; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 30, 0, 0, 10);");

        StackPane root = new StackPane(cabinet);
        root.setStyle("-fx-background-color: linear-gradient(#e9e4dc, #cfc7ba);");
        return root;
    }

    private void press(String key) {
        typed = Character.isLetter(key.charAt(0)) ? key : typed + key;
        codeLabel.setText("Choix : " + typed);
        if (typed.length() == 2) {
            choose(typed);
            typed = "";
        }
    }

    /** Buys the product with this code, with the coins or by card. */
    private void choose(String code) {
        if (cardButton.isSelected()) {
            machine.payByCard(code);
            cardButton.setSelected(false);
        } else {
            machine.select(code);
        }
    }

    private void refreshSlots() {
        slotViews.values().forEach(SlotView::refresh);
    }

    private static Label small(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Inter", FontWeight.BOLD, 11));
        l.setTextFill(Color.web("#c3cad3"));
        return l;
    }

    // --- the "hardware" seen by the model ---

    @Override
    public void show(String a, String b) {
        line1.setText(a);
        line2.setText(b);
        if (maintenance != null) {
            // after the machine has finished its action (sales are recorded after the message)
            Platform.runLater(() -> {
                maintenance.refresh();
                refreshSlots();
            });
        }
    }

    @Override
    public void dispense(Coin coin, int count) {
        for (int i = 0; i < count; i++) {
            changeTray.getChildren().add(new CoinView(coin, 0.9));
        }
    }

    @Override
    public boolean charge(int cents) {
        boolean accepted = maintenance == null || maintenance.cardAccepted();
        if (maintenance != null) {
            maintenance.recordCharge(cents, accepted);
        }
        return accepted;
    }

    @Override
    public void release(Slot slot) {
        slotViews.get(slot.code()).refresh();
        var g = pickup.getGraphicsContext2D();
        g.clearRect(0, 0, pickup.getWidth(), pickup.getHeight());
        g.save();
        g.translate(pickup.getWidth() / 2 - 10, 60);
        g.rotate(-80);
        ProductPainter.paint(g, Look.of(slot.product().name()), slot.product().name(), 0, 34, 1.05);
        g.restore();
    }
}
