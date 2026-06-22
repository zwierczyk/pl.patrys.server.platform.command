package dev.patrys.custommenu;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;

import java.util.*;

public abstract class Menu {

    private final String title;
    private final int size;
    private final Inventory inventory;
    private final Map<Integer, MenuItem> items;
    private boolean reopenOnClose;

    public Menu(String title, int rows) {
        this.title = title;
        this.size = rows * 9;
        this.inventory = Bukkit.createInventory(null, size, title);
        this.items = new HashMap<>();
        this.reopenOnClose = false;
    }

    public abstract void onBuild(Player player);

    public void open(Player player) {
        inventory.clear();
        items.clear();
        onBuild(player);
        player.openInventory(inventory);
    }

    public void handleClick(Player player, int slot, ClickType clickType) {
        MenuItem item = items.get(slot);
        if (item != null) {
            item.onClick(player, clickType);
        }
    }

    public void setItem(int slot, MenuItem item) {
        if (slot < 0 || slot >= size) return;
        items.put(slot, item);
        inventory.setItem(slot, item.getItemStack());
    }

    public void setItem(int slot, org.bukkit.inventory.ItemStack itemStack) {
        if (slot < 0 || slot >= size) return;
        inventory.setItem(slot, itemStack);
    }

    public void refresh() {
        List<Player> viewers = getViewers();
        for (Player player : viewers) {
            inventory.clear();
            items.clear();
            onBuild(player);
            player.updateInventory();
        }
    }

    public void onOpen(Player player) {
    }

    public void onClose(Player player) {
    }

    public Inventory getInventory() {
        return inventory;
    }

    public List<Player> getViewers() {
        List<Player> players = new ArrayList<>();
        for (HumanEntity entity : inventory.getViewers()) {
            if (entity instanceof Player) {
                players.add((Player) entity);
            }
        }
        return players;
    }

    public int getSize() {
        return size;
    }

    public String getTitle() {
        return title;
    }

    public void setReopenOnClose(boolean reopenOnClose) {
        this.reopenOnClose = reopenOnClose;
    }

    public boolean shouldReopenOnClose() {
        return reopenOnClose;
    }
}