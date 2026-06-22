package dev.patrys.customcommands.handler;

public class HandlerRegistry {
    private PermissionHandler permissionHandler = (sender, permission) ->
            sender.sendMessage("§cNie masz uprawnień! Wymagane: §7" + permission);

    private UsageHandler usageHandler = (sender, usage) ->
            sender.sendMessage("§cPoprawne użycie:\n" + usage);

    private CooldownHandler cooldownHandler = (sender, time) ->
            sender.sendMessage("§cMusisz poczekać §7" + time + " §csekund!");

    private PlayerNotFoundHandler playerNotFoundHandler = (sender, name) ->
            sender.sendMessage("§cGracz o nicku §7" + name + " §cnie został znaleziony (lub nigdy tu nie grał)!");

    public PermissionHandler getPermissionHandler() {
        return permissionHandler;
    }

    public void setPermissionHandler(PermissionHandler permissionHandler) {
        this.permissionHandler = permissionHandler;
    }

    public UsageHandler getUsageHandler() {
        return usageHandler;
    }

    public void setUsageHandler(UsageHandler usageHandler) {
        this.usageHandler = usageHandler;
    }

    public CooldownHandler getCooldownHandler() {
        return cooldownHandler;
    }

    public void setCooldownHandler(CooldownHandler cooldownHandler) {
        this.cooldownHandler = cooldownHandler;
    }

    public PlayerNotFoundHandler getPlayerNotFoundHandler() {
        return playerNotFoundHandler;
    }

    public void setPlayerNotFoundHandler(PlayerNotFoundHandler playerNotFoundHandler) {
        this.playerNotFoundHandler = playerNotFoundHandler;
    }
}