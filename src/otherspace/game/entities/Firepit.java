package otherspace.game.entities;

import com.google.gson.JsonObject;
import org.joml.Vector2d;
import org.joml.Vector2i;
import org.joml.primitives.Rectanglei;
import otherspace.core.engine.Color;
import otherspace.core.engine.world.entities.Entity;
import otherspace.core.engine.world.entities.TileEntity;
import otherspace.core.engine.world.entities.components.Container;
import otherspace.core.engine.world.entities.components.EntityComponent;
import otherspace.core.engine.world.entities.components.GUI;
import otherspace.core.engine.world.items.InputInventory;
import otherspace.core.engine.world.items.Item;
import otherspace.core.engine.world.items.ItemDrop;
import otherspace.core.engine.world.items.ItemStack;
import otherspace.core.session.Drawer;
import otherspace.core.session.SettingsManager;
import otherspace.core.session.SoundManager;
import otherspace.core.session.scenes.world.layers.LightingLayer;
import otherspace.game.Assets;
import otherspace.game.items.IBurnable;
import otherspace.game.items.Items;

/**
 * Firepit that provides an early game light source and can cook meals.
 */
public class Firepit extends TileEntity {
    private float lightRange;
    private float flickerDir;
    private SoundManager.AudioSource soundSource;

    private final BurnComponent burnComponent;

    public Firepit(Vector2d position) {
        super(position);
        lightRange = 8;
        flickerDir = 1;
        soundSource = null;

        addComponent(new Container<>(this, new InputInventory(1, IBurnable.class)));
        burnComponent = new  BurnComponent(this);
        addComponent(burnComponent);

        addComponent(new GUI<>(this, new Rectanglei(-213, -164, 213, 164), (d) -> {
            d.setColor(Color.WHITE);
            d.setHalign(Drawer.H_LEFT);
            d.setValign(Drawer.V_TOP);
            d.drawText(4, 4, SettingsManager.getText("firepit_gui_header"));
            getComponent(Container.class).getInventory(0).draw(d, new Vector2i(193, 80), 1, false);

            d.drawValueBar(
                    new Rectanglei(181, 137, 245, 147),
                    burnComponent.burnTime,
                    3600,
                    Color.BLACK,
                    Color.RED,
                    Color.ORANGE,
                    false,
                    true,
                    true
            );

            d.setColor(Color.WHITE);
            d.setHalign(Drawer.H_LEFT);
            d.setValign(Drawer.V_TOP);
            d.drawText(4, 180, SettingsManager.getText("player_inventory"));
            Player.getPlayerInventory().draw(d, new Vector2i(4, 200), 3, false);
        }));
    }

    @Override
    public void update() {
        super.update();

        // Consume fuel or burn out if timer reaches 0.
        if (burnComponent.burnTime <= 0) {
            // Check if there is any fuel left, and burn some if there is.
            InputInventory fuelInput = (InputInventory) getComponent(Container.class).getInventory(0);
            if (fuelInput.get(0).stackSize > 0) {
                fuelInput.get(0).stackSize--;
                burnComponent.burnTime = 3600;
                Item fuel = fuelInput.get(0).getItem();
                if (fuel != null) {
                    burnComponent.burnRate = 60 / fuel.getTag(IBurnable.class).getBurntime();
                }
                burnComponent.active = true;
            }
            else if (burnComponent.active) {
                burnComponent.active = false;
            }
        }
        else {
            // Decrement burn timer.
            burnComponent.burnTime -= burnComponent.burnRate;
            burnComponent.active = true;
        }

        // Show fire if active.
        if (burnComponent.active) {
            spriteSpeed = 8;
            if (spriteFrame >= getSprite().getFrames() || spriteFrame <= 0) {
                spriteFrame = 1;
            }
        }
        else {
            spriteFrame = 0;
            spriteSpeed = 0;
        }

        // Flickering
        if (lightRange < 7.8f || lightRange > 8.2f) {
            flickerDir = -flickerDir;
        }
        lightRange += 0.01f * flickerDir;

        // Audio management
        if (burnComponent.active && soundSource == null) {
            soundSource = SoundManager.playSoundAt(position, Assets.firepitSound, true, 1, 8, 32, 1);
        }
        else if (!burnComponent.active && soundSource != null) {
            SoundManager.stopSound(soundSource);
            soundSource = null;
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        if (soundSource != null) {
            SoundManager.stopSound(soundSource);
        }
    }

    @Override
    public void drawSelf(Drawer d) {
        super.drawSelf(d);

        // Draw light
        if (burnComponent.active) {
            LightingLayer.drawLight(position, lightRange, 1);
        }
    }

    @Override
    public float getHardness() {
        return 1;
    }

    @Override
    public ItemDrop[] returns() {
        return new ItemDrop[] {
                new ItemDrop(new ItemStack(Items.FIREPIT, 1), 1)
        };
    }

    private static class BurnComponent extends EntityComponent<Firepit> {
        private float burnTime;
        private float burnRate;
        private boolean active;

        public BurnComponent(Entity parent) {
            super((Firepit) parent);
            burnTime = 0;
            burnRate = 0;
            active = false;
        }

        @Override
        public JsonObject serialize() {
            JsonObject stats = new JsonObject();
            stats.addProperty("burnTime", burnTime);
            stats.addProperty("burnRate", burnRate);
            stats.addProperty("active", active);
            return stats;
        }

        @Override
        public void deserialize(JsonObject json) {
            burnTime = json.get("burnTime").getAsFloat();
            burnRate = json.get("burnRate").getAsFloat();
            active = json.get("active").getAsBoolean();
        }
    }
}
