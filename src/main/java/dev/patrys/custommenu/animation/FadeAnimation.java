package dev.patrys.custommenu.animation;

import dev.patrys.custommenu.Menu;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;

public class FadeAnimation extends MenuAnimation {

    public FadeAnimation(JavaPlugin plugin, Menu menu, long delay) {
        super(plugin, menu, delay);
    }

    @Override
    public void play(Player player) {
        Map<Integer, ItemStack> items = captureItems();
        long currentDelay = 0;

        for (Map.Entry<Integer, ItemStack> entry : items.entrySet()) {
            place(player, entry.getKey(), entry.getValue(), currentDelay);
            currentDelay += delay;
        }
    }
}