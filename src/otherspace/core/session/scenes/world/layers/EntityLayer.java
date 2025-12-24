package otherspace.core.session.scenes.world.layers;

import org.joml.Vector2d;
import org.joml.primitives.Rectangled;
import org.joml.primitives.Rectanglei;
import otherspace.core.engine.Color;
import otherspace.core.engine.Layer;
import otherspace.core.engine.world.entities.Entity;
import otherspace.core.session.Drawer;
import otherspace.core.session.scenes.SceneManager;
import otherspace.core.session.scenes.world.GameScene;
import otherspace.core.session.scenes.world.World;

import java.util.Arrays;
import java.util.Comparator;

public class EntityLayer extends Layer {
    @Override
    public void draw(Drawer d) {
        super.draw(d);
        Vector2d cameraPos = SceneManager.getCurrentScene().getCamera().getPosition();

        Entity[] entityDepthList = World.getEntityList().toArray(new Entity[0]);
        Arrays.sort(entityDepthList, Comparator.comparingInt(Entity::getDepth).reversed());

        for (Entity e : entityDepthList) {
            e.drawSelf(d);
        }

        if (GameScene.isDebugEnabled()) {
            for (Entity e : entityDepthList) {
                Rectangled bbox = e.getBounds();
                Rectanglei boxLocation = new Rectanglei((int) (bbox.minX * 32), (int) (bbox.minY * 32), (int) (bbox.maxX * 32), (int) (bbox.maxY * 32));
                boxLocation.translate((int) (-cameraPos.x * 32), (int) (-cameraPos.y * 32));
                d.setColor(new Color(1, 1, 1, 0.2f));
                d.drawRect(boxLocation);
            }
        }
    }
}
