package com.unittesting.checkout.ui;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

/** Small drawings of the shop's products, by product code. Pure decoration. */
final class ProductIcon {
    private ProductIcon() { }

    static Canvas of(String code, double size) {
        Canvas c = new Canvas(size, size);
        GraphicsContext g = c.getGraphicsContext2D();
        g.scale(size / 64, size / 64);
        switch (code) {
            case "Y01" -> { // four yogurt pots
                for (int i = 0; i < 2; i++) {
                    for (int j = 0; j < 2; j++) {
                        double x = 10 + i * 23, y = 12 + j * 22;
                        g.setFill(Color.WHITE);
                        g.fillPolygon(new double[] {x, x + 20, x + 18, x + 2}, new double[] {y, y, y + 20, y + 20}, 4);
                        g.setStroke(Color.web("#cfd8dc"));
                        g.strokePolygon(new double[] {x, x + 20, x + 18, x + 2}, new double[] {y, y, y + 20, y + 20}, 4);
                        g.setFill(Color.web("#29b6f6"));
                        g.fillRect(x + 3, y + 8, 14, 5);
                        g.setFill(Color.web("#b0bec5"));
                        g.fillRect(x - 1, y - 2, 22, 3);
                    }
                }
            }
            case "C01" -> { // chocolate bar
                g.setFill(Color.web("#3e2723"));
                g.fillRoundRect(10, 14, 44, 36, 4, 4);
                g.setFill(Color.web("#c62828"));
                g.fillRect(10, 30, 44, 20);
                g.setFill(Color.web("#ffd54f"));
                g.setFont(Font.font("Inter", FontWeight.EXTRA_BOLD, 8));
                g.setTextAlign(TextAlignment.CENTER);
                g.fillText("NOIR 70%", 32, 43);
                g.setStroke(Color.web("#5d4037"));
                for (int i = 1; i < 3; i++) {
                    g.strokeLine(10 + i * 14.7, 14, 10 + i * 14.7, 30);
                }
            }
            case "B01" -> { // bananas
                g.setStroke(Color.web("#fbc02d"));
                g.setLineWidth(9);
                for (int i = 0; i < 3; i++) {
                    g.strokeArc(8 + i * 4, 4 + i * 5, 46, 46, 200, 120, ArcType.OPEN);
                }
                g.setFill(Color.web("#5d4037"));
                g.fillOval(50, 34, 6, 6);
            }
            case "A01" -> { // apples
                apple(g, 18, 34, Color.web("#d32f2f"));
                apple(g, 40, 30, Color.web("#7cb342"));
                apple(g, 29, 46, Color.web("#e53935"));
            }
            case "L01" -> { // milk carton
                g.setFill(Color.WHITE);
                g.fillRect(18, 18, 28, 40);
                g.setFill(Color.web("#e3f2fd"));
                g.fillPolygon(new double[] {18, 32, 46}, new double[] {18, 6, 18}, 3);
                g.setFill(Color.web("#1e88e5"));
                g.fillRect(18, 34, 28, 12);
                g.setStroke(Color.web("#90a4ae"));
                g.strokeRect(18, 18, 28, 40);
                g.setFill(Color.WHITE);
                g.setFont(Font.font("Inter", FontWeight.BOLD, 8));
                g.setTextAlign(TextAlignment.CENTER);
                g.fillText("LAIT", 32, 43);
            }
            case "P01" -> { // baguette
                g.save();
                g.translate(32, 32);
                g.rotate(-35);
                g.setFill(Color.web("#d9a05b"));
                g.fillRoundRect(-28, -7, 56, 14, 14, 14);
                g.setStroke(Color.web("#a86a2a"));
                g.setLineWidth(2);
                for (int i = -2; i <= 2; i++) {
                    g.strokeLine(i * 10 - 3, -4, i * 10 + 3, 4);
                }
                g.restore();
            }
            case "E01" -> { // water pack
                for (int i = 0; i < 3; i++) {
                    double x = 10 + i * 15;
                    g.setFill(Color.web("#b3e5fc"));
                    g.fillRoundRect(x, 18, 13, 40, 6, 6);
                    g.setFill(Color.web("#0288d1"));
                    g.fillRect(x + 4, 12, 5, 6);
                    g.setFill(Color.web("#4fc3f7"));
                    g.fillRect(x, 32, 13, 9);
                }
            }
            case "F01" -> { // cheese wedge
                g.setFill(Color.web("#ffe082"));
                g.fillPolygon(new double[] {8, 56, 56}, new double[] {46, 22, 46}, 3);
                g.setFill(Color.web("#ffca28"));
                g.fillRect(8, 46, 48, 8);
                g.setFill(Color.web("#e6b800"));
                g.fillOval(38, 34, 7, 6);
                g.fillOval(26, 49, 5, 3);
                g.fillOval(48, 40, 4, 4);
            }
            default -> {
                g.setFill(Color.web("#90a4ae"));
                g.fillRoundRect(12, 12, 40, 40, 8, 8);
            }
        }
        return c;
    }

    private static void apple(GraphicsContext g, double x, double y, Color c) {
        g.setFill(c);
        g.fillOval(x - 11, y - 11, 22, 22);
        g.setFill(Color.rgb(255, 255, 255, 0.35));
        g.fillOval(x - 6, y - 7, 6, 5);
        g.setStroke(Color.web("#5d4037"));
        g.setLineWidth(2);
        g.strokeLine(x, y - 11, x + 2, y - 16);
        g.setFill(Color.web("#43a047"));
        g.fillOval(x + 2, y - 18, 8, 5);
    }
}
