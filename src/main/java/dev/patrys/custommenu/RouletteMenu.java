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
    private final List<ItemStack> prizes;
    private final int[] spinSlots;
    private final int centerSlotIndex;
    private final Random random;

    private boolean spinning;
    private int taskId;
    private int ticks;
    private int delay;
    private int passed;

    // Bezpieczna pamięć przedmiotów w ruletce
    private ItemStack[] currentWheel;

    public RouletteMenu(JavaPlugin plugin, String title, int rows, int[] spinSlots, int centerSlotIndex) {
        super(title, rows);
        this.plugin = plugin;
        this.spinSlots = spinSlots;
        this.centerSlotIndex = centerSlotIndex;
        this.prizes = new ArrayList<>();
        this.random = new Random();
        this.spinning = false;
    }

    public void addPrize(ItemStack itemStack) {
        prizes.add(itemStack);
    }

    public abstract void onWin(Player player, ItemStack wonItem);

    public abstract void onBuildBackground(Player player);

    @Override
    public void onBuild(Player player) {
        onBuildBackground(player);
        if (!spinning && !prizes.isEmpty()) {
            currentWheel = new ItemStack[spinSlots.length];
            for (int i = 0; i < spinSlots.length; i++) {
                currentWheel[i] = prizes.get(random.nextInt(prizes.size()));
                setItem(spinSlots[i], currentWheel[i]);
            }
        }
    }

    @Override
    public void open(Player player) {
        if (spinning) {
            // Jeśli koło się kręci, a gracz wciśnie ESC,
            // system tylko otwiera okno ponownie (bez resetowania przedmiotów!)
            player.openInventory(getInventory());
        } else {
            super.open(player);
        }
    }

    @Override
    public void refresh() {
        if (spinning) {
            // Zablokuj całkowite czyszczenie menu podczas kręcenia
            for (Player viewer : getViewers()) {
                viewer.updateInventory();
            }
        } else {
            super.refresh();
        }
    }

    public void start(Player player) {
        if (spinning || prizes.isEmpty()) {
            return;
        }
        spinning = true;
        setReopenOnClose(true); // Wymuś otwarte okno

        // Załaduj koło początkowe
        currentWheel = new ItemStack[spinSlots.length];
        for (int i = 0; i < spinSlots.length; i++) {
            currentWheel[i] = prizes.get(random.nextInt(prizes.size()));
            setItem(spinSlots[i], currentWheel[i]);
        }

        super.refresh(); // Przeładuj tło (np. by guzik START zniknął)

        ticks = 0;
        delay = 1;
        passed = 0;

        taskId = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (passed >= delay) {
                passed = 0;
                shiftWheel();
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1.0f, 1.5f);

                if (ticks > 20 && ticks <= 30) delay = 2;
                else if (ticks > 30 && ticks <= 40) delay = 3;
                else if (ticks > 40 && ticks <= 45) delay = 5;
                else if (ticks > 45 && ticks <= 48) delay = 8;
                else if (ticks > 48) {
                    finish(player);
                }
                ticks++;
            }
            passed++;
        }, 0L, 1L).getTaskId();
    }

    private void shiftWheel() {
        // Płynne przesuwanie w bezpiecznej tablicy
        for (int i = currentWheel.length - 1; i > 0; i--) {
            currentWheel[i] = currentWheel[i - 1];
        }
        currentWheel[0] = prizes.get(random.nextInt(prizes.size()));

        // Przenoszenie bezpiecznej tablicy do GUI
        for (int i = 0; i < spinSlots.length; i++) {
            setItem(spinSlots[i], currentWheel[i]);
        }
    }

    private void finish(Player player) {
        Bukkit.getScheduler().cancelTask(taskId);

        // Zawsze bierzemy item z bezpiecznej tablicy, a nie z widoku Bukkitowego
        ItemStack won = currentWheel[centerSlotIndex].clone();

        spinning = false;
        setReopenOnClose(false);

        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);

        super.refresh(); // Przywracamy normalne zachowanie menu
        onWin(player, won);
    }

    @Override
    public void onClose(Player player) {
        super.onClose(player);
        if (spinning && !shouldReopenOnClose()) {
            Bukkit.getScheduler().cancelTask(taskId);
            spinning = false;
        }
    }

    public boolean isSpinning() {
        return spinning;
    }
}