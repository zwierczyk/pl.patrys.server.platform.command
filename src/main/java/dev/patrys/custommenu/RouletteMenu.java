package dev.patrys.custommenu;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class RouletteMenu extends Menu {

    private final JavaPlugin plugin;
    private final List<ItemStack> items;
    private final int[] spinSlots;
    private final int centerSlotIndex;
    private final Random random;

    private boolean spinning;
    private int taskId;
    private int ticks;
    private int delay;
    private int passed;

    public RouletteMenu(JavaPlugin plugin, String title, int rows, int[] spinSlots, int centerSlotIndex) {
        super(title, rows);
        this.plugin = plugin;
        this.spinSlots = spinSlots;
        this.centerSlotIndex = centerSlotIndex;
        this.items = new ArrayList<>();
        this.random = new Random();
        this.spinning = false;
    }

    public void addPrize(ItemStack itemStack) {
        items.add(itemStack);
    }

    public abstract void onWin(Player player, ItemStack wonItem);

    public abstract void onBuildBackground(Player player);

    @Override
    public void onBuild(Player player) {
        onBuildBackground(player);
        if (!spinning && items.size() > 0) {
            fillWheelRandomly();
        }
    }

    public void start(Player player) {
        if (spinning || items.isEmpty()) {
            return;
        }
        spinning = true;
        setReopenOnClose(true);

        ticks = 0;
        delay = 1;
        passed = 0;

        refresh();

        taskId = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (passed >= delay) {
                passed = 0;
                shiftWheel(player);
                ticks++;
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1.0f, 1.5f);

                if (ticks > 20 && ticks <= 30) delay = 2;
                else if (ticks > 30 && ticks <= 40) delay = 3;
                else if (ticks > 40 && ticks <= 45) delay = 5;
                else if (ticks > 45 && ticks <= 48) delay = 8;
                else if (ticks > 48) {
                    finish(player);
                }
            }
            passed++;
        }, 0L, 1L).getTaskId();
    }

    private void shiftWheel(Player player) {
        ItemStack[] current = new ItemStack[spinSlots.length];
        for (int i = 0; i < spinSlots.length; i++) {
            current[i] = getInventory().getItem(spinSlots[i]);
        }

        for (int i = spinSlots.length - 1; i > 0; i--) {
            setItem(spinSlots[i], current[i - 1]);
        }

        setItem(spinSlots[0], items.get(random.nextInt(items.size())));
        player.updateInventory();
    }

    private void fillWheelRandomly() {
        for (int slot : spinSlots) {
            setItem(slot, items.get(random.nextInt(items.size())));
        }
    }

    private void finish(Player player) {
        Bukkit.getScheduler().cancelTask(taskId);
        spinning = false;
        setReopenOnClose(false);

        ItemStack won = getInventory().getItem(spinSlots[centerSlotIndex]);
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);

        refresh();
        onWin(player, won);
    }

    @Override
    public void onClose(Player player) {
        super.onClose(player);
        if (spinning && !shouldReopenOnClose()) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
    }

    public boolean isSpinning() {
        return spinning;
    }
}