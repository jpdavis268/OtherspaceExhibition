package otherspace.core.engine.utils;

import org.joml.Vector2d;
import org.joml.Vector2i;
import org.joml.primitives.Rectangled;
import org.joml.primitives.Rectanglei;
import otherspace.core.engine.world.entities.Entity;
import otherspace.core.engine.world.entities.MobileEntity;
import otherspace.core.session.scenes.world.Chunk;

import java.util.HashSet;

/**
 * Utility class for handling collisions.
 */
public final class CollisionUtils {
    private CollisionUtils() {}

    /**
     * Get the entities within a radius of chunks around a point.
     *
     * @param origin Center of area to check.
     * @param range How far to check, in chunks (Set to 0 to only check the chunk under the point).
     * @return Set of all entities within the given chunks.
     */
    public static HashSet<Entity> getNearbyEntities(Vector2d origin, int range) {
        HashSet<Chunk> chunkSet = getNearbyChunks(origin, range);
        HashSet<Entity> entities = new HashSet<>();
        for (Chunk c : chunkSet) {
            entities.addAll(c.localEntities);
        }
        return entities;
    }

    /**
     * Get the entities near a given point (within a radius of 2 chunks).
     *
     * @param origin Center of area to check.
     * @return Set of nearby entities.
     */
    public static HashSet<Entity> getNearbyEntities(Vector2d origin) {
        return getNearbyEntities(origin, 5);
    }

    /**
     * Get the nine chunks surrounding a point in the world.
     *
     * @param origin Center of area to check.
     * @return 9 Chunks immediately surrounding area (an index is null if no chunk exists there).
     */
    public static HashSet<Chunk> getNearbyChunks(Vector2d origin, int range) {
        int cX = (int) Math.floor(origin.x / 16);
        int cY = (int) Math.floor(origin.y / 16);
        int dia = 1 + (range * 2);
        HashSet<Chunk> chunks = new HashSet<>();
        for (int x = 0; x < dia; x++) {
            int xOffset = cX - range + x;
            for (int y = 0; y < dia; y++) {
                int yOffset = cY - range + y;
                Chunk chunk = Chunk.getChunk(new Vector2i(xOffset, yOffset));
                if (chunk != null) {
                    chunks.add(chunk);
                }
            }
        }

        return chunks;
    }

    /**
     * Check for collisions with entities at a specific point.
     *
     * @param location Point to check.
     * @return First entity detected at point, if any exists.
     */
    @SuppressWarnings("unchecked")
    public static <E extends Entity> E collisionPoint(Class<E> type, Vector2d location) {
        HashSet<Entity> candidates = getNearbyEntities(location);

        for (Entity e : candidates) {
            if (type.isInstance(e) && e.getBounds().containsPoint(location)) {
                return (E) e;
            }
        }

        return null;
    }

    /**
     * Get a list of every entity of a given type that collides with this rectangle.
     *
     * @param entityList Superset to check.
     * @param area Area to check for collisions.
     * @return Set of entities of said type within the collision area.
     */
    @SuppressWarnings("unchecked")
    public static <E> HashSet<E> collisionRectList(HashSet<Entity> entityList, Class<E> type, Rectangled area) {
        HashSet<E> out = new HashSet<>();
        for (Entity e : entityList) {
            if (e.getBounds().intersectsRectangle(area) && type.isInstance(e)) {
                out.add((E) e);
            }
        }

        return out;
    }

    /**
     * Check if an area collides with any tiles in a set of chunks.
     *
     * @param chunks Set of chunks.
     * @param area Area to check.
     * @return Whether any solid tiles intersect this area.
     */
    public static boolean checkTileCollision(HashSet<Chunk> chunks, Rectangled area) {
        for (Chunk c : chunks) {
            Rectangled toCheck = new Rectangled();
            area.intersection(new Rectangled(c.chunkCoords.x * 16d, c.chunkCoords.y * 16d, c.chunkCoords.x * 16d + 16, c.chunkCoords.y * 16d + 16), toCheck);
            if (toCheck.isValid()) {
                toCheck.translate(-c.chunkCoords.x * 16, -c.chunkCoords.y * 16);
                Rectanglei tiles = new Rectanglei((int) toCheck.minX, (int) toCheck.minY, (int) Math.ceil(toCheck.maxX), (int) Math.ceil(toCheck. maxY));
                for (int i = tiles.minX; i < tiles.maxX; i++) {
                    for (int j = tiles.minY; j < tiles.maxY; j++) {
                        if (c.getTileData(Chunk.STM).getAbsoluteTile(i, j) > -1) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Check if an area collides with any entities in a set of entities.
     *
     * @param entities Set of entities.
     * @param area Area to check.
     * @return Whether any entities intersect this area.
     */
    public static boolean checkEntityCollision(HashSet<Entity> entities, Rectangled area, boolean includeMobile) {
        for (Entity e : entities) {
            if (e.getBounds().intersectsRectangle(area) && (includeMobile || !(e instanceof MobileEntity))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determine where an entity would stop moving, respecting collisions.
     *
     * @param entity Entity to move.
     * @param destination Destination of movement.
     * @param speed Speed of movement.
     */
    public static void moveAndCollide(Entity entity, Vector2d destination, double speed) {
        // This is absolutely terrible and will not hold up if anything moves faster than a few meters per second,
        // but it will do until a more robust collision detection system is implemented.
        HashSet<Chunk> nearby = getNearbyChunks(entity.position, 1);
        HashSet<Entity> nearbyEntities = getNearbyEntities(entity.position);
        nearbyEntities.remove(entity);
        int steps = (int) Math.ceil(speed);

        double vx = (entity.position.x - destination.x) / steps;
        double vy = (entity.position.y - destination.y) / steps;

        for (int i = 0; i < steps; i++) {
            if (Chunk.getChunk(new Vector2i((int) ((entity.position.x - vx) / 16), (int) (entity.position.y / 16))) != null) {
                if (!checkTileCollision(nearby, new Rectangled(entity.getBounds()).translate(-vx, 0)) && !checkEntityCollision(nearbyEntities, new Rectangled(entity.getBounds()).translate(-vx, 0), false)) {
                    entity.position.x -= vx;
                }
            }

            if (Chunk.getChunk(new Vector2i((int) (entity.position.x / 16), (int) ((entity.position.y - vy) / 16))) != null) {
                if (!checkTileCollision(nearby, new Rectangled(entity.getBounds()).translate(0, -vy)) && !checkEntityCollision(nearbyEntities, new Rectangled(entity.getBounds()).translate(0, -vy), false)) {
                    entity.position.y -= vy;
                }
            }
        }
    }
}
