package dev.patrys.customcommands;

import dev.patrys.customcommands.platform.PlatformSender;

@FunctionalInterface
public interface CommandExecutor {
    void execute(PlatformSender sender, String[] args);
}