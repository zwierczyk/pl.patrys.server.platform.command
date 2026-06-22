package dev.patrys.customcommands.platform.bukkit;

import dev.patrys.customcommands.CommandExecutor;
import dev.patrys.customcommands.TabCompleter;
import dev.patrys.customcommands.platform.Platform;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.util.List;

public class BukkitPlatform implements Platform {
    private final Plugin plugin;
    private CommandMap commandMap;

    public BukkitPlatform(Plugin plugin) {
        this.plugin = plugin;
        try {
            Field field = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            field.setAccessible(true);
            commandMap = (CommandMap) field.get(Bukkit.getServer());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void registerCommand(String name, CommandExecutor executor, TabCompleter completer) {
        BukkitCommand command = new BukkitCommand(name) {
            @Override
            public boolean execute(org.bukkit.command.CommandSender sender, String label, String[] args) {
                BukkitSender platformSender = new BukkitSender(sender);
                executor.execute(platformSender, args);
                return true;
            }

            @Override
            public List<String> tabComplete(org.bukkit.command.CommandSender sender, String alias, String[] args) {
                BukkitSender platformSender = new BukkitSender(sender);
                return completer.complete(platformSender, args);
            }
        };
        commandMap.register(plugin.getName(), command);
    }

    @Override
    public void scheduleAsync(Runnable task) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
    }

    @Override
    public void scheduleSync(Runnable task) {
        Bukkit.getScheduler().runTask(plugin, task);
    }
}