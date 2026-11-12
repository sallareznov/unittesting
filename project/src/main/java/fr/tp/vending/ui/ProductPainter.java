package fr.tp.vending.ui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

/** Draws one product centred on (x, y) with the bottom at y. */
final class ProductPainter {
    private ProductPainter() { }

    static void paint(GraphicsContext g, Look look, String name, double x, double bottom, double scale) {
        g.save();
        g.translate(x, bottom);
        g.scale(scale, scale);
        switch (look.shape()) {
            case CAN -> can(g, look, name);
            case BOTTLE -> bottle(g, look);
            case BAG -> bag(g, look, name);
            case BAR -> bar(g, look, name);
        }
        g.restore();
    }

    private static LinearGradient shine(Color c) {
        return new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, c.darker()), new Stop(0.35, c.brighter()), new Stop(0.6, c), new Stop(1, c.darker().darker()));
    }

    private static void can(GraphicsContext g, Look l, String name) {
        g.setFill(shine(Color.web("#c9ccd1")));
        g.fillRoundRect(-17, -66, 34, 8, 6, 6);
        g.setFill(shine(l.main()));
        g.fillRoundRect(-18, -60, 36, 58, 6, 6);
        g.setFill(shine(Color.web("#c9ccd1")));
        g.fillRoundRect(-17, -6, 34, 6, 6, 6);
        g.setStroke(l.accent());
        g.setLineWidth(3);
        g.strokeArc(-14, -44, 28, 20, 200, 140, javafx.scene.shape.ArcType.OPEN);
        label(g, (name.length() > 5 ? name.substring(0, 4) : name).toUpperCase(), l.accent(), -27, 7.5);
    }

    private static void bottle(GraphicsContext g, Look l) {
        g.setFill(l.accent());
        g.fillRect(-6, -78, 12, 7);
        Color glass = l.main().deriveColor(0, 1, 1, 0.75);
        g.setFill(shine(glass));
        g.fillPolygon(new double[] {-5, 5, 15, 15, -15, -15}, new double[] {-71, -71, -54, -2, -2, -54}, 6);
        g.fillRoundRect(-15, -60, 30, 60, 10, 10);
        g.setFill(l.accent());
        g.fillRect(-15, -40, 30, 16);
        g.setFill(Color.rgb(255, 255, 255, 0.5));
        g.fillRect(-10, -56, 3, 50);
    }

    private static void bag(GraphicsContext g, Look l, String name) {
        double[] xs = {-24, -20, -16, -12, -8, -4, 0, 4, 8, 12, 16, 20, 24, 23, 25, 20, 16, 12, 8, 4, 0, -4, -8, -12, -16, -20, -25, -23};
        double[] ys = {-70, -74, -70, -74, -70, -74, -70, -74, -70, -74, -70, -74, -70, -36, -4, -1, -4, -1, -4, -1, -4, -1, -4, -1, -4, -1, -4, -36};
        g.setFill(shine(l.main()));
        g.fillPolygon(xs, ys, xs.length);
        g.setFill(l.accent());
        g.fillOval(-16, -50, 32, 26);
        label(g, (name.length() > 6 ? name.substring(0, 5) : name).toUpperCase(), l.main().darker(), -34, 7);
    }

    private static void bar(GraphicsContext g, Look l, String name) {
        g.setFill(shine(l.main()));
        g.fillRoundRect(-28, -34, 56, 32, 6, 6);
        g.setFill(Color.web("#d7d7d7"));
        g.fillRect(-30, -34, 4, 32);
        g.fillRect(26, -34, 4, 32);
        String short_ = name.contains(" ") ? name.substring(name.indexOf(' ') + 1) : name;
        label(g, short_.toUpperCase(), l.accent(), -14, 9);
    }

    private static void label(GraphicsContext g, String text, Color c, double y, double size) {
        g.setFill(c);
        g.setFont(Font.font("Inter", FontWeight.EXTRA_BOLD, size));
        g.setTextAlign(TextAlignment.CENTER);
        g.fillText(text, 0, y);
    }
}
