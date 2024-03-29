import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
// Modularisierungseinheit: Klasse

// handles the entire logic that depends on grid operations
public class Grid {

    // GOOD: objektorientierter Teil: hoher Zusammenhang zwischen Grid und Tiles.
    // Enge Verbindung der beiden Klassen um die Verwaltung von Tiles für den Zustands des Grids zu speichern
    private final Map<Vector, Tile> map;
    private Vector startPoint;
    private Vector endPoint;

    /**
     * Initializes the Grid with a given size.
     * Generates basic entities in the Grid.
     * The size of the grid might increase if Ants reach the borders.
     *
     * @param width  Width of initial grid, must be > 0
     * @param height Height of initial grid, must be > 0
     */
    public Grid(int width, int height) {
        startPoint = new Vector(-1 * width / 2, -1 * height / 2);
        endPoint = new Vector(width / 2, height / 2);
        this.map = new ConcurrentHashMap<>();

        int nestCounter = (int) (Math.random() * 2) + 4;
        int foodCounter = (int) (Math.random() * 30) + 20;
        int antsPerNest = 500;
        int obstacleCounter = (int) (Math.random() * 15) + 5;

        // STYLE: objektorientierte Programmierung
        // hohe Objektkopplung. Generator braucht unbedingt eine Grid-Instanz.
        Generator generator = new Generator(this, nestCounter, foodCounter, antsPerNest, obstacleCounter);
        generator.generateTilesForChunk(startPoint, endPoint);
        System.out.println("created everything!");
    }

    /**
     * Returns the entities in a map.
     *
     * @return Entity Map. Map != null
     */
    public Map<Vector, Tile> getMap() {
        return map;
    }

    /**
     * Handles the update process of the Grid.
     * Tiles which are not needed anymore are removed from the map.
     */
    //STYLE: wir haben die update Funktion vom Grid refactored als parallelem Teil.
    // Ziel dahinter ist, die Berechnung der ganzen Simulation zu beschleunigen.
    public void update() {
        // update all entities

        // BAD: Wir müssen die Nests extra behandeln. Das hat den Grund, dass die Nests die Updates ihrer Ants triggert.
        // Da alle Entities in der Map durcheinander gespeichert sind, könnte es sein dass ein Tile vor den Ants das Update macht und es deswegen zu Inkonsistenzen kommt.
        // Dadurch dass wir zuerst die Nests updaten, werden am Anfang die Ants geupdated und dann erst der Rest.
        // Man sollte versuchen, alle Tiles mit einem Schleiendurchlauf und ohne extra Bedingung für Nests zu updaten.
        List<Vector> removingItems = new CopyOnWriteArrayList<>();
        getNests().parallelStream().forEach(nest -> {
            if (nest.update()) {
                removingItems.add(nest.getPosition());
                System.out.println("total farmed food of nest" + nest + "  was " + nest.getTotalFarmedFood() + " with a total amout of ants of " + nest.getTotalAntsCreated());
            }
        });

        // GOOD: Durch die Verwendung von dynamischen Binden werden von allen Entities die update Methoden aufgerufen
        map.entrySet().parallelStream().forEach(entry -> {
            if (entry.getValue() instanceof Nest) {
                return;
            }

            if (entry.getValue().update()) {
                removingItems.add(entry.getKey());
            }
        });

        removingItems.forEach(map::remove);
        generateNewChunks();
    }

    /**
     * Returns a List of all Nests on the Grid.
     *
     * @return List of Nests, which is != null
     */
    public ArrayList<Nest> getNests() {
        ArrayList<Nest> nests = new ArrayList<>();

        map.forEach((key, value) -> {
            if (value instanceof Nest nest) {
                nests.add(nest);
            }
        });

        return nests;
    }

    /**
     * Returns a List of all Ants on the Grid.
     *
     * @return List of Ants, which is != null
     */
    public ArrayList<Ant> getAnts() {
        ArrayList<Ant> ants = new ArrayList<>();
        getNests().forEach(nest -> ants.addAll(nest.getAnts()));
        return ants;
    }

    /**
     * Returns all entities which need an update in a List.
     * Map is build in this way:
     * normal tiles,
     * ants,
     * foodSources, obstacles & nests
     *
     * @return Entity List != null
     */
    public ArrayList<Entity> queue() {
        ArrayList<Entity> entities = new ArrayList<>();
        ArrayList<Entity> tilesWithScent = new ArrayList<>();
        ArrayList<Entity> otherTiles = new ArrayList<>(); // Nests, FoodSources, Obstacles

        map.forEach((key, value) -> {
            if (value.getCurrentStink(null) <= 1f) {
                tilesWithScent.add(value);
            } else {
                otherTiles.add(value);
            }
        });

        // add all entities in a specific order
        entities.addAll(tilesWithScent);
        entities.addAll(getAnts());
        entities.addAll(otherTiles);

        return entities;
    }

    /**
     * Returns a tile at a given position.
     * If the Tile at this position is not already in the map, it gets created.
     *
     * @param position Position, must be != null
     * @return Tile != null
     */
    public Tile getTile(Vector position) {
        Tile tile = map.get(position);
        if (tile == null) {
            Tile newTile = new Tile(position);
            putTile(newTile);
            return newTile;
        }
        return tile;
    }

    /**
     * Removes a Tile from the Map.
     * If the Tile does not exist, it has no effects.
     *
     * @param tile Tile, must be != null
     */
    public void removeTile(Tile tile) {
        map.remove(tile.getPosition());
    }

    /**
     * Puts a Tile into the Map.
     * The key is retrieved from the Tile.
     *
     * @param tile tile, must be != null
     */
    private void putTile(Tile tile) {
        map.put(tile.getPosition(), tile);
    }

    /**
     * Checks if there is any ant which want to go to unknown territory.
     * If so, the map increases and new Obstacles, Nests and FoodSources are getting generated.
     */

    private void generateNewChunks() {
        int chunkSize = 20;

        Boolean[] extendSides = new Boolean[4];
        ArrayList<Ant> ants = getAnts();

        extendSides[0] = ants.stream().anyMatch(ant -> ant.getPosition().getX() == startPoint.getX());
        extendSides[1] = ants.stream().anyMatch(ant -> ant.getPosition().getX() == endPoint.getX());
        extendSides[2] = ants.stream().anyMatch(ant -> ant.getPosition().getY() == startPoint.getY());
        extendSides[3] = ants.stream().anyMatch(ant -> ant.getPosition().getY() == endPoint.getY());

        // no need to update chunks
        if (!List.of(extendSides).contains(true)) {
            return;
        }

        Generator generator = new Generator(this, 0, 7, 0, 2);
        Vector newStartPoint, newChunkStartPoint, newEndPoint, newChunkEndPoint;
        if (extendSides[0]) {
            // extend left
            newStartPoint = new Vector(startPoint.getX() - chunkSize, startPoint.getY());
            // newEndPoint = endPoint;

            newChunkStartPoint = newStartPoint;
            newChunkEndPoint = new Vector(startPoint.getX(), endPoint.getY());

            startPoint = newStartPoint;

            generator.generateTilesForChunk(newChunkStartPoint, newChunkEndPoint);
        }

        if (extendSides[1]) {
            // extend right
            // newStartPoint = startPoint;
            newEndPoint = new Vector(endPoint.getX() + chunkSize, endPoint.getY());

            newChunkStartPoint = new Vector(endPoint.getX(), startPoint.getY());
            newChunkEndPoint = endPoint;

            endPoint = newEndPoint;

            generator.generateTilesForChunk(newChunkStartPoint, newChunkEndPoint);
        }

        if (extendSides[2]) {
            // extend bottom
            newStartPoint = new Vector(startPoint.getX(), startPoint.getY() - chunkSize);
            // newEndPoint = endPoint;

            newChunkStartPoint = newStartPoint;
            newChunkEndPoint = new Vector(endPoint.getX(), startPoint.getY());

            startPoint = newStartPoint;

            generator.generateTilesForChunk(newChunkStartPoint, newChunkEndPoint);
        }

        if (extendSides[3]) {
            // extend top
            // newStartPoint = startPoint;
            newEndPoint = new Vector(endPoint.getX(), endPoint.getY() + chunkSize);

            newChunkStartPoint = new Vector(startPoint.getX(), endPoint.getY());
            newChunkEndPoint = newEndPoint;

            endPoint = newEndPoint;

            generator.generateTilesForChunk(newChunkStartPoint, newChunkEndPoint);
        }
    }

    /**
     * Generates 15 random FoodSources on the Grid.
     */
    //STYLE: neue Funktion für funktionalem Teil von Simulation
    public void generateFoodSources() {
        // GOOD: objektorientierter Teil: hoher Zusammenhang zwischen Grid und Generator, da der Generator eine Instanz eines Grids benötigt.
        Generator generator = new Generator(this, 0, 15, 0, 0);
        generator.generateTilesForChunk(startPoint, endPoint);
    }

    /**
     * Generates a random Nests on the Grid.
     */
    //STYLE: neue Funktion für funktionalem Teil von Simulation
    public void generateNests() {
        Generator generator = new Generator(this, 1, 0, 200, 0);
        generator.generateTilesForChunk(startPoint, endPoint);
    }
}
