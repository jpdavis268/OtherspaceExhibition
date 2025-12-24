package otherspace.core.session;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import otherspace.core.engine.utils.IOUtils;

/**
 * Singleton class responsible for loading and storing settings.
 */
public class SettingsManager {
    private static SettingsManager singleton;

    private final JsonObject SETTINGS;
    private JsonObject textMap;

    public SettingsManager() {
        singleton = this;
        SETTINGS = IOUtils.loadJson("resources/settings.json");
        textMap = IOUtils.loadJson("resources/lang/" + get("language").getAsString() + ".json");
    }

    /**
     * Return the value of a setting, as a JsonElement.
     *
     * @param setting Setting to query.
     * @return JsonElement containing value.
     */
    public static JsonElement get(String setting) {
        return singleton.SETTINGS.get(setting);
    }

    /**
     * Set the value of a setting.
     *
     * @param setting Setting to set.
     * @param value Setting value of setting to set setting to set. Setting.
     */
    public static void set(String setting, JsonElement value) {
        // Not very clean, but better than writing a whole file if we don't have to.
        if (!singleton.SETTINGS.get(setting).toString().equals(value.toString())) {
            singleton.SETTINGS.add(setting, value);
            // Saving the settings object every time something is changed may not be the best idea, but I'll leave it for now.
            IOUtils.saveJson(singleton.SETTINGS, "resources/settings.json", true);
        }
    }

    /**
     * Retrieve text from the current language using a key.
     *
     * @param key Key of text on lang map.
     * @return Text at key, or the key itself if no text was found.
     */
    public static String getText(String key) {
        JsonElement text = singleton.textMap.get(key);
        return text == null ? key : text.getAsString();
    }

    /**
     * Convenience method for getting a keybind.
     * This is kind of inefficient, but hopefully this won't be called enough for that to matter.
     *
     * @param bindName Name of keybind variable in settings.
     * @return Keycode for bind.
     */
    public static int getKeybind(String bindName) {
        return get(bindName).getAsInt();
    }

    /**
     * Reload language map.
     */
    public static void reloadTextMap() {
        singleton.textMap = IOUtils.loadJson("resources/lang/" + SettingsManager.get("language").getAsString() + ".json");
    }
}
