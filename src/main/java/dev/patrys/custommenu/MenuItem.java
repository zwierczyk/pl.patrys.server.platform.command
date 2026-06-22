package dev.patrys.custommenu;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.function.BiConsumer;

public class MenuItem {

    private final ItemStack itemStack;
    private BiConsumer<Player, ClickType> clickAction;

    public MenuItem(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public MenuItem onClick(BiConsumer<Player, ClickType> action) {
        this.clickAction = action;
        return this;
    }

    public void onClick(Player player, ClickType clickType) {
        if (clickAction != null) {
            clickAction.accept(player, clickType);
        }
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public static MenuItem of(ItemStack itemStack) {
        return new MenuItem(itemStack);
    }
}