package dev.patrys.custommenu.animation;

import dev.patrys.custommenu.Menu;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;

public class SpiralAnimation extends MenuAnimation {

    public SpiralAnimation(JavaPlugin plugin, Menu menu, long delay) {
        super(plugin, menu, delay);
    }

    @Override
    public void play(Player player) {
        Map<Integer, ItemStack> items = captureItems();

        int rows = menu.getSize() / 9;
        int cols = 9;

        int top = 0, bottom = rows - 1;
        int left = 0, right = cols - 1;

        long currentDelay = 0;

        while (top <= bottom && left <= right) {

            for (int i = left; i <= right; i++) {
                int slot = top * cols + i;
                if (items.containsKey(slot)) {
                    place(player, slot, items.get(slot), currentDelay);
                    currentDelay += delay;
                }
            }
            top++;

            for (int i = top; i <= bottom; i++) {
                int slot = i * cols + right;
                if (items.containsKey(slot)) {
                    place(player, slot, items.get(slot), currentDelay);
                    currentDelay += delay;
                }
            }
            right--;

            for (int i = right; i >= left; i--) {
                int slot = bottom * cols + i;
                if (items.containsKey(slot)) {
                    place(player, slot, items.get(slot), currentDelay);
                    currentDelay += delay;
                }
            }
            bottom--;

            for (int i = bottom; i >= top; i--) {
                int slot = i * cols + left;
                if (items.containsKey(slot)) {
                    place(player, slot, items.get(slot), currentDelay);
                    currentDelay += delay;
                }
            }
            left++;
        }
    }
}