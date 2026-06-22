package dev.patrys.custommenu;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.function.BiConsumer;

public class MenuItem {

    private final ItemStack itemStack;
    private BiConsumer<Player, ClickType> clickAction;
    private Sound clickSound;
    private float soundVolume = 1.0f;
    private float soundPitch = 1.0f;

    public MenuItem(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public MenuItem onClick(BiConsumer<Player, ClickType> action) {
        this.clickAction = action;
        return this;
    }

    public MenuItem sound(Sound sound) {
        this.clickSound = sound;
        return this;
    }

    public MenuItem sound(Sound sound, float volume, float pitch) {
        this.clickSound = sound;
        this.soundVolume = volume;
        this.soundPitch = pitch;
        return this;
    }

    public void onClick(Player player, ClickType clickType) {
        if (clickSound != null) {
            player.playSound(player.getLocation(), clickSound, soundVolume, soundPitch);
        }

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