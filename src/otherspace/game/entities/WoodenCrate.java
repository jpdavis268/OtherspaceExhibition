package otherspace.game.entities;

import org.joml.Vector2d;
import org.joml.Vector2i;
import org.joml.primitives.Rectanglei;
import otherspace.core.engine.Color;
import otherspace.core.engine.world.entities.TileEntity;
import otherspace.core.engine.world.entities.components.Container;
import otherspace.core.engine.world.entities.components.GUI;
import otherspace.core.engine.world.items.Inventory;
import otherspace.core.engine.world.items.ItemDrop;
import otherspace.core.engine.world.items.ItemStack;
import otherspace.core.session.Drawer;
import otherspace.core.session.SettingsManager;
import otherspace.game.items.Items;

/**
 * Wooden crate that can be used to store items.
 */
public class WoodenCrate extends TileEntity {
    public WoodenCrate(Vector2d position) {
        super(position);
        addComponent(new Container<>(this, new Inventory(30)));
        addComponent(new GUI<>(this, new Rectanglei(-213, -152, 213, 152), (d) -> {
            d.setColor(Color.WHITE);
            d.setHalign(Drawer.H_LEFT);
            d.setValign(Drawer.V_TOP);
            d.drawText(4, 4, SettingsManager.getText("wooden_crate_gui_header"));

            getComponent(Container.class).getInventory(0).draw(d, new Vector2i(4, 24), 3, false);

            d.setColor(Color.WHITE);
            d.setHalign(Drawer.H_LEFT);
            d.setValign(Drawer.V_TOP);
            d.drawText(4, 156, SettingsManager.getText("player_inventory"));
            Player.getPlayerInventory().draw(d, new Vector2i(4, 176), 3, false);
        }));
    }

    @Override
    public float getHardness() {
        return 1;
    }

    @Override
    public ItemDrop[] returns() {
        return new ItemDrop[] {
                new ItemDrop(new ItemStack(Items.WOODEN_CRATE, 1), 1)
        };
    }
}
