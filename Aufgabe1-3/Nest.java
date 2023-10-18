import java.awt.*;
// Modularisierungseinheit: Klasse
// Eine Erweiterung der Tile-Klasse, die ein Untertyp von Entity ist.

//extends Tiles with custom methodes
public class Nest extends Tile {
    /**
     * Initializes the Nest at the given position
     *
     * @param position Position where Nest is located.
     */
    public Nest(Vector position) {
        super(position);
    }

    /**
     * Nest needs no updates because it does not change colors.
     *
     * @return true
     */
    @Override
    public void update() {
        return;
    }
}
