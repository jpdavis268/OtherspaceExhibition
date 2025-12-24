package otherspace.game;

import org.joml.Vector2i;
import otherspace.core.engine.AudioGroup;
import otherspace.core.engine.Font;
import otherspace.core.engine.Sound;
import otherspace.core.engine.Sprite;
import otherspace.core.registry.FontRegistry;
import otherspace.core.registry.SoundRegistry;
import otherspace.core.registry.SpriteRegistry;
import otherspace.core.session.SettingsManager;

/**
 * Store references to core game assets.
 */
public abstract class Assets {
    // Font
    private static final FontRegistry fontRegistry = new FontRegistry();
    public static final Font DEFAULT_FONT = fontRegistry.register(new Font("resources/fonts/DejaVuSans.ttf", 20));

    // Sprites
    private static final SpriteRegistry spriteRegistry = new SpriteRegistry();
    public static final Sprite otherspaceLogo = spriteRegistry.register(new Sprite("resources/sprites/otherspaceLogo.png", 1, new Vector2i(350, 64)));
    public static final Sprite tileGrid = spriteRegistry.register(new Sprite("resources/sprites/tileGrid.png", 1, new Vector2i()));
    public static final Sprite selector = spriteRegistry.register(new Sprite("resources/sprites/selector.png", 1, new Vector2i()));
    public static final Sprite sandboxBrushIcon = spriteRegistry.register(new Sprite("resources/sprites/sandboxBrushIcon.png", 1, new Vector2i(32, 32)));
    public static final Sprite healthBar = spriteRegistry.register(new Sprite("resources/sprites/healthBar.png", 1, new Vector2i(96, 16)));

    // Audio Groups
    // If the initialization phase is ever modified so that this is called before the settings manager is created,
    // this will throw an NPE, but for now it's fine.
    public static final AudioGroup ambienceGroup = new AudioGroup(SettingsManager.get("ambience_volume").getAsFloat());
    public static final AudioGroup entityGroup = new AudioGroup(SettingsManager.get("entity_volume").getAsFloat());
    public static final AudioGroup tileGroup = new AudioGroup(SettingsManager.get("tile_volume").getAsFloat());
    public static final AudioGroup uiGroup = new AudioGroup(SettingsManager.get("ui_volume").getAsFloat());

    // Sounds
    private static final SoundRegistry soundRegistry = new SoundRegistry();
    public static final Sound uiSelSound = soundRegistry.register(new Sound("resources/audio/ui/uiSel.ogg", uiGroup));
    public static final Sound forestAmbienceDay = soundRegistry.register(new Sound("resources/audio/ambience/forestAmbienceDay.ogg", ambienceGroup));
    public static final Sound forestAmbienceNight = soundRegistry.register(new Sound("resources/audio/ambience/forestAmbienceNight.ogg", ambienceGroup));
    public static final Sound firepitSound = soundRegistry.register(new Sound("resources/audio/entity/firepit.ogg", entityGroup));
    public static final Sound footstepsGrass = soundRegistry.register(new Sound("resources/audio/entity/footstepsGrass.ogg", entityGroup));
    public static final Sound footstepsSandDirt = soundRegistry.register(new Sound("resources/audio/entity/footstepsSandDirt.ogg", entityGroup));
    public static final Sound footstepsShallowWater = soundRegistry.register(new Sound("resources/audio/entity/footstepsShallowWater.ogg", entityGroup));;
    public static final Sound footstepsDeepWater = soundRegistry.register(new Sound("resources/audio/entity/footstepsDeepWater.ogg", entityGroup));;
    public static final Sound footstepsStone = soundRegistry.register(new Sound("resources/audio/entity/footstepsStone.ogg", entityGroup));;
    public static final Sound footstepsWood = soundRegistry.register(new Sound("resources/audio/entity/footstepsStone.ogg", entityGroup));

    /**
     * Invoke initializers.
     */
    public static void register() {}
}
