package com.unittesting.parking.ui;

import java.util.Map;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

/** The car park seen from above: bays, cars, the two barriers. */
class LotView extends Canvas {

    static final int BAYS_PER_ROW = 12;
    static final int ROWS = 3;
    private static final double BAY_W = 58;
    private static final double BAY_H = 92;
    private static final double LEFT = 70;
    private static final double[] ROW_Y = {36, 236, 328};   // rows 2 and 3 are back to back
    private static final Color[] PAINT = {
        Color.web("#e53935"), Color.web("#1e88e5"), Color.web("#fdd835"), Color.web("#eceff1"),
        Color.web("#43a047"), Color.web("#212121"), Color.web("#8e24aa"), Color.web("#fb8c00"),
        Color.web("#90a4ae"), Color.web("#00897b"),
    };

    LotView() {
        super(LEFT * 2 + BAYS_PER_ROW * BAY_W, 600);
    }

    /** The bay under a point of the canvas, or -1. */
    int bayAt(double x, double y) {
        for (int r = 0; r < ROWS; r++) {
            if (y >= ROW_Y[r] && y < ROW_Y[r] + BAY_H && x >= LEFT && x < LEFT + BAYS_PER_ROW * BAY_W) {
                return r * BAYS_PER_ROW + (int) ((x - LEFT) / BAY_W);
            }
        }
        return -1;
    }

    /** bays: bay index -> ticket number. */
    void draw(Map<Integer, Integer> bays, int selectedTicket, boolean carAtEntry, boolean entryOpen, boolean exitOpen) {
        GraphicsContext g = getGraphicsContext2D();
        double w = getWidth(), h = getHeight();
        g.setFill(Color.web("#4a4f55"));
        g.fillRect(0, 0, w, h);
        // texture
        g.setFill(Color.rgb(255, 255, 255, 0.025));
        for (int i = 0; i < 400; i++) {
            double x = (i * 73) % w, y = (i * 191) % h;
            g.fillRect(x, y, 2, 2);
        }
        // walls
        g.setStroke(Color.web("#b0b6bb"));
        g.setLineWidth(8);
        g.strokeRect(4, 4, w - 8, h - 8);
        // bays
        g.setLineWidth(3);
        g.setStroke(Color.WHITE);
        for (int r = 0; r < ROWS; r++) {
            for (int b = 0; b <= BAYS_PER_ROW; b++) {
                double x = LEFT + b * BAY_W;
                g.strokeLine(x, ROW_Y[r], x, ROW_Y[r] + BAY_H);
            }
        }
        g.strokeLine(LEFT, ROW_Y[2], LEFT + BAYS_PER_ROW * BAY_W, ROW_Y[2]);
        // bay numbers
        g.setFont(Font.font("Inter", FontWeight.BOLD, 10));
        g.setTextAlign(TextAlignment.CENTER);
        g.setFill(Color.rgb(255, 255, 255, 0.45));
        for (int r = 0; r < ROWS; r++) {
            for (int b = 0; b < BAYS_PER_ROW; b++) {
                double y = r == 1 ? ROW_Y[r] + 12 : ROW_Y[r] + BAY_H - 4;
                g.fillText((char) ('A' + r) + String.valueOf(b + 1), LEFT + b * BAY_W + BAY_W / 2, y);
            }
        }
        // driving arrows
        g.setFill(Color.rgb(255, 255, 255, 0.55));
        for (int k = 0; k < 4; k++) {
            arrow(g, 170 + k * 170, 185, 1);
            arrow(g, 170 + k * 170, 470, -1);
        }
        // cars
        bays.forEach((bay, ticket) -> {
            int r = bay / BAYS_PER_ROW, b = bay % BAYS_PER_ROW;
            double cx = LEFT + b * BAY_W + BAY_W / 2;
            double cy = ROW_Y[r] + BAY_H / 2;
            car(g, cx, cy, r == 1 ? 180 : 0, PAINT[(ticket * 7) % PAINT.length], ticket == selectedTicket);
        });
        // gates at the bottom
        double gateY = h - 84;
        g.setFill(Color.rgb(255, 255, 255, 0.85));
        g.setFont(Font.font("Inter", FontWeight.EXTRA_BOLD, 15));
        g.fillText("ENTRÉE ↑", 110, gateY - 16);
        g.fillText("↓ SORTIE", w - 110, gateY - 16);
        if (carAtEntry) {
            car(g, 110, h - 44, 0, Color.web("#00acc1"), false);
        }
        barrier(g, 60, gateY, 100, entryOpen);
        barrier(g, w - 60, gateY, -100, exitOpen);
    }

    private static void arrow(GraphicsContext g, double x, double y, int dir) {
        g.fillRect(x - 24 * dir, y - 3, 30 * dir, 6);
        g.fillPolygon(new double[] {x + 6 * dir, x + 18 * dir, x + 6 * dir}, new double[] {y - 9, y, y + 9}, 3);
    }

    private static void barrier(GraphicsContext g, double x, double y, double length, boolean open) {
        g.setFill(Color.web("#37474f"));
        g.fillRoundRect(x - 10, y - 10, 20, 20, 6, 6);
        g.save();
        g.translate(x, y);
        if (open) {
            g.rotate(length > 0 ? -75 : 75);
        }
        int segments = 6;
        double seg = Math.abs(length) / segments;
        for (int i = 0; i < segments; i++) {
            g.setFill(i % 2 == 0 ? Color.web("#e53935") : Color.WHITE);
            double sx = length > 0 ? 8 + i * seg : -8 - (i + 1) * seg;
            g.fillRect(sx, -3.5, seg, 7);
        }
        g.restore();
        g.setFill(Color.web("#ffca28"));
        g.fillOval(x - 4, y - 4, 8, 8);
    }

    private static void car(GraphicsContext g, double x, double y, double angle, Color paint, boolean selected) {
        g.save();
        g.translate(x, y);
        g.rotate(angle);
        g.setFill(Color.rgb(0, 0, 0, 0.35));
        g.fillRoundRect(-17, -29, 38, 62, 14, 14);
        g.setFill(paint);
        g.fillRoundRect(-19, -32, 38, 64, 14, 14);
        g.setFill(paint.deriveColor(0, 1, 0.8, 1));
        g.fillRoundRect(-15, -10, 30, 26, 8, 8);
        g.setFill(Color.web("#263238", 0.9));
        g.fillRoundRect(-15, -20, 30, 11, 6, 6);   // windscreen
        g.fillRoundRect(-14, 17, 28, 7, 5, 5);     // rear window
        g.setFill(Color.web("#fff59d"));
        g.fillOval(-16, -32, 8, 4);
        g.fillOval(8, -32, 8, 4);
        if (selected) {
            g.setStroke(Color.web("#ffca28"));
            g.setLineWidth(3);
            g.setLineDashes(6, 4);
            g.strokeRoundRect(-25, -38, 50, 76, 16, 16);
            g.setLineDashes();
        }
        g.restore();
    }
}
