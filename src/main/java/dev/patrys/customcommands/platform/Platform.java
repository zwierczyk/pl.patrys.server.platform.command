package dev.patrys.customcommands.platform;

import dev.patrys.customcommands.CommandExecutor;
import dev.patrys.customcommands.TabCompleter;

public interface Platform {
    // Tutaj dodaliśmy trzeci argument: TabCompleter completer
    void registerCommand(String name, CommandExecutor executor, TabCompleter completer);
    void scheduleAsync(Runnable task);
    void scheduleSync(Runnable task);
}