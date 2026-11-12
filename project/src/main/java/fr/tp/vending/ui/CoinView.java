package fr.tp.vending.ui;

import fr.tp.vending.model.Coin;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.effect.DropShadow;

/** A euro coin: copper for 5c, gold for 10c to 50c, two metals for 1 and 2 euros. */
class CoinView extends StackPane {

    CoinView(Coin coin, double scale) {
        double r = switch (coin) {
            case TWO_EUROS -> 22;
            case ONE_EURO -> 20;
            case FIFTY_CENTS -> 21;
            case TWENTY_CENTS -> 19;
            case TEN_CENTS -> 17;
            case FIVE_CENTS -> 18;
        } * scale;
        Color gold = Color.web("#d9b44a");
        Color silver = Color.web("#c7cbd1");
        Color copper = Color.web("#c06a3a");
        Color outer = switch (coin) {
            case TWO_EUROS -> silver;
            case ONE_EURO -> gold;
            case FIVE_CENTS -> copper;
            default -> gold;
        };
        Color inner = switch (coin) {
            case TWO_EUROS -> gold;
            case ONE_EURO -> silver;
            default -> outer;
        };
        Circle rim = new Circle(r, metal(outer));
        rim.setStroke(outer.darker());
        Circle centre = new Circle(r * 0.68, metal(inner));
        centre.setStroke(inner.darker());
        centre.setStrokeWidth(0.8);
        String label = coin.cents() >= 100 ? (coin.cents() / 100) + "€" : coin.cents() + "c";
        Text text = new Text(label);
        text.setFont(Font.font("Inter", FontWeight.EXTRA_BOLD, r * 0.62));
        text.setFill(Color.rgb(60, 40, 10, 0.85));
        getChildren().addAll(rim, centre, text);
        setEffect(new DropShadow(4, 1, 2, Color.rgb(0, 0, 0, 0.5)));
        setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);
    }

    private static RadialGradient metal(Color c) {
        return new RadialGradient(0, 0, 0.35, 0.3, 0.8, true, CycleMethod.NO_CYCLE,
                new Stop(0, c.brighter().brighter()), new Stop(0.6, c), new Stop(1, c.darker()));
    }
}
