package fr.tp.vending.ui;

import javafx.scene.paint.Color;

/** How a product is drawn: its shape and colours. Pure decoration, the model does not know it. */
record Look(Shape shape, Color main, Color accent) {

    enum Shape { CAN, BOTTLE, BAG, BAR }

    static Look of(String name) {
        return switch (name) {
            case "Eau" -> new Look(Shape.BOTTLE, Color.web("#7ec8f0"), Color.web("#1565c0"));
            case "Eau gazeuse" -> new Look(Shape.BOTTLE, Color.web("#8fd8a8"), Color.web("#2e7d32"));
            case "Thé glacé" -> new Look(Shape.BOTTLE, Color.web("#e0a040"), Color.web("#6d3b00"));
            case "Cola" -> new Look(Shape.CAN, Color.web("#d32f2f"), Color.WHITE);
            case "Cola zéro" -> new Look(Shape.CAN, Color.web("#222222"), Color.web("#e53935"));
            case "Orange" -> new Look(Shape.CAN, Color.web("#fb8c00"), Color.web("#fff3e0"));
            case "Citron" -> new Look(Shape.CAN, Color.web("#fdd835"), Color.web("#2e7d32"));
            case "Café froid" -> new Look(Shape.CAN, Color.web("#6d4c41"), Color.web("#ffe0b2"));
            case "Chips" -> new Look(Shape.BAG, Color.web("#fbc02d"), Color.web("#c62828"));
            case "Bretzels" -> new Look(Shape.BAG, Color.web("#1e88e5"), Color.web("#ffeb3b"));
            case "Cookies" -> new Look(Shape.BAG, Color.web("#8d6e63"), Color.web("#fff8e1"));
            case "Popcorn" -> new Look(Shape.BAG, Color.web("#e53935"), Color.web("#fffde7"));
            case "Barre choco" -> new Look(Shape.BAR, Color.web("#5d4037"), Color.web("#ffd54f"));
            case "Gaufre" -> new Look(Shape.BAR, Color.web("#f9a825"), Color.web("#5d4037"));
            case "Barre céréales" -> new Look(Shape.BAR, Color.web("#7cb342"), Color.web("#ffffff"));
            default -> new Look(Shape.BAR, Color.web("#8e24aa"), Color.WHITE);
        };
    }
}
