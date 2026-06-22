package dev.patrys.customcommands.handler;

public class HandlerRegistry {
    private PermissionHandler permissionHandler = (sender, permission) ->
            sender.sendMessage("§cNie masz uprawnień! Wymagane: §7" + permission);

    private UsageHandler usageHandler = (sender, usage) ->
            sender.sendMessage("§cPoprawne użycie:\n" + usage);

    private CooldownHandler cooldownHandler = (sender, time) ->
            sender.sendMessage("§cMusisz poczekać §7" + time + " §csekund!");

    private PlayerNotFoundHandler playerNotFoundHandler = (sender, name) ->
            sender.sendMessage("§cGracz o nicku §7" + name + " §cnie został znaleziony!");

    private PlayerOnlyHandler playerOnlyHandler = sender ->
            sender.sendMessage("§cTa komenda może być użyta tylko przez gracza w grze!");

    private ConsoleOnlyHandler consoleOnlyHandler = sender ->
            sender.sendMessage("§cTa komenda może być użyta tylko z poziomu konsoli!");

    // GETTERY I SETTERY
    public PermissionHandler getPermissionHandler() { return permissionHandler; }
    public void setPermissionHandler(PermissionHandler permissionHandler) { this.permissionHandler = permissionHandler; }

    public UsageHandler getUsageHandler() { return usageHandler; }
    public void setUsageHandler(UsageHandler usageHandler) { this.usageHandler = usageHandler; }

    public CooldownHandler getCooldownHandler() { return cooldownHandler; }
    public void setCooldownHandler(CooldownHandler cooldownHandler) { this.cooldownHandler = cooldownHandler; }

    public PlayerNotFoundHandler getPlayerNotFoundHandler() { return playerNotFoundHandler; }
    public void setPlayerNotFoundHandler(PlayerNotFoundHandler playerNotFoundHandler) { this.playerNotFoundHandler = playerNotFoundHandler; }

    public PlayerOnlyHandler getPlayerOnlyHandler() { return playerOnlyHandler; }
    public void setPlayerOnlyHandler(PlayerOnlyHandler playerOnlyHandler) { this.playerOnlyHandler = playerOnlyHandler; }

    public ConsoleOnlyHandler getConsoleOnlyHandler() { return consoleOnlyHandler; }
    public void setConsoleOnlyHandler(ConsoleOnlyHandler consoleOnlyHandler) { this.consoleOnlyHandler = consoleOnlyHandler; }
}