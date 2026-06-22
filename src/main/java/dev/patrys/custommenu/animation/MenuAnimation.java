package dev.patrys.custommenu.animation;

import dev.patrys.custommenu.Menu;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public abstract class MenuAnimation {

    protected final JavaPlugin plugin;
    protected final Menu menu;
    protected final long delay;

    public MenuAnimation(JavaPlugin plugin, Menu menu, long delay) {
        this.plugin = plugin;
        this.menu = menu;
        this.delay = delay;
    }

    protected Map<Integer, ItemStack> captureItems() {
        Map<Integer, ItemStack> map = new HashMap<>();
        for (int i = 0; i < menu.getSize(); i++) {
            ItemStack item = menu.getInventory().getItem(i);
            if (item != null) {
                map.put(i, item.clone());
                menu.getInventory().setItem(i, null);
            }
        }
        return map;
    }

    protected void place(Player player, int slot, ItemStack item, long delayTicks) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.getOpenInventory().getTopInventory() == menu.getInventory()) {
                menu.getInventory().setItem(slot, item);
            }
        }, delayTicks);
    }

    public abstract void play(Player player);
}