package dev.patrys.custommenu;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MenuRegistry {

    private static MenuRegistry instance;
    private final Map<UUID, Object> playerData;
    private MenuManager menuManager;

    private MenuRegistry() {
        this.playerData = new ConcurrentHashMap<>();
    }

    public static MenuRegistry getInstance() {
        if (instance == null) {
            instance = new MenuRegistry();
        }
        return instance;
    }

    public void initialize(JavaPlugin plugin) {
        if (menuManager == null) {
            menuManager = new MenuManager(plugin);
        }
    }

    public MenuManager getMenuManager() {
        return menuManager;
    }

    public void setPlayerData(Player player, String key, Object value) {
        playerData.put(generateKey(player.getUniqueId(), key), value);
    }

    public Object getPlayerData(Player player, String key) {
        return playerData.get(generateKey(player.getUniqueId(), key));
    }

    public void removePlayerData(Player player, String key) {
        playerData.remove(generateKey(player.getUniqueId(), key));
    }

    public void clearPlayerData(Player player) {
        playerData.entrySet().removeIf(entry ->
                entry.getKey().toString().startsWith(player.getUniqueId().toString()));
    }

    private UUID generateKey(UUID playerUUID, String key) {
        return UUID.nameUUIDFromBytes((playerUUID.toString() + key).getBytes());
    }

    public void shutdown() {
        if (menuManager != null) {
            menuManager.shutdown();
        }
        playerData.clear();
    }
}