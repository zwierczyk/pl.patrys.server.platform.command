package dev.patrys.customcommands.platform.bukkit;

import dev.patrys.customcommands.platform.PlatformSender;
import org.bukkit.command.CommandSender;

public class BukkitSender implements PlatformSender {
    private final CommandSender sender;

    public BukkitSender(CommandSender sender) {
        this.sender = sender;
    }

    @Override
    public void sendMessage(String message) {
        sender.sendMessage(message);
    }

    @Override
    public String getName() {
        return sender.getName();
    }

    @Override
    public boolean hasPermission(String permission) {
        return sender.hasPermission(permission);
    }

    @Override
    public Object getHandle() {
        return sender;
    }
}