package fr.tp.checkout.ui;

import fr.tp.checkout.model.Checkout;
import fr.tp.checkout.model.Coupon;
import fr.tp.checkout.model.Discount;
import fr.tp.checkout.model.InMemoryCatalogue;
import fr.tp.checkout.model.Line;
import fr.tp.checkout.model.Money;
import fr.tp.checkout.model.PaymentTerminal;
import fr.tp.checkout.model.Product;
import fr.tp.checkout.model.Receipt;
import fr.tp.checkout.model.ReceiptPrinter;
import fr.tp.checkout.model.Scale;
import fr.tp.checkout.model.SecondHalfPrice;
import fr.tp.checkout.model.ThreeForTwo;
import fr.tp.checkout.model.ThresholdDiscount;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * The self-checkout kiosk. The window plays the hardware: it is the Scale, the PaymentTerminal
 * and the ReceiptPrinter of the model, as mocks are in the tests.
 */
public class CheckoutApp extends Application implements Scale, PaymentTerminal, ReceiptPrinter {

    private static final int THRESHOLD = 5000;
    private static final InMemoryCatalogue CATALOGUE = new InMemoryCatalogue(List.of(
        new Product("Y01", "Yaourts nature ×4", 180, false),
        new Product("C01", "Chocolat noir 70 %", 245, false),
        new Product("B01", "Bananes", 199, true),
        new Product("A01", "Pommes Gala", 280, true),
        new Product("L01", "Lait demi-écrémé 1 L", 115, false),
        new Product("P01", "Baguette tradition", 110, false),
        new Product("E01", "Eau minérale 6 × 1,5 L", 320, false),
        new Product("F01", "Comté 18 mois 250 g", 490, false)));
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.FRENCH);

    private final ZoneId zone = ZoneId.systemDefault();
    private LocalDate today = LocalDate.now();
    private final Clock clock = new Clock() {
        @Override public ZoneId getZone() { return zone; }
        @Override public Clock withZone(ZoneId z) { return this; }
        @Override public Instant instant() { return today.atTime(java.time.LocalTime.now()).atZone(zone).toInstant(); }
    };

    private Checkout checkout;
    private int accountPoints = 1234;
    private Line selected;

    private final VBox receipt = new VBox(2);
    private final Label subtotal = new Label();
    private final Label savings = new Label();
    private final Label total = new Label();
    private final Label message = new Label();
    private final Label loyalty = new Label();
    private final Button pay = new Button();
    private final Spinner<Integer> scaleGrams = new Spinner<>(0, 5000, 1268, 10);
    private final RadioButton accept = new RadioButton("Accepter");
    private final RadioButton refuse = new RadioButton("Refuser");
    private final ComboBox<Coupon> couponChoice = new ComboBox<>();
    private final StackPane centreStack = new StackPane();
    private final VBox printedReceipt = new VBox(4);

    public static void main(String[] args) {
        launch(args);
    }

    // --- the hardware seen by the model ---

    @Override
    public int grams() { return scaleGrams.getValue(); }

    @Override
    public boolean charge(int cents) { return accept.isSelected(); }

    @Override
    public void print(Receipt r) {
        printedReceipt.getChildren().clear();
        printedReceipt.getChildren().add(mono("    MARCHÉ FRAIS — caisse 4", true));
        printedReceipt.getChildren().add(mono("    " + DateTimeFormatter.ofPattern("dd/MM/yyyy").format(today), false));
        printedReceipt.getChildren().add(mono("--------------------------------", false));
        for (Line l : r.lines()) {
            String qty = l.product().byWeight() ? kilos(l.grams()) : "x" + l.quantity();
            printedReceipt.getChildren().add(mono(pad(l.product().name(), 20) + pad(qty, 6) + leftPad(Money.format(l.amount()), 8), false));
        }
        for (Discount d : r.discounts()) {
            printedReceipt.getChildren().add(mono(pad(d.label(), 24) + leftPad(Money.format(-d.amount()), 10), false));
        }
        printedReceipt.getChildren().add(mono("--------------------------------", false));
        printedReceipt.getChildren().add(mono(pad("TOTAL", 22) + leftPad(Money.format(r.total()), 12), true));
        printedReceipt.getChildren().add(mono("Points fidélité gagnés : " + r.pointsEarned(), false));
        printedReceipt.getChildren().add(mono("       Merci et à bientôt !", false));
    }

    @Override
    public void start(Stage stage) {
        newCustomer();
        Scene scene = new Scene(buildRoot(), 1280, 820);
        stage.setTitle("Caisse automatique");
        stage.setScene(scene);
        stage.show();
        refresh();

        String snapshot = System.getProperty("snapshot");
        if (snapshot != null) {
            for (String code : List.of("Y01", "C01", "Y01", "C01", "B01", "L01", "P01", "Y01", "E01", "F01")) {
                checkout.scan(code);
            }
            checkout.scanLoyaltyCard(accountPoints);
            refresh();
            PauseTransition wait = new PauseTransition(Duration.millis(800));
            wait.setOnFinished(e -> {
                Snapshot.save(scene, snapshot);
                Platform.exit();
            });
            wait.play();
        }
    }

    private void newCustomer() {
        checkout = new Checkout(CATALOGUE, List.of(new ThreeForTwo("Y01"), new SecondHalfPrice("C01"),
                new ThresholdDiscount(THRESHOLD, 500)), this, this, this, clock);
        selected = null;
        message.setText("Bonjour ! Scannez votre premier article.");
        printedReceipt.getChildren().clear();
        if (centreStack.getChildren().size() > 1) {
            centreStack.getChildren().remove(1);
        }
        couponChoice.getItems().setAll(
            new Coupon("BIENVENUE5", 500, 3000, today.plusMonths(1)),
            new Coupon("FLASH2", 200, 1000, today),
            new Coupon("NOEL3", 300, 0, today.minusDays(1)));
        couponChoice.getSelectionModel().selectFirst();
    }

    private BorderPane buildRoot() {
        Label shop = new Label("MARCHÉ FRAIS");
        shop.setFont(Font.font("Inter", FontWeight.EXTRA_BOLD, 22));
        shop.setTextFill(Color.WHITE);
        Label till = new Label("Caisse libre-service n° 4");
        till.setFont(Font.font("Inter", FontWeight.SEMI_BOLD, 15));
        till.setTextFill(Color.web("#c8f7d4"));
        Region gap = new Region();
        HBox.setHgrow(gap, Priority.ALWAYS);
        Label dateTitle = new Label("Date du jour");
        dateTitle.setTextFill(Color.web("#c8f7d4"));
        DatePicker date = new DatePicker(today);
        date.setPrefWidth(140);
        date.setConverter(new javafx.util.converter.LocalDateStringConverter(
                DateTimeFormatter.ofPattern("dd/MM/yyyy"), DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        date.setOnAction(e -> {
            if (date.getValue() != null) {
                today = date.getValue();
                refresh();
            }
        });
        HBox top = new HBox(18, shop, till, gap, dateTitle, date);
        top.setAlignment(Pos.CENTER_LEFT);
        top.setPadding(new Insets(12, 24, 12, 24));
        top.setStyle("-fx-background-color: linear-gradient(to right, #0f7a3d, #18a058);");

        BorderPane root = new BorderPane();
        root.setTop(top);
        root.setLeft(scanArea());
        root.setCenter(screen());
        root.setRight(sidePanel());
        root.setStyle("-fx-background-color: #e9eef0;");
        return root;
    }

    private VBox scanArea() {
        GridPane tiles = new GridPane();
        tiles.setHgap(10);
        tiles.setVgap(10);
        int i = 0;
        for (Product p : CATALOGUE.all()) {
            VBox tile = new VBox(4, ProductIcon.of(p.code(), 52), small(p.name(), "#263238", 11),
                    small(Money.format(p.price()) + (p.byWeight() ? " / kg" : ""), "#0f7a3d", 12));
            tile.setAlignment(Pos.CENTER);
            tile.setPrefSize(150, 104);
            tile.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-cursor: hand;"
                    + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 6, 0, 0, 2);");
            tile.setOnMouseClicked(e -> act(() -> {
                checkout.scan(p.code());
                message.setText(p.name() + " scanné.");
            }));
            tiles.add(tile, i % 2, i / 2);
            i++;
        }

        scaleGrams.setEditable(true);
        scaleGrams.setPrefWidth(110);
        Label unit = small("g (plateau : " + Checkout.TRAY_GRAMS + " g)", "#455a64", 12);
        StackPane plate = new StackPane(ProductIcon.of("B01", 48));
        plate.setPrefSize(150, 56);
        plate.setStyle("-fx-background-color: linear-gradient(#e0e4e8, #9aa3ab); -fx-background-radius: 10 10 4 4;");
        HBox weight = new HBox(8, scaleGrams, unit);
        weight.setAlignment(Pos.CENTER);
        VBox scale = new VBox(8, heading("BALANCE (poids brut)"), plate, weight,
                small("Posez le produit, puis touchez sa vignette.", "#546e7a", 11));
        scale.setAlignment(Pos.CENTER);
        scale.setPadding(new Insets(10));
        scale.setStyle("-fx-background-color: #cfd8dc; -fx-background-radius: 10;");

        VBox box = new VBox(12, heading("TOUCHEZ UN ARTICLE POUR LE SCANNER"), tiles, scale);
        box.setPadding(new Insets(16));
        box.setPrefWidth(350);
        return box;
    }

    private StackPane screen() {
        HBox header = row("Article", "Qté", "Montant", "#607d8b", FontWeight.BOLD, 12);
        ScrollPane scroll = new ScrollPane(receipt);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: white; -fx-background-color: white;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        subtotal.setFont(Font.font("Inter", 15));
        savings.setFont(Font.font("Inter", FontWeight.BOLD, 15));
        savings.setTextFill(Color.web("#0f7a3d"));
        total.setFont(Font.font("Inter", FontWeight.EXTRA_BOLD, 32));
        total.setTextFill(Color.web("#102027"));
        VBox totals = new VBox(4, subtotal, savings, total);
        totals.setAlignment(Pos.CENTER_RIGHT);

        message.setFont(Font.font("Inter", FontWeight.SEMI_BOLD, 14));
        message.setTextFill(Color.web("#8a4b00"));
        message.setWrapText(true);
        message.setMaxWidth(Double.MAX_VALUE);
        message.setPadding(new Insets(10, 14, 10, 14));
        message.setStyle("-fx-background-color: #fff3d6; -fx-background-radius: 8; -fx-border-color: #ffd27a; -fx-border-radius: 8;");

        Rectangle line = new Rectangle(500, 1, Color.web("#cfd8dc"));
        VBox panel = new VBox(10, header, scroll, line, totals, message);
        panel.setPadding(new Insets(18));
        panel.setStyle("-fx-background-color: white; -fx-background-radius: 14;"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 14, 0, 0, 4);");
        centreStack.getChildren().setAll(panel);
        centreStack.setPadding(new Insets(16, 6, 16, 6));
        return centreStack;
    }

    private VBox sidePanel() {
        Label brand = new Label("MARCHÉ FRAIS · FIDÉLITÉ");
        brand.setFont(Font.font("Inter", FontWeight.EXTRA_BOLD, 12));
        brand.setTextFill(Color.WHITE);
        loyalty.setFont(Font.font("Inter", FontWeight.BOLD, 15));
        loyalty.setTextFill(Color.WHITE);
        loyalty.setWrapText(true);
        VBox card = new VBox(10, brand, loyalty);
        card.setPadding(new Insets(14));
        card.setStyle("-fx-background-color: linear-gradient(to bottom right, #ff8f00, #f4511e); -fx-background-radius: 12;"
                + "-fx-cursor: hand;");
        card.setOnMouseClicked(e -> act(() -> {
            checkout.scanLoyaltyCard(accountPoints);
            message.setText("Carte fidélité reconnue.");
        }));

        couponChoice.setMaxWidth(Double.MAX_VALUE);
        couponChoice.setCellFactory(lv -> new javafx.scene.control.ListCell<>() {
            @Override protected void updateItem(Coupon c, boolean empty) {
                super.updateItem(c, empty);
                setText(empty || c == null ? null : describe(c));
            }
        });
        couponChoice.setButtonCell(couponChoice.getCellFactory().call(null));
        Button applyCoupon = sideButton("Appliquer le coupon");
        applyCoupon.setOnAction(e -> act(() -> {
            Coupon c = couponChoice.getValue();
            message.setText(checkout.applyCoupon(c) ? "Coupon " + c.code() + " appliqué." : "Coupon " + c.code() + " refusé.");
        }));
        Button cancel = sideButton("Annuler l'article sélectionné");
        cancel.setOnAction(e -> act(() -> {
            if (selected == null) {
                message.setText("Touchez d'abord une ligne du ticket.");
                return;
            }
            checkout.cancel(selected.product().code());
            message.setText(selected.product().name() + " : un article annulé.");
            selected = null;
        }));
        Button next = sideButton("Client suivant");
        next.setOnAction(e -> {
            newCustomer();
            refresh();
        });

        ToggleGroup group = new ToggleGroup();
        accept.setToggleGroup(group);
        refuse.setToggleGroup(group);
        accept.setSelected(true);
        accept.setTextFill(Color.WHITE);
        refuse.setTextFill(Color.WHITE);
        Label tpe = new Label("Terminal de paiement\nla carte sera :");
        tpe.setTextFill(Color.web("#7CFFA0"));
        tpe.setFont(Font.font("DejaVu Sans Mono", 12));
        tpe.setPadding(new Insets(8));
        tpe.setMaxWidth(Double.MAX_VALUE);
        tpe.setStyle("-fx-background-color: #0b1a10; -fx-background-radius: 4;");
        HBox choice = new HBox(10, accept, refuse);
        choice.setAlignment(Pos.CENTER);
        VBox terminal = new VBox(8, tpe, choice);
        terminal.setPadding(new Insets(12));
        terminal.setStyle("-fx-background-color: #37474f; -fx-background-radius: 14;");

        pay.setMaxWidth(Double.MAX_VALUE);
        pay.setPrefHeight(60);
        pay.setStyle("-fx-background-color: #18a058; -fx-text-fill: white; -fx-font-size: 20; -fx-font-weight: bold;"
                + "-fx-background-radius: 12;");
        pay.setOnAction(e -> act(() -> {
            if (checkout.pay()) {
                if (checkout.hasLoyaltyCard()) {
                    accountPoints = checkout.loyaltyPoints();
                }
                showReceipt();
                message.setText("Paiement accepté. Prenez votre ticket.");
            } else {
                message.setText("Paiement refusé. Réessayez ou appelez un vendeur.");
            }
        }));

        Region grow = new Region();
        VBox.setVgrow(grow, Priority.ALWAYS);
        VBox box = new VBox(10, card, heading("COUPONS"), couponChoice, applyCoupon, cancel, next, grow, terminal, pay);
        box.setPadding(new Insets(16));
        box.setPrefWidth(320);
        return box;
    }

    private void showReceipt() {
        Button close = sideButton("Client suivant");
        close.setOnAction(e -> {
            newCustomer();
            refresh();
        });
        VBox paper = new VBox(10, printedReceipt, close);
        paper.setPadding(new Insets(18));
        paper.setMaxSize(420, Region.USE_PREF_SIZE);
        paper.setStyle("-fx-background-color: #fffdf5; -fx-background-radius: 4;"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.35), 20, 0, 0, 6);");
        centreStack.getChildren().add(paper);
    }

    /** Runs an action of the customer and shows what the model refuses. */
    private void act(Runnable action) {
        try {
            action.run();
        } catch (RuntimeException ex) {
            message.setText("Refusé : " + ex.getClass().getSimpleName() + " — " + ex.getMessage());
        }
        refresh();
    }

    private void refresh() {
        receipt.getChildren().clear();
        for (Line l : checkout.lines()) {
            String qty = l.product().byWeight() ? kilos(l.grams()) : "× " + l.quantity();
            HBox r = row(l.product().name(), qty, Money.format(l.amount()), "#263238", FontWeight.NORMAL, 15);
            r.setStyle(l == selected ? "-fx-background-color: #d7ecff; -fx-background-radius: 6;" : "-fx-cursor: hand;");
            r.setOnMouseClicked(e -> {
                selected = l;
                refresh();
            });
            receipt.getChildren().add(r);
            if (l.product().byWeight()) {
                receipt.getChildren().add(row("    " + Money.format(l.product().price()) + " / kg", "", "",
                        "#90a4ae", FontWeight.NORMAL, 12));
            }
        }
        int saved = 0;
        for (Discount d : checkout.discounts()) {
            HBox r = row("★ " + d.label(), "", Money.format(-d.amount()), "#0f7a3d", FontWeight.BOLD, 14);
            r.setStyle("-fx-background-color: #e8f7ee; -fx-background-radius: 6;");
            receipt.getChildren().add(r);
            saved += d.amount();
        }
        subtotal.setText("Sous-total  " + Money.format(checkout.subtotal()));
        savings.setText("Vos économies  " + Money.format(-saved));
        savings.setVisible(saved > 0);
        total.setText("TOTAL  " + Money.format(checkout.total()));
        pay.setText("PAYER  " + Money.format(checkout.total()));
        pay.setDisable(checkout.isPaid());
        int missing = THRESHOLD - checkout.subtotal();
        if (!checkout.isPaid() && missing > 0 && !checkout.lines().isEmpty()) {
            message.setText(message.getText().endsWith("!") || message.getText().startsWith("Refusé")
                    || message.getText().startsWith("Coupon") || message.getText().startsWith("Paiement")
                    || message.getText().startsWith("Touchez") || message.getText().contains("annulé")
                    ? message.getText()
                    : "Plus que " + Money.format(missing) + " d'achats pour profiter de -5,00 € !");
        }
        loyalty.setText(checkout.hasLoyaltyCard()
                ? checkout.loyaltyPoints() + " points  (+" + checkout.pointsEarned() + " avec cet achat)"
                : "Touchez la carte pour la scanner\n(" + accountPoints + " points sur le compte)");
    }

    private static String describe(Coupon c) {
        return c.code() + " : " + Money.format(-c.amount())
                + (c.minimumPurchase() > 0 ? " dès " + Money.format(c.minimumPurchase()) : "")
                + ", jusqu'au " + DateTimeFormatter.ofPattern("dd/MM").format(c.expiry());
    }

    private static String kilos(int grams) {
        return String.format("%d,%03d kg", grams / 1000, grams % 1000);
    }

    private static String pad(String s, int n) {
        return s.length() >= n ? s.substring(0, n) : s + " ".repeat(n - s.length());
    }

    private static String leftPad(String s, int n) {
        return s.length() >= n ? s : " ".repeat(n - s.length()) + s;
    }

    private static Label mono(String text, boolean bold) {
        Label l = new Label(text);
        l.setFont(Font.font("DejaVu Sans Mono", bold ? FontWeight.BOLD : FontWeight.NORMAL, 12));
        l.setTextFill(Color.web("#222"));
        return l;
    }

    private static HBox row(String name, String qty, String amount, String colour, FontWeight weight, double size) {
        Label n = small(name, colour, size);
        n.setFont(Font.font("Inter", weight, size));
        Label q = small(qty, colour, size);
        q.setFont(Font.font("Inter", weight, size));
        q.setPrefWidth(90);
        q.setAlignment(Pos.CENTER_RIGHT);
        Label a = small(amount, colour, size);
        a.setFont(Font.font("Inter", weight, size));
        a.setPrefWidth(100);
        a.setAlignment(Pos.CENTER_RIGHT);
        Region gap = new Region();
        HBox.setHgrow(gap, Priority.ALWAYS);
        HBox r = new HBox(8, n, gap, q, a);
        r.setPadding(new Insets(5, 8, 5, 8));
        return r;
    }

    private static Label heading(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Inter", FontWeight.BOLD, 12));
        l.setTextFill(Color.web("#546e7a"));
        return l;
    }

    private static Label small(String text, String colour, double size) {
        Label l = new Label(text);
        l.setFont(Font.font("Inter", FontWeight.SEMI_BOLD, size));
        l.setTextFill(Color.web(colour));
        return l;
    }

    private static Button sideButton(String text) {
        Button b = new Button(text);
        b.setMaxWidth(Double.MAX_VALUE);
        b.setStyle("-fx-background-color: white; -fx-text-fill: #263238; -fx-font-size: 13; -fx-font-weight: bold;"
                + "-fx-background-radius: 10; -fx-padding: 10; -fx-border-color: #cfd8dc; -fx-border-radius: 10;");
        return b;
    }
}
