package fr.tp.vending.ui;

import fr.tp.vending.model.Money;
import fr.tp.vending.model.Slot;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

/** One compartment behind the glass: up to three products in a row, the spiral, the price tag. */
class SlotView extends Canvas {

    static final double W = 118;
    static final double H = 128;
    private final Slot slot;

    SlotView(Slot slot) {
        super(W, H);
        this.slot = slot;
        refresh();
    }

    void refresh() {
        GraphicsContext g = getGraphicsContext2D();
        g.clearRect(0, 0, W, H);
        Look look = Look.of(slot.product().name());
        int shown = Math.min(3, slot.quantity());
        // back products first, smaller and darker to suggest depth
        for (int i = shown - 1; i >= 0; i--) {
            double scale = 1 - i * 0.08;
            g.setGlobalAlpha(1 - i * 0.25);
            ProductPainter.paint(g, look, slot.product().name(), W / 2 + i * 9, 96 - i * 4, scale);
        }
        g.setGlobalAlpha(1);
        // spiral
        g.setStroke(Color.web("#b0b6bf"));
        g.setLineWidth(2);
        for (int k = 0; k < 7; k++) {
            g.strokeArc(6 + k * 15, 88, 16, 12, 180, 180, javafx.scene.shape.ArcType.OPEN);
        }
        // shelf and price tag
        g.setFill(Color.web("#9aa3ad"));
        g.fillRect(0, 100, W, 5);
        g.setFill(Color.web("#fafafa"));
        g.fillRoundRect(8, 108, W - 16, 18, 4, 4);
        g.setFont(Font.font("Inter", FontWeight.BOLD, 11));
        g.setTextAlign(TextAlignment.LEFT);
        g.setFill(Color.web("#b71c1c"));
        g.fillText(slot.code(), 13, 121);
        g.setFill(Color.web("#212121"));
        g.setTextAlign(TextAlignment.RIGHT);
        g.fillText(Money.format(slot.product().price()), W - 13, 121);
        if (slot.isEmpty()) {
            g.setFill(Color.rgb(183, 28, 28, 0.9));
            g.fillRoundRect(16, 46, W - 32, 22, 6, 6);
            g.setFill(Color.WHITE);
            g.setTextAlign(TextAlignment.CENTER);
            g.fillText("ÉPUISÉ", W / 2, 61);
        }
    }
}
