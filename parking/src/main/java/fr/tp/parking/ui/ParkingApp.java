package fr.tp.parking.ui;

import fr.tp.parking.model.CarPark;
import fr.tp.parking.model.Money;
import fr.tp.parking.model.PaymentTerminal;
import fr.tp.parking.model.Tariff;
import fr.tp.parking.model.Ticket;
import fr.tp.parking.model.TicketPrinter;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
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
 * The car park from above, with the clock and the pay station on the right.
 * Click a car to put its ticket in the pay station. The window plays the hardware:
 * barriers, ticket printer and payment terminal.
 */
public class ParkingApp extends Application implements TicketPrinter, PaymentTerminal {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.FRENCH);
    private static final DateTimeFormatter HM = DateTimeFormatter.ofPattern("HH:mm");

    private final SettableClock clock = new SettableClock(LocalDateTime.now().withHour(7).withMinute(0).withSecond(0).withNano(0));
    private final LotView lot = new LotView();
    private final Map<Integer, Ticket> bays = new HashMap<>();   // bay -> ticket of the car parked there
    private final Random random = new Random(3);
    private CarPark park;
    private int selectedBay = -1;
    private boolean entryOpen;
    private boolean exitOpen;
    private Ticket lastPrinted;

    private final Label free = new Label();
    private final Label day = new Label();
    private final Label time = new Label();
    private final Label stationLine1 = new Label();
    private final Label stationLine2 = new Label();
    private final Label stationLine3 = new Label();
    private final Label amount = new Label();
    private final Label message = new Label();
    private final Canvas ticketView = new Canvas(240, 96);
    private final RadioButton accept = new RadioButton("Carte acceptée");
    private final RadioButton refuse = new RadioButton("Carte refusée");

    public static void main(String[] args) {
        launch(args);
    }

    // --- hardware seen by the model ---

    @Override
    public void print(Ticket ticket) { lastPrinted = ticket; }

    @Override
    public boolean charge(int cents) {
        boolean ok = accept.isSelected();
        message.setText(ok ? "Carte débitée de " + Money.format(cents) + "." : "Paiement de " + Money.format(cents) + " refusé.");
        return ok;
    }

    @Override
    public void start(Stage stage) {
        park = new CarPark(LotView.BAYS_PER_ROW * LotView.ROWS, clock, new Tariff(),
                () -> entryOpen = true, () -> exitOpen = true, this, this);

        Scene scene = new Scene(buildRoot(), 1240, 700);
        stage.setTitle("Parking");
        stage.setScene(scene);
        stage.show();

        // a morning of arrivals
        for (int i = 0; i < 29; i++) {
            clock.advance(java.time.Duration.ofMinutes(4 + random.nextInt(14)));
            arrive();
        }
        clock.set(clock.instant().atZone(clock.getZone()).toLocalDateTime().withHour(12).withMinute(0));
        selectedBay = bays.entrySet().stream().filter(e -> e.getValue().number() == 9)
                .map(Map.Entry::getKey).findFirst().orElse(-1);
        entryOpen = false;
        message.setText("Cliquez sur une voiture pour insérer son ticket.");
        refresh();

        String snapshot = System.getProperty("snapshot");
        if (snapshot != null) {
            PauseTransition wait = new PauseTransition(Duration.millis(800));
            wait.setOnFinished(e -> {
                Snapshot.save(scene, snapshot);
                Platform.exit();
            });
            wait.play();
        }
    }

    private void arrive() {
        entryOpen = false;
        if (park.enter().isEmpty()) {
            message.setText("COMPLET : la barrière reste fermée.");
            refresh();
            return;
        }
        int bay;
        int tries = 0;
        do {
            bay = random.nextInt(LotView.BAYS_PER_ROW * LotView.ROWS);
            tries++;
        } while (bays.containsKey(bay) && tries < 1000);
        if (!bays.containsKey(bay)) {
            bays.put(bay, lastPrinted);
            selectedBay = bay;
        } else {
            message.setText("Ticket n° " + lastPrinted.number() + " imprimé… mais aucune place n'est libre !");
        }
        refresh();
    }

    private Ticket current() { return selectedBay < 0 ? null : bays.get(selectedBay); }

    private BorderPane buildRoot() {
        lot.setOnMouseClicked(e -> {
            int bay = lot.bayAt(e.getX(), e.getY());
            if (bays.containsKey(bay)) {
                selectedBay = bay;
                exitOpen = false;
                message.setText("Ticket inséré.");
                refresh();
            }
        });
        StackPane centre = new StackPane(lot);
        centre.setPadding(new Insets(16));
        centre.setStyle("-fx-background-color: #2b2f33;");

        Label signTitle = new Label("PLACES LIBRES");
        signTitle.setFont(Font.font("Inter", FontWeight.BOLD, 13));
        signTitle.setTextFill(Color.web("#cfd8dc"));
        free.setFont(Font.font("DejaVu Sans Mono", FontWeight.BOLD, 42));
        VBox sign = new VBox(2, signTitle, free);
        sign.setAlignment(Pos.CENTER);
        sign.setPadding(new Insets(8));
        sign.setStyle("-fx-background-color: #0d0f12; -fx-background-radius: 8; -fx-border-color: #455a64;"
                + "-fx-border-width: 3; -fx-border-radius: 8;");

        day.setFont(Font.font("Inter", 13));
        day.setTextFill(Color.web("#b0bec5"));
        time.setFont(Font.font("Inter", FontWeight.EXTRA_BOLD, 28));
        time.setTextFill(Color.WHITE);
        TextField setTime = new TextField();
        setTime.setPromptText("hh:mm");
        setTime.setPrefColumnCount(5);
        Button go = button("Régler", "#455a64");
        go.setOnAction(e -> {
            try {
                LocalTime t = LocalTime.parse(setTime.getText().strip());
                LocalDateTime now = park.now().toLocalDateTime();
                LocalDateTime target = now.toLocalDate().atTime(t);
                clock.set(target.isBefore(now) ? target.plusDays(1) : target);
                exitOpen = false;
                refresh();
            } catch (DateTimeParseException ex) {
                message.setText("Heure invalide (hh:mm).");
                refresh();
            }
        });
        HBox setRow = new HBox(6, time, new Region(), setTime, go);
        HBox.setHgrow(setRow.getChildren().get(1), Priority.ALWAYS);
        setRow.setAlignment(Pos.CENTER_LEFT);
        HBox jumps = new HBox(6, jump("+15 min", java.time.Duration.ofMinutes(15)),
                jump("+1 h", java.time.Duration.ofHours(1)), jump("+1 jour", java.time.Duration.ofDays(1)));
        VBox clockBox = new VBox(4, day, setRow, jumps);

        Button arriveButton = button("Une voiture arrive", "#1e88e5");
        arriveButton.setOnAction(e -> arrive());

        Label stationTitle = new Label("CAISSE AUTOMATIQUE");
        stationTitle.setFont(Font.font("Inter", FontWeight.EXTRA_BOLD, 13));
        stationTitle.setTextFill(Color.web("#ffca28"));
        for (Label l : new Label[] {stationLine1, stationLine2, stationLine3}) {
            l.setFont(Font.font("DejaVu Sans Mono", 12));
            l.setTextFill(Color.web("#9be7ff"));
        }
        amount.setFont(Font.font("DejaVu Sans Mono", FontWeight.BOLD, 22));
        amount.setTextFill(Color.web("#ffe082"));
        VBox screen = new VBox(3, stationLine1, stationLine2, stationLine3, amount);
        screen.setPadding(new Insets(8));
        screen.setStyle("-fx-background-color: #06222e; -fx-background-radius: 6;");

        ToggleGroup g = new ToggleGroup();
        accept.setToggleGroup(g);
        refuse.setToggleGroup(g);
        accept.setSelected(true);
        accept.setTextFill(Color.web("#cfd8dc"));
        refuse.setTextFill(Color.web("#cfd8dc"));
        HBox cardChoice = new HBox(10, accept, refuse);

        Button pay = button("Payer", "#43a047");
        pay.setOnAction(e -> act(() -> {
            Ticket t = current();
            if (t != null && park.pay(t)) {
                message.setText(message.getText() + " Ticket validé : 15 min pour sortir.");
            }
        }));
        Button lost = button("Ticket perdu", "#757575");
        lost.setOnAction(e -> act(() -> {
            if (selectedBay < 0 || !bays.containsKey(selectedBay)) {
                message.setText("Choisissez d'abord la voiture.");
                return;
            }
            park.lostTicket().ifPresent(pass -> {
                bays.put(selectedBay, pass);
                message.setText("Ticket de sortie n° " + pass.number() + " délivré.");
            });
        }));
        Button leave = button("Sortir", "#e53935");
        leave.setOnAction(e -> act(() -> {
            Ticket t = current();
            if (t == null) {
                return;
            }
            if (park.exit(t)) {
                bays.remove(selectedBay);
                selectedBay = -1;
                message.setText("Bonne route !");
            } else {
                message.setText("Barrière fermée : " + (t.isUsed() ? "ticket déjà utilisé." : "il reste "
                        + Money.format(park.amountDue(t)) + " à payer."));
            }
        }));
        HBox payRow = new HBox(8, pay, lost);
        message.setWrapText(true);
        message.setTextFill(Color.web("#ffcc80"));
        message.setFont(Font.font("Inter", 12));
        message.setMinHeight(34);
        VBox station = new VBox(8, stationTitle, ticketView, screen, cardChoice, payRow, leave, message);
        station.setPadding(new Insets(12));
        station.setStyle("-fx-background-color: linear-gradient(#37474f, #263238); -fx-background-radius: 12;");

        Region grow = new Region();
        VBox.setVgrow(grow, Priority.ALWAYS);
        VBox side = new VBox(10, sign, clockBox, arriveButton, grow, station);
        side.setPadding(new Insets(14));
        side.setPrefWidth(310);
        side.setStyle("-fx-background-color: #1c1f23;");

        return new BorderPane(centre, null, side, null, null);
    }

    private void act(Runnable r) {
        try {
            r.run();
        } catch (RuntimeException ex) {
            message.setText("Refusé : " + ex.getMessage());
        }
        refresh();
    }

    private void refresh() {
        int n = park.freeSpaces();
        free.setText(n <= 0 ? "COMPLET" : String.format("%02d", n));
        free.setTextFill(n <= 0 ? Color.web("#ff5252") : Color.web("#69f0ae"));
        ZonedDateTime now = park.now();
        day.setText(DAY.format(now));
        time.setText(HM.format(now));
        Ticket t = current();
        if (t == null) {
            stationLine1.setText("Insérez votre ticket");
            stationLine2.setText("(cliquez sur une voiture)");
            stationLine3.setText("");
            amount.setText("");
        } else {
            java.time.Duration stay = java.time.Duration.between(t.entry(), now);
            stationLine1.setText("Ticket n° " + String.format("%04d", t.number()) + "   entrée " + HM.format(t.entry()));
            stationLine2.setText(String.format("Durée %d h %02d", stay.toHours(), stay.toMinutesPart())
                    + (t.amountPaid() > 0 ? "   déjà payé " + Money.format(t.amountPaid()) : ""));
            stationLine3.setText(t.isPaid() ? "Payé à " + HM.format(t.paidAt()) + ", sortie avant "
                    + HM.format(t.paidAt().plus(CarPark.TIME_TO_LEAVE)) : "");
            int due = park.amountDue(t);
            amount.setText(due == 0 ? "Rien à payer ✓" : "À payer " + Money.format(due));
        }
        drawTicket(t);
        Map<Integer, Integer> numbers = new HashMap<>();
        bays.forEach((bay, ticket) -> numbers.put(bay, ticket.number()));
        lot.draw(numbers, t == null ? -1 : t.number(), true, entryOpen, exitOpen);
    }

    private void drawTicket(Ticket t) {
        GraphicsContext g = ticketView.getGraphicsContext2D();
        g.clearRect(0, 0, ticketView.getWidth(), ticketView.getHeight());
        if (t == null) {
            return;
        }
        g.setFill(Color.web("#fffdf5"));
        g.fillRoundRect(10, 4, 220, 86, 6, 6);
        g.setFill(Color.web("#e53935"));
        g.fillRect(10, 4, 220, 14);
        g.setFill(Color.WHITE);
        g.setFont(Font.font("Inter", FontWeight.BOLD, 10));
        g.fillText("PARKING DU CENTRE · TICKET D'ENTRÉE", 18, 15);
        g.setFill(Color.web("#263238"));
        g.setFont(Font.font("DejaVu Sans Mono", 12));
        g.fillText("N° " + String.format("%04d", t.number()), 18, 36);
        g.fillText(DateTimeFormatter.ofPattern("dd/MM/yyyy  HH:mm").format(t.entry()), 18, 54);
        Random r = new Random(t.number());
        double x = 18;
        while (x < 220) {
            double bar = 1 + r.nextInt(3);
            g.fillRect(x, 62, bar, 22);
            x += bar + 1 + r.nextInt(3);
        }
    }

    private Button jump(String text, java.time.Duration d) {
        Button b = button(text, "#455a64");
        b.setOnAction(e -> {
            clock.advance(d);
            exitOpen = false;
            refresh();
        });
        return b;
    }

    private static Button button(String text, String colour) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color: " + colour + "; -fx-text-fill: white; -fx-font-weight: bold;"
                + "-fx-font-size: 13; -fx-background-radius: 6; -fx-padding: 7 10 7 10;");
        b.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(b, Priority.ALWAYS);
        return b;
    }
}
