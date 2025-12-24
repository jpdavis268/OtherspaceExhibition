package otherspace.core.session.scenes.mainmenu;

import com.google.gson.JsonObject;
import org.joml.primitives.Rectanglei;
import otherspace.core.engine.Color;
import otherspace.core.engine.Menu;
import otherspace.core.engine.Surface;
import otherspace.core.engine.guicomponents.*;
import otherspace.core.engine.guicomponents.Button;
import otherspace.core.engine.guicomponents.Component;
import otherspace.core.engine.utils.GenUtils;
import otherspace.core.engine.utils.IOUtils;
import otherspace.core.engine.utils.SaveUtils;
import otherspace.core.session.Drawer;
import otherspace.core.session.SettingsManager;
import otherspace.core.session.scenes.SceneManager;
import otherspace.core.session.scenes.world.GameScene;
import otherspace.core.session.window.KeyListener;
import otherspace.core.session.window.MouseListener;
import otherspace.core.session.window.Window;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.lwjgl.glfw.GLFW.*;

/**
 * Menu for modifying or selecting a save file.
 */
public class WorldSelect extends Menu {
    Component userPrompt = null;

    File[] saves = SaveUtils.getSaves();
    JsonObject[] saveManifests = SaveUtils.getSaveManifests(saves);
    long[] saveSizes = SaveUtils.getSaveSizes(saves);
    int selIndex = -1;

    Button playButton = new Button(
            256,
            64,
            "menu_worldselect_play",
            (_) -> SceneManager.changeScene(new GameScene(saves[selIndex])),
            Color.DARK_GREEN,
            Color.GREEN
    );

    Button renameButton = new Button(96, 32, "menu_worldselect_rename", (_) -> userPrompt = new InputDialogue(500, 100, "menu_worldselect_rename_prompt", (e) -> {
        InputDialogue info = (InputDialogue) e.getParent();

        // Verify new save name and rename directory to it.
        String inputName = IOUtils.verifyFileName(info.getStoredInput());
        if (selIndex >= 0 && selIndex < saves.length) {
            File toRename = saves[selIndex];
            String newName = "saves/" + inputName;
            boolean renamed = false;
            Path target = Paths.get(newName);

            while (!renamed) {
                if (Files.exists(target)) {
                    newName += "_";
                }
                else {
                    try {
                        Files.createDirectory(Paths.get(newName));
                    }
                    catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    renamed = true;
                }
            }

            try {
                Files.move(toRename.toPath(), target, REPLACE_EXISTING);

                // Reload save list (manifests, size, and selection index do not need to be reloaded).
                saves = SaveUtils.getSaves();
            }
            catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

        userPrompt = null;
    }, (_) -> userPrompt = null));

    Button copyButton = new Button(96, 32, "menu_worldselect_copy", (_) -> {
        if (selIndex >= 0 && selIndex < saves.length) {
            File toCopy = saves[selIndex];

            // Generate new save name.
            String newName = "saves/" + toCopy.getName();
            while (Files.exists(Paths.get(newName))) {
                newName += "_";
            }

            // Create clone of existing directory with new name.
            IOUtils.copyDirectory(toCopy, new File(newName));

            // Reload save list.
            saves = SaveUtils.getSaves();
            saveManifests = SaveUtils.getSaveManifests(saves);
            saveSizes = SaveUtils.getSaveSizes(saves);
            selIndex = -1;
        }
    });

    Button deleteButton = new Button(96, 32, "menu_worldselect_delete", (_) -> userPrompt = new ConfirmDialogue("menu_worldselect_delete_confirm", (_) -> {
        // If there is a save to delete, delete it.
        if (selIndex >= 0 && selIndex < saves.length) {
            IOUtils.deleteDirectory(saves[selIndex]);

            // Reload save list
            saves = SaveUtils.getSaves();
            saveManifests = SaveUtils.getSaveManifests(saves);
            saveSizes = SaveUtils.getSaveSizes(saves);
            selIndex = -1;
        }
        userPrompt = null;
    }, (_) -> userPrompt = null), Color.DARK_RED, Color.RED);

    ScrollableSurface saveList = new ScrollableSurface(400, saves.length * 41, 400, false, new Surface(400, saves.length * 41) {
        @Override
        public void draw(Drawer d) {
            setHeight(saves.length * 41);
            int gh = Window.getHeight();
            saveList.setMaxHeight(gh - 130);
            Color buttonDefault = new Color(0.2f, 0.2f, 0.2f, 0.4f);
            Color buttonOver = new Color(0.5f, 0.5f, 0.5f, 0.8f);

            for (int i = 0; i < saves.length; i++) {
                String text = saves[i].getName();
                int y = i * 41;

                Color drawCol = buttonDefault;
                if (GenUtils.isMouseOver(new Rectanglei(0, y - saveList.getCeiling(), 480, y - saveList.getCeiling() + 40))) {
                    drawCol = buttonOver;
                    if (MouseListener.checkPressed(GLFW_MOUSE_BUTTON_LEFT)) {
                        selIndex = i;
                    }
                }

                if (selIndex == i) {
                    drawCol = buttonOver;
                }

                d.setColor(drawCol);
                d.drawRect(new Rectanglei(0, y, 400, y + 40));

                d.setColor(Color.WHITE);
                d.setHalign(Drawer.H_CENTER);
                d.setValign(Drawer.V_MIDDLE);
                d.drawText(200, y + 20, text);
            }
        }
    });

    Surface saveInfo = new Surface(500, 500) {
        @Override
        public void draw(Drawer d) {
            int gw = Window.getWidth() - 410;
            int gh = Window.getHeight() - 80;
            setWidth(gw);
            setHeight(gh);

            d.setColor(Color.GRAY);
            d.drawRect(new Rectanglei(12, 31, width - 12, 32));
            if (selIndex >= 0 && selIndex < saves.length) {
                // Save Name
                d.setColor(Color.WHITE);
                d.setHalign(Drawer.H_LEFT);
                d.setValign(Drawer.V_TOP);
                d.drawText(12, 8, saves[selIndex].getName());

                // Get time and date format
                boolean dateFormat = SettingsManager.get("date_format").getAsBoolean();
                boolean timeFormat = SettingsManager.get("time_format").getAsBoolean();
                String unformattedDate = saveManifests[selIndex].get("date").getAsString();
                String unformattedTime = saveManifests[selIndex].get("time").getAsString();
                String[] dateInfo = unformattedDate.split("-");
                String[] timeInfo = unformattedTime.split("-");

                // Doing this every frame is inefficient, but there won't be enough
                // happening in the save selection screen for it to matter.
                int year = Integer.parseInt(dateInfo[0]);
                int month = Integer.parseInt(dateInfo[1]);
                int day = Integer.parseInt(dateInfo[2]);
                int hour = Integer.parseInt(timeInfo[0]);
                int minute = Integer.parseInt(timeInfo[1]);

                LocalDateTime timeStamp;
                try {
                    timeStamp = LocalDateTime.of(year, month, day, hour, minute);
                }
                catch (DateTimeException e) {
                    // If the data from the manifest is invalid, just use the current time.
                    timeStamp = LocalDateTime.now();
                }

                String dateDisplay;
                String timeDisplay;

                if (dateFormat) {
                    // dd/mm/yyyy
                    dateDisplay = timeStamp.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                }
                else {
                    // mm/dd/yyyy
                    dateDisplay = timeStamp.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
                }

                if (timeFormat) {
                    // 24 hour time.
                    timeDisplay = timeStamp.format(DateTimeFormatter.ofPattern("HH:mm"));
                }
                else {
                    // AM/PM time.
                    timeDisplay = timeStamp.format(DateTimeFormatter.ofPattern("hh:mm a"));
                }

                // Save Info
                String version = SettingsManager.getText("menu_worldselect_version") + saveManifests[selIndex].get("version").getAsString();
                d.drawText(12, 40, version);
                String playtime = SettingsManager.getText("menu_worldselect_playtime") + saveManifests[selIndex].get("playTime").getAsString();
                d.drawText(12, 60, playtime);
                String lastPlayed = SettingsManager.getText("menu_worldselect_last_played") + dateDisplay + ", " + timeDisplay;
                d.drawText(12, 80, lastPlayed);
                String fileSize = String.format("%.2f MB", saveSizes[selIndex] / 1000000f);
                String fileSizeInfo = SettingsManager.getText("menu_worldselect_file_size") + fileSize;
                d.drawText(12, 100, fileSizeInfo);

                // Buttons
                playButton.draw(d, width / 2, height - 128);
                renameButton.draw(d, width - 272, 64);
                copyButton.draw(d, width - 168, 64);
                deleteButton.draw(d, width - 64, 64);
            }
        }
    };

    Button openSaveFolder = new Button(256, 64, "menu_worldselect_folder", (_) -> {
        try {
            Path saves = Paths.get("saves");
            if (!Files.exists(saves)) {
                Files.createDirectory(saves);
            }

            // For some reason a warning gets printed to the console whenever the save folder is closed.
            // This doesn't seem to actually do anything, so I'll just put up with it for now.
            File saveFolder = saves.toFile();
            Desktop.getDesktop().open(saveFolder);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    });

    Button createWorldButton = new Button(256, 64, "menu_worldselect_create", (_) ->
            SceneManager.getCurrentScene().setCurrentMenu(new WorldCreate())
    );

    Button backButton = new Button(256, 64, "menu_back", (_) ->
            SceneManager.getCurrentScene().setCurrentMenu(new TitleScreen())
    );

    @Override
    public void draw(Drawer d) {
        int gw = Window.getWidth();
        int gh = Window.getHeight();

        // Draw a prompt instead if there is one.
        if (userPrompt != null) {
            userPrompt.draw(d, gw / 2, gh / 2);

            if (KeyListener.checkPressed(GLFW_KEY_ENTER)) {
                if (userPrompt instanceof ConfirmDialogue c) {
                    c.invokeAccept();
                }
            }

            if (KeyListener.checkPressed(GLFW_KEY_ESCAPE)) {
                if (userPrompt instanceof ConfirmDialogue c) {
                    c.invokeCancel();
                }
            }

            return;
        }

        // Draw header
        d.setColor(Color.DARK_GRAY);
        d.drawRect(new Rectanglei(0, 0, gw, 50));
        d.setHalign(Drawer.H_CENTER);
        d.setValign(Drawer.V_MIDDLE);
        d.setColor(Color.WHITE);
        d.drawText(gw / 2, 25, SettingsManager.getText("menu_worldselect_header"));

        // Draw save list
        d.setColor(new Color(0, 0, 0, 0.2f));
        d.drawRect(new Rectanglei(0, 50, 410, gh - 80));
        d.drawRect(new Rectanglei(0, 50, 400, gh - 80));
        saveList.draw(d, 0, 50);

        // Draw save info
        saveInfo.render(d, 410, 50);

        // Draw footer
        d.setColor(Color.DARK_GRAY);
        d.drawRect(new Rectanglei(0, gh - 80, gw, gh));
        backButton.draw(d, gw / 2 - 264, gh - 40);
        createWorldButton.draw(d, gw / 2, gh - 40);
        openSaveFolder.draw(d, gw / 2 + 264, gh - 40);

        // Return to title menu if ESC is pressed.
        if (KeyListener.checkPressed(GLFW_KEY_ESCAPE)) {
            backButton.invokeCallback();
        }
    }
}
