package otherspace.core.session.scenes.world.ui;

import org.joml.primitives.Rectanglei;
import otherspace.core.engine.Color;
import otherspace.core.engine.Font;
import otherspace.core.engine.Surface;
import otherspace.core.engine.guicomponents.ScrollableSurface;
import otherspace.core.engine.utils.StringUtils;
import otherspace.core.session.Drawer;
import otherspace.core.session.scenes.world.ConsoleCommand;
import otherspace.core.session.scenes.world.GameScene;
import otherspace.core.session.scenes.world.InputHandler;
import otherspace.core.session.window.KeyListener;
import otherspace.core.session.window.Window;

import java.util.ArrayList;
import java.util.LinkedList;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Displays and handles the chat/console.
 */
public class ChatBox {
    private static ChatBox singleton;

    private static final Color histColor = new Color(0, 0, 0, 0.6f);
    private static final Color inputColor = new Color(0, 0, 0, 0.8f);

    private boolean enabled;
    private StringBuilder currentLine;
    private int inputPosition;
    private int inputPositionOffset;
    private int historyScroll;
    private final LinkedList<String> messageHistory;
    private final ArrayList<Integer> chatRecentTimer;
    private final LinkedList<String> inputHistory;
    private boolean clearFlag;

    private final ScrollableSurface chatHistory;
    private final Surface recentSurface;
    private final Surface chatInput;

    public ChatBox() {
        singleton = this;

        enabled = false;
        currentLine = new StringBuilder(255);
        inputPosition = 0;
        inputPositionOffset = 0;
        historyScroll = 0;
        messageHistory = new LinkedList<>();
        chatRecentTimer = new ArrayList<>();
        inputHistory = new LinkedList<>();
        clearFlag = false;

        inputHistory.push("");

        chatHistory = new ScrollableSurface(600, 300, 300, true, new Surface(600, 300) {
            @Override
            public void draw(Drawer d) {
                d.setColor(histColor);
                d.drawRect(new Rectanglei(0, 0, this.width, this.height));

                // Draw text
                d.setHalign(Drawer.H_LEFT);
                d.setValign(Drawer.V_TOP);
                d.setColor(Color.WHITE);
                int lastLineSize = 0;
                for (String s : messageHistory) {
                    int lineHeight = d.getFont().getHeight(s);

                    int y = chatHistory.getHeight() - lastLineSize - 4;
                    if (y > chatHistory.getCeiling() && y < chatHistory.getCeiling() + 300 + lineHeight) {
                        d.drawText(0, (chatHistory.getHeight() - lastLineSize - lineHeight), s);
                    }
                    lastLineSize += lineHeight;
                }

                // Adjust height
                int prevHeight = chatHistory.getHeight();
                chatHistory.setHeight(Math.max(300, lastLineSize));
                if (chatHistory.getCeiling() == prevHeight - 300) {
                    chatHistory.resetScroll();
                }
            }
        });

        recentSurface = new Surface(600, 300) {
            @Override
            public void draw(Drawer d) {
                // Draw text
                d.setHalign(Drawer.H_LEFT);
                d.setValign(Drawer.V_TOP);
                d.setColor(Color.WHITE);
                int lastLineSize = 0;
                int lines = chatRecentTimer.size();
                int i = 0;
                for (String s : messageHistory) {
                    if (i >= lines) {
                        break;
                    }

                    Color drawColor = new Color(1, 1, 1, Math.clamp(chatRecentTimer.get(i) / 120f, 0, 1));
                    int lineHeight = d.getFont().getHeight(s);
                    d.setColor(drawColor);
                    d.drawText(0, (this.height - lastLineSize - lineHeight), s);
                    lastLineSize += lineHeight;
                    i++;
                }
            }
        };

        chatInput = new Surface(600, 20) {
            @Override
            public void draw(Drawer d) {
                // Back panel
                d.setColor(inputColor);
                d.drawRect(new Rectanglei(0, 0, this.width, this.height));

                // Text
                String c = currentLine.substring(0, inputPosition);
                int w = d.getFont().getWidth(c);
                d.setColor(Color.WHITE);
                d.setHalign(Drawer.H_LEFT);
                d.setValign(Drawer.V_TOP);
                if (w < 550) {
                    // If input can fit in box, draw it normally.
                    d.drawText(0, 0, currentLine.toString());
                    d.drawText(w - 1, 0, StringUtils.getFieldCarat());
                }
                else {
                    // Otherwise, draw the section around the cursor.
                    d.drawText(550 - w, 0, currentLine.toString());
                    d.drawText(549, 0, StringUtils.getFieldCarat());
                }
            }
        };
    }

    /**
     * Handle user input and manage chatbox.
     *
     * @param d Drawer for use in font calculations.
     */
    private void processInput(Drawer d) {
        // Chat message typing and sending.
        if (enabled) {
            // Typing
            if (KeyListener.getLastChar() != '\0' && currentLine.length() < 255) {
                if (!clearFlag) {
                    currentLine.insert(inputPosition, KeyListener.getLastChar());
                }
                else {
                    clearFlag = false;
                }
                KeyListener.clearLastChar();
            }

            // Key processing
            switch (KeyListener.getLastKey()) {
                case GLFW_KEY_BACKSPACE:
                    if (inputPosition > 0) {
                        currentLine.deleteCharAt(inputPosition - 1);
                    }
                    break;
                case GLFW_KEY_LEFT:
                    if (-inputPositionOffset < currentLine.length()) {
                        inputPositionOffset--;
                    }
                    break;
                case GLFW_KEY_RIGHT:
                    if (inputPositionOffset < 0) {
                        inputPositionOffset++;
                    }
                    break;
                case GLFW_KEY_UP:
                    if (inputHistory.size() > historyScroll + 1) {
                        historyScroll++;
                        currentLine = new StringBuilder(inputHistory.get(historyScroll));
                    }
                    break;
                case GLFW_KEY_DOWN:
                    if (historyScroll > 0) {
                        historyScroll--;
                        currentLine = new StringBuilder(inputHistory.get(historyScroll));
                    }
                    break;
                case GLFW_KEY_V:
                    if (KeyListener.isCtrlPressed()) {
                        String toPaste = KeyListener.getClipboardString();
                        if (toPaste != null) {
                            String in = toPaste.substring(0, Math.min(toPaste.length(), 255 - currentLine.length()));
                            currentLine.insert(inputPosition, in);
                        }
                    }
                    break;
            }

            // Reset input
            KeyListener.clearLastKey();
            inputPosition = currentLine.length() + inputPositionOffset;

            // Send message
            if (KeyListener.checkPressed(GLFW_KEY_ENTER) && !currentLine.isEmpty()) {
                String toSend = currentLine.toString();

                // Reset current input position.
                inputPosition = 0;
                inputPositionOffset = 0;

                // Increment sent message history
                historyScroll = 0;
                inputHistory.remove();
                inputHistory.push(toSend);
                inputHistory.push("");

                // Process message
                if (!toSend.startsWith("/")) {
                    sendChatMessage(d, GameScene.getPlayerUsername() + ": " + toSend);
                }
                else {
                    // Console command
                    sendChatMessage(d, ConsoleCommand.processCommand(toSend.substring(1)));
                }

                // Clear current line and close chat
                currentLine = new StringBuilder();
                enabled = false;
                InputHandler.setPlayerControlEnabled(true);
            }
        }

        // Handle recent chat message timer.
        chatRecentTimer.replaceAll(integer -> --integer);
        if (!chatRecentTimer.isEmpty() && chatRecentTimer.getLast() < 1 || chatRecentTimer.size() > 16) {
            // Clear older messages.
            chatRecentTimer.removeLast();
        }
    }

    /**
     * Draw the chat box.
     *
     * @param d Drawer to use.
     */
    public void draw(Drawer d) {
        processInput(d);

        int gH = Window.getHeight();

        if (enabled) {
            chatHistory.draw(d, 0, gH / 2 - 100);
            chatInput.render(d, 0, gH / 2 + 200);
        }
        else if (!GameScene.isPaused()) {
            recentSurface.render(d, 0, gH / 2 - 100);
        }
    }

    /**
     * Send a message to the chat box.
     *
     * @param message Message to send.
     */
    private void sendChatMessage(Drawer d, String message) {
        // Wrap message
        Font curFont = d.getFont();
        int lines = Math.ceilDiv(curFont.getWidth(message), 580);
        int lineLength = lines > 0 ? message.length() / lines : 0;
        String[] words = message.split(" ");

        StringBuilder out = new StringBuilder(message.length() + lines);
        int curLength = 0;
        for (String w : words) {
            if (w.length() > lineLength) {
                // Long single word.
                int subwords = w.length() / lineLength;
                for (int i = 0; i <= subwords; i++) {
                    int index = lineLength * i;
                    String subword = w.substring(index, Math.min(index + lineLength, w.length()));
                    out.append('\n').append(subword);
                    curLength = subword.length();
                }
            }
            else {
                // Append words, making breaks as needed.
                if (curLength + w.length() > lineLength) {
                    out.append('\n');
                    curLength = 0;
                }
                out.append(w).append(' ');
                curLength += w.length() + 1;
            }
        }

        // Output final message.
        messageHistory.addFirst(out.toString());
        chatRecentTimer.addFirst(3600);
    }

    /**
     * Enable or disable the chat box.
     *
     * @param enableChat Whether to enable the chat box.
     */
    public static void setChatEnabled(boolean enableChat) {
        singleton.enabled = enableChat;
        singleton.clearFlag = true;
        singleton.chatHistory.resetScroll();
    }

    /**
     * Whether the player is currently in the chat box.
     *
     * @return Whether player has opened the chat box.
     */
    public static boolean isChatEnabled() {
        return singleton.enabled;
    }

    /**
     * Print a system message to chat.
     *
     * @param message Message to print.
     */
    public static void message(String message) {
        singleton.messageHistory.addFirst(message);
        singleton.chatRecentTimer.addFirst(3600);
    }
}
