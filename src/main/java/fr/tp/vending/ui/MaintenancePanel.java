package fr.tp.vending.ui;

import fr.tp.vending.model.Coin;
import fr.tp.vending.model.Money;
import fr.tp.vending.model.Pricing;
import fr.tp.vending.model.Product;
import fr.tp.vending.model.Slot;
import fr.tp.vending.model.VendingMachine;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * The technician's drawer (Ctrl+M). It reaches the parts of the model the customer side cannot:
 * the time, the card reader's answer, the coin stock, sales, restocking and the products.
 */
class MaintenancePanel extends ScrollPane {

    private final VendingMachine machine;
    private final SimulatedClock clock;
    private final Pricing pricing;
    private final Runnable onChange;
    private final Consumer<String> chooseCode;

    private final RadioButton accept = new RadioButton("Accepter");
    private final RadioButton refuse = new RadioButton("Refuser");
    private final Label lastCharge = value("—");
    private final Label happyHour = value("");
    private final TextField timeField = new TextField("14:00:00");
    private final RadioButton realTime = new RadioButton("Heure réelle");
    private final RadioButton simulatedTime = new RadioButton("Simulée");
    private final Map<Coin, Spinner<Integer>> coinSpinners = new EnumMap<>(Coin.class);
    private final Label revenue = value("");
    private final Label sales = value("");
    private final Label message = new Label();
    private boolean refreshing;

    MaintenancePanel(VendingMachine machine, SimulatedClock clock, Pricing pricing,
                     Runnable onChange, Consumer<String> chooseCode) {
        this.machine = machine;
        this.clock = clock;
        this.pricing = pricing;
        this.onChange = onChange;
        this.chooseCode = chooseCode;

        Label title = new Label("MODE MAINTENANCE");
        title.setFont(Font.font("Inter", FontWeight.EXTRA_BOLD, 16));
        title.setStyle("-fx-text-fill: #1a1a1a; -fx-background-color: #f5c400; -fx-padding: 2 8 2 8; -fx-background-radius: 4;");
        HBox banner = new HBox(title);
        banner.setPadding(new Insets(10, 14, 10, 14));
        banner.setStyle("-fx-background-color: linear-gradient(from 0px 0px to 18px 18px, repeat,"
                + " #f5c400 0%, #f5c400 50%, #222 50%, #222 100%); -fx-background-radius: 6;");

        message.setWrapText(true);
        message.setStyle("-fx-text-fill: #ffcf66; -fx-font-size: 12;");

        VBox content = new VBox(12, banner, clockSection(), cardSection(), coinSection(), salesSection(),
                restockSection(), codeSection(), productSection(), message);
        content.setPadding(new Insets(14));
        content.setStyle("-fx-background-color: #2b2f35;");
        setContent(content);
        setFitToWidth(true);
        setPrefWidth(330);
        setHbarPolicy(ScrollBarPolicy.NEVER);
        setStyle("-fx-background: #2b2f35; -fx-background-color: #2b2f35;");
        refresh();
    }

    /** Sets up a typical state for the preview screenshot. */
    void snapshotDemo() {
        timeField.setText("14:30");
        applyTime();
    }

    boolean cardAccepted() { return accept.isSelected(); }

    void recordCharge(int cents, boolean accepted) {
        lastCharge.setText(Money.format(cents) + (accepted ? " (accepté)" : " (refusé)"));
    }

    /** Reads the model again. Called after every action of the machine. */
    void refresh() {
        refreshing = true;
        happyHour.setText(pricing.isHappyHour() ? "Happy hour : OUI (−20 %)" : "Happy hour : non");
        coinSpinners.forEach((coin, spinner) ->
                spinner.getValueFactory().setValue(machine.coins().getOrDefault(coin, 0)));
        revenue.setText("Chiffre d'affaires : " + Money.format(machine.revenue()));
        StringBuilder sb = new StringBuilder();
        for (Slot slot : machine.slots().values()) {
            int n = machine.salesOf(slot.code());
            if (n > 0) {
                sb.append(slot.code()).append(" × ").append(n).append("   ");
            }
        }
        sales.setText(sb.length() == 0 ? "Aucune vente" : sb.toString().trim());
        refreshing = false;
    }

    // --- sections ---

    private Node clockSection() {
        ToggleGroup group = new ToggleGroup();
        realTime.setToggleGroup(group);
        simulatedTime.setToggleGroup(group);
        realTime.setSelected(true);
        timeField.setPrefColumnCount(7);
        Button apply = button("Régler");
        apply.setOnAction(e -> applyTime());
        realTime.setOnAction(e -> { clock.useRealTime(); changed(); });
        simulatedTime.setOnAction(e -> applyTime());
        return section("Horloge", new HBox(10, realTime, simulatedTime),
                new HBox(8, timeField, apply), happyHour);
    }

    private void applyTime() {
        try {
            clock.simulate(LocalTime.parse(timeField.getText().strip()));
            simulatedTime.setSelected(true);
            say("Heure simulée : " + timeField.getText().strip());
            changed();
        } catch (DateTimeParseException ex) {
            say("Heure invalide (format HH:MM ou HH:MM:SS)");
        }
    }

    private Node cardSection() {
        ToggleGroup group = new ToggleGroup();
        accept.setToggleGroup(group);
        refuse.setToggleGroup(group);
        accept.setSelected(true);
        return section("Lecteur de carte", new HBox(10, accept, refuse), new HBox(6, label("Dernier débit :"), lastCharge));
    }

    private Node coinSection() {
        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(6);
        int i = 0;
        for (Coin coin : Coin.values()) {
            Spinner<Integer> spinner = new Spinner<>(0, 99, 0);
            spinner.setEditable(true);
            spinner.setPrefWidth(76);
            spinner.valueProperty().addListener((obs, old, now) -> {
                if (!refreshing && now != null) {
                    machine.setCoins(coin, now);
                }
            });
            coinSpinners.put(coin, spinner);
            HBox cell = new HBox(6, new CoinView(coin, 0.6), spinner);
            cell.setAlignment(Pos.CENTER_LEFT);
            grid.add(cell, i % 2, i / 2);
            i++;
        }
        return section("Caisse (pièces dans la machine)", grid);
    }

    private Node salesSection() {
        sales.setWrapText(true);
        return section("Ventes", revenue, sales);
    }

    private Node restockSection() {
        ComboBox<String> slot = slotChoice();
        Spinner<Integer> count = new Spinner<>(1, 20, 1);
        count.setPrefWidth(70);
        Button add = button("Ajouter");
        add.setOnAction(e -> attempt(() -> {
            machine.restock(slot.getValue(), count.getValue());
            say(slot.getValue() + " : " + machine.slots().get(slot.getValue()).quantity() + " produits");
        }));
        return section("Réapprovisionner", new HBox(8, slot, count, add));
    }

    private Node codeSection() {
        TextField code = new TextField();
        code.setPromptText("ex. b2");
        code.setPrefColumnCount(6);
        Button go = button("Choisir");
        go.setOnAction(e -> attempt(() -> chooseCode.accept(code.getText())));
        return section("Saisie libre du code", new HBox(8, code, go));
    }

    private Node productSection() {
        ComboBox<String> slot = slotChoice();
        TextField name = new TextField();
        name.setPromptText("Nom");
        name.setPrefColumnCount(8);
        TextField price = new TextField();
        price.setPromptText("Prix (cts)");
        price.setPrefColumnCount(6);
        Button replace = button("Remplacer");
        replace.setOnAction(e -> attempt(() -> {
            Product product = new Product(name.getText(), Integer.parseInt(price.getText().strip()));
            machine.replaceProduct(slot.getValue(), product);
            say(slot.getValue() + " contient maintenant « " + product.name() + " » à " + Money.format(product.price()));
        }));
        return section("Changer un produit", new HBox(8, slot, name), new HBox(8, price, replace));
    }

    // --- helpers ---

    private void attempt(Runnable action) {
        try {
            action.run();
            changed();
        } catch (RuntimeException ex) {
            say("Refusé : " + ex.getClass().getSimpleName() + " — " + ex.getMessage());
        }
    }

    private void changed() {
        refresh();
        onChange.run();
    }

    private void say(String text) { message.setText(text); }

    private ComboBox<String> slotChoice() {
        ComboBox<String> box = new ComboBox<>();
        box.getItems().addAll(machine.slots().keySet());
        box.getSelectionModel().selectFirst();
        box.setPrefWidth(72);
        return box;
    }

    private static VBox section(String title, Node... children) {
        Label t = new Label(title.toUpperCase());
        t.setFont(Font.font("Inter", FontWeight.BOLD, 11));
        t.setStyle("-fx-text-fill: #f5c400;");
        VBox box = new VBox(8, t);
        box.getChildren().addAll(children);
        box.setPadding(new Insets(10));
        box.setStyle("-fx-background-color: #363b43; -fx-background-radius: 6;");
        for (Node n : children) {
            n.setStyle(n.getStyle() + "-fx-text-fill: #e6e6e6;");
        }
        box.lookupAll(".radio-button").forEach(n -> n.setStyle("-fx-text-fill: #e6e6e6;"));
        return box;
    }

    private static Label label(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: #b8bec7;");
        return l;
    }

    private static Label value(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: #e6e6e6; -fx-font-weight: bold;");
        return l;
    }

    private static Button button(String text) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color: #f5c400; -fx-text-fill: #1a1a1a; -fx-font-weight: bold; -fx-background-radius: 4;");
        b.setMinWidth(Region.USE_PREF_SIZE);
        return b;
    }
}
