package otherspace.core.session.scenes.world;

import org.joml.Vector2d;
import otherspace.core.engine.world.items.Inventory;
import otherspace.core.engine.world.items.Item;
import otherspace.core.engine.world.items.ItemStack;
import otherspace.core.engine.world.items.tools.ToolItem;
import otherspace.game.entities.Player;

import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Function;

/**
 * Processes console commands executed by the player.
 */
public class ConsoleCommand {
    private static final HashMap<String, ConsoleCommand> list;
    static {
        list = new HashMap<>();

        // Help
        list.put("help", new ConsoleCommand((_) ->
                """
                List of console commands:
                help: Display this list.
                tp <x> <y>: Teleports self to specified coordinates.
                gamemode <gamemode>: Set the player's gamemode to the specified value (0 for survival, 1 for sandbox).
                give <modName:itemName> <amount>: Give a player the specified amount of an item with the given handle.
                timeset <value>: Set the game time to the specified value.
                setdefaultgamemode <gamemode>: Set the default gamemode for the world (0 for survival, 1 for sandbox).
                heal <amount>: Heal self by a specified amount.
                damage <amount>: Damage self by a specified amount.
                kill: Kill self.
                clear: Clear own inventory.
                """
        ));

        // TP
        list.put("tp", new ConsoleCommand((args) -> {
            try {
                // Get coordinates.
                double x;
                double y;
                try {
                    x = Double.parseDouble(args[0]);
                    y = Double.parseDouble(args[1]);
                }
                catch (NumberFormatException e) {
                    return "Invalid coordinates.";
                }

                // Check that coordinates are within world limit.
                if (Math.abs(x) > Chunk.WORLD_MAX_DIST - 16 || Math.abs(y) > Chunk.WORLD_MAX_DIST - 16) {
                    return "Coordinates are outside of world, maximum allowed distance in any direction is " + (Chunk.WORLD_MAX_DIST - 16) + ".";
                }

                // If there were no issues, teleport self.
                Player.getOwnPlayer().position = new Vector2d(x, y);
                return "Teleported self to x: " + x + ", y: " + y + ".";
            }
            catch (Exception e) {
                return "Syntax Error.";
            }
        }));

        // Gamemode
        list.put("gamemode", new ConsoleCommand((args) -> {
            try {
                int gm;
                try {
                    gm = Integer.parseInt(args[0]);
                    gm = Math.clamp(gm, 0, 1);
                }
                catch (NumberFormatException e) {
                    return "Invalid gamemode.";
                }

                Player.getOwnPlayer().setGamemode(gm);
                return "Set gamemode to " + (gm == 1 ? "Sandbox." : "Survival.");
            }
            catch (Exception e) {
                return "Syntax Error.";
            }
        }));

        // Give
        list.put("give", new ConsoleCommand((args) -> {
            try {
                // Get item handle and count.
                String handle = args[0];
                int amount;
                try {
                    amount = Integer.parseInt(args[1]);
                }
                catch (NumberFormatException e) {
                    return "Invalid amount.";
                }

                // Ensure item handle is a real item.
                handle = handle.replace(':', '/');
                if (!handle.contains("/")) {
                    handle = "base/" + handle;
                }

                try {
                    Item.getID(handle);
                }
                catch (NullPointerException e) {
                    return "Item does not exist.";
                }

                int id = Item.getID(handle);
                amount = Math.min(amount, 256);

                int state = 0;
                if (Item.get(id) instanceof ToolItem ti) {
                    if (args.length > 2) {
                        try {
                            state = Math.clamp(Integer.parseInt(args[2]), 0, ti.getMaxDurability());
                        }
                        catch (NumberFormatException e) {
                            return "Invalid state.";
                        }
                    }
                    else {
                        state = ti.getMaxDurability();
                    }
                }


                Player.getPlayerInventory().add(new ItemStack(id, amount, state), new Vector2d(Player.getOwnPlayer().position));
                return "Gave self " + amount + " of " + Item.get(id).NAME;
            }
            catch (Exception e) {
                return "Syntax Error.";
            }
        }));

        // Timeset
        list.put("timeset", new ConsoleCommand((args) -> {
            try {
                long sec = Long.parseLong(args[0]);
                World.setTime(sec);
                return "Set time to " + sec + " seconds";
            }
            catch (Exception e) {
                return "Syntax Error.";
            }
        }));

        // Default Gamemode
        list.put("setdefaultgamemode", new ConsoleCommand((args) -> {
            int gm;
            try {
                gm = Integer.parseInt(args[0]);
                gm = Math.clamp(gm, 0, 1);
            }
            catch (NumberFormatException e) {
                return "Invalid Gamemode.";
            }

            // Set default gamemode
            World.setDefaultGM(gm);
            return "Set default gamemode to " + (gm == 1 ? "Sandbox." : "Survival.");
        }));

        // Heal
        list.put("heal", new ConsoleCommand((args) -> {
            try {
                // Get healing amount.
                int amount;
                try {
                    amount = Integer.parseInt(args[0]);
                    amount = Math.max(amount, 0);
                }
                catch (NumberFormatException e) {
                    return "Invalid number.";
                }

                Player.getOwnPlayer().heal(amount);
                return "Healed self by " + amount + " HP.";
            }
            catch (Exception e) {
                return "Syntax Error.";
            }
        }));

        // Damage
        list.put("damage", new ConsoleCommand((args) -> {
            try {
                // Get damage amount.
                int amount;
                try {
                    amount = Integer.parseInt(args[0]);
                    amount = Math.max(amount, 0);
                }
                catch (NumberFormatException e) {
                    return "Invalid number.";
                }

                Player.getOwnPlayer().damage(amount);
                return "Dealt " + amount + " damage to self.";
            }
            catch (Exception e) {
                return "Syntax Error.";
            }
        }));

        // Kill
        list.put("kill", new ConsoleCommand((_) -> {
            Player.getOwnPlayer().kill();
            return "Killed self.";
        }));

        // Clear
        list.put("clear", new ConsoleCommand(_ -> {
            Player.getHeldItem().stackSize = 0;
            Inventory playerInventory = Player.getPlayerInventory();
            for (int i = 0; i < playerInventory.getSize(); i++) {
                playerInventory.get(i).stackSize = 0;
            }
            return "Cleared own inventory.";
        }));
    }

    /**
     * Process a console command.
     *
     * @param command Command we have been told to execute.
     */
    public static String processCommand(String command) {
        // Get command and arguments.
        String[] cmd = command.split(" ");
        ConsoleCommand toExecute = list.get(cmd[0]);

        // Does this command exist?
        if (toExecute == null) {
            return "Unknown Command. Use the \"help\" command for a list of commands.";
        }

        // If the command exists, pass the remaining arguments in.
        return toExecute.action.apply(Arrays.copyOfRange(cmd, 1, cmd.length));
    }

    private Function<String[], String> action;

    private ConsoleCommand(Function<String[], String> action) {
        this.action = action;
    }
}
