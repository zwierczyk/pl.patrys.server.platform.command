package dev.patrys.custommenu;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public abstract class PaginatedMenu extends Menu {

    private int currentPage;
    private final int itemsPerPage;
    private final Map<Integer, Integer> navigationSlots;

    public PaginatedMenu(String title, int rows) {
        super(title, rows);
        this.currentPage = 0;
        this.itemsPerPage = (rows - 2) * 7;
        this.navigationSlots = new HashMap<>();
        setupNavigation();
    }

    private void setupNavigation() {
        int lastRow = (getSize() / 9) - 1;
        navigationSlots.put(lastRow * 9 + 3, -1);
        navigationSlots.put(lastRow * 9 + 5, 1);
    }

    public abstract int getTotalItems(Player player);

    public abstract void onPageBuild(Player player, int startIndex, int endIndex);

    @Override
    public void onBuild(Player player) {
        int totalItems = getTotalItems(player);
        int maxPages = (int) Math.ceil((double) totalItems / itemsPerPage);

        if (currentPage < 0) {
            currentPage = 0;
        }
        if (currentPage >= maxPages && maxPages > 0) {
            currentPage = maxPages - 1;
        }

        int startIndex = currentPage * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, totalItems);

        onPageBuild(player, startIndex, endIndex);

        int lastRow = (getSize() / 9) - 1;
        if (currentPage > 0) {
            setItem(lastRow * 9 + 3, createPreviousPageItem()
                    .onClick((p, click) -> {
                        previousPage();
                        open(p);
                    }));
        }

        if (currentPage < maxPages - 1) {
            setItem(lastRow * 9 + 5, createNextPageItem()
                    .onClick((p, click) -> {
                        nextPage();
                        open(p);
                    }));
        }
    }

    protected MenuItem createPreviousPageItem() {
        return MenuItem.of(ItemBuilder.of(org.bukkit.Material.ARROW)
                .name("§aPrevious Page")
                .build());
    }

    protected MenuItem createNextPageItem() {
        return MenuItem.of(ItemBuilder.of(org.bukkit.Material.ARROW)
                .name("§aNext Page")
                .build());
    }

    public void nextPage() {
        currentPage++;
    }

    public void previousPage() {
        currentPage--;
    }

    public void setPage(int page) {
        this.currentPage = page;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    protected int getItemsPerPage() {
        return itemsPerPage;
    }
}