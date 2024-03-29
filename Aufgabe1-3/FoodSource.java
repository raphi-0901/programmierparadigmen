// Modularisierungseinheit: Klasse
// Eine Erweiterung der Tile-Klasse, die ein Untertyp von Entity ist.

import java.awt.*;

// This class extends from Tile
// It describes the FoodSources
public class FoodSource extends Tile {
    private static final Color color = Color.GREEN;
    private int foodAmount = 200;

    /**
     * Initializes a FoodSource at the given position.
     *
     * @param position Position of FoodSource
     */
    public FoodSource(Vector position) {
        super(position);
    }

    /**
     * Returns a static stink value.
     *
     * @return 100
     */
    @Override
    public float getCurrentStink(Nest nest) {
        return 100;
    }

    /**
     * Returns the color of a FoodSource.
     *
     * @return Color.GREEN
     */
    @Override
    public Color getColor() {
        return new Color(color.getRed(), color.getGreen(), color.getBlue());
    }

    /**
     * Decreases the amount of left food.
     *
     * @return true if the foodAmount is larger than 0.
     */
    public boolean decreaseFoodAmount() {
        foodAmount--;
        return foodAmount > 0;
    }

    /**
     * Update function for a FoodSource.
     *
     * @return True if there is foodAmount left.
     */
    @Override
    public boolean update() {
        return foodAmount < 0;
    }
}
