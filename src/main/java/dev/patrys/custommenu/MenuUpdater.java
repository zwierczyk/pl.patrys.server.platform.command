package dev.patrys.custommenu;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class MenuUpdater {

    private final JavaPlugin plugin;
    private final Map<UUID, Integer> updateTasks;
    private final Map<UUID, Long> updateIntervals;

    public MenuUpdater(JavaPlugin plugin) {
        this.plugin = plugin;
        this.updateTasks = new HashMap<>();
        this.updateIntervals = new HashMap<>();
    }

    public void startUpdating(Player player, Menu menu, long intervalTicks) {
        stopUpdating(player);

        updateIntervals.put(player.getUniqueId(), intervalTicks);

        int taskId = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            Menu activeMenu = MenuRegistry.getInstance().getMenuManager().getActiveMenu(player);
            if (activeMenu == null || !activeMenu.equals(menu)) {
                stopUpdating(player);
                return;
            }

            if (!player.isOnline()) {
                stopUpdating(player);
                return;
            }

            menu.refresh();
        }, intervalTicks, intervalTicks).getTaskId();

        updateTasks.put(player.getUniqueId(), taskId);
    }

    public void stopUpdating(Player player) {
        Integer taskId = updateTasks.remove(player.getUniqueId());
        if (taskId != null) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
        updateIntervals.remove(player.getUniqueId());
    }

    public boolean isUpdating(Player player) {
        return updateTasks.containsKey(player.getUniqueId());
    }

    public void shutdown() {
        updateTasks.values().forEach(Bukkit.getScheduler()::cancelTask);
        updateTasks.clear();
        updateIntervals.clear();
    }
}