package dev.patrys.custommenu;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class ItemBuilder {

    private final ItemStack itemStack;
    private final ItemMeta itemMeta;

    public ItemBuilder(Material material) {
        this.itemStack = new ItemStack(material);
        this.itemMeta = itemStack.getItemMeta();
    }

    public ItemBuilder(Material material, int amount) {
        this.itemStack = new ItemStack(material, amount);
        this.itemMeta = itemStack.getItemMeta();
    }

    public ItemBuilder(ItemStack itemStack) {
        this.itemStack = itemStack.clone();
        this.itemMeta = this.itemStack.getItemMeta();
    }

    public ItemBuilder name(String name) {
        if (itemMeta != null) {
            itemMeta.setDisplayName(name);
        }
        return this;
    }

    public ItemBuilder lore(String... lore) {
        if (itemMeta != null) {
            itemMeta.setLore(Arrays.asList(lore));
        }
        return this;
    }

    public ItemBuilder lore(List<String> lore) {
        if (itemMeta != null) {
            itemMeta.setLore(lore);
        }
        return this;
    }

    public ItemBuilder amount(int amount) {
        itemStack.setAmount(amount);
        return this;
    }

    public ItemBuilder glow() {
        if (itemMeta != null) {
            itemMeta.addEnchant(Enchantment.DURABILITY, 1, true);
            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        return this;
    }

    public ItemBuilder flags(ItemFlag... flags) {
        if (itemMeta != null) {
            itemMeta.addItemFlags(flags);
        }
        return this;
    }

    public ItemBuilder unbreakable(boolean unbreakable) {
        if (itemMeta != null) {
            itemMeta.setUnbreakable(unbreakable);
        }
        return this;
    }

    public ItemBuilder enchant(Enchantment enchantment, int level) {
        if (itemMeta != null) {
            itemMeta.addEnchant(enchantment, level, true);
        }
        return this;
    }

    public ItemStack build() {
        if (itemMeta != null) {
            itemStack.setItemMeta(itemMeta);
        }
        return itemStack;
    }

    public static ItemBuilder of(Material material) {
        return new ItemBuilder(material);
    }

    public static ItemBuilder of(ItemStack itemStack) {
        return new ItemBuilder(itemStack);
    }
}