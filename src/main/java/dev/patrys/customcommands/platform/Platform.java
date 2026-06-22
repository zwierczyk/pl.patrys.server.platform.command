package dev.patrys.customcommands.platform;

import dev.patrys.customcommands.CommandExecutor;

public interface Platform {
    void registerCommand(String name, CommandExecutor executor);
    void scheduleAsync(Runnable task);
    void scheduleSync(Runnable task);
}