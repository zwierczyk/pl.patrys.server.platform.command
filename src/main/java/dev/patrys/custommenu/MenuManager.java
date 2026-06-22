package dev.patrys.custommenu;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MenuManager implements Listener {

    private final JavaPlugin plugin;
    private final Map<UUID, Menu> activeMenus;
    private final Map<UUID, Long> cooldowns;

    public MenuManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.activeMenus = new ConcurrentHashMap<>();
        this.cooldowns = new ConcurrentHashMap<>();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void openMenu(Player player, Menu menu) {
        if (hasCooldown(player)) {
            return;
        }

        Menu currentMenu = activeMenus.get(player.getUniqueId());
        if (currentMenu != null) {
            currentMenu.onClose(player);
        }

        activeMenus.put(player.getUniqueId(), menu);
        menu.open(player);
        setCooldown(player);
    }

    public void closeMenu(Player player) {
        Menu menu = activeMenus.remove(player.getUniqueId());
        if (menu != null) {
            player.closeInventory();
        }
    }

    public Menu getActiveMenu(Player player) {
        return activeMenus.get(player.getUniqueId());
    }

    private boolean hasCooldown(Player player) {
        Long cooldownTime = cooldowns.get(player.getUniqueId());
        if (cooldownTime == null) {
            return false;
        }
        return System.currentTimeMillis() < cooldownTime;
    }

    private void setCooldown(Player player) {
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis() + 100);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        Menu menu = activeMenus.get(player.getUniqueId());

        if (menu == null) {
            return;
        }

        if (event.getClickedInventory() == null) {
            return;
        }

        if (!event.getInventory().equals(menu.getInventory())) {
            return;
        }

        event.setCancelled(true);

        if (hasCooldown(player)) {
            return;
        }

        menu.handleClick(player, event.getSlot(), event.getClick());
        setCooldown(player);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getPlayer();
        Menu menu = activeMenus.get(player.getUniqueId());

        if (menu == null) {
            return;
        }

        if (!event.getInventory().equals(menu.getInventory())) {
            return;
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (menu.shouldReopenOnClose() && player.isOnline()) {
                activeMenus.put(player.getUniqueId(), menu);
                menu.open(player);
            } else {
                activeMenus.remove(player.getUniqueId());
                menu.onClose(player);
            }
        }, 1L);
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getPlayer();
        Menu menu = activeMenus.get(player.getUniqueId());

        if (menu != null && event.getInventory().equals(menu.getInventory())) {
            menu.onOpen(player);
        }
    }

    public void shutdown() {
        activeMenus.values().forEach(menu -> menu.getViewers().forEach(Player::closeInventory));
        activeMenus.clear();
        cooldowns.clear();
    }
}