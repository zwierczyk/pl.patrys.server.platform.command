package dev.patrys.customcommands;

import dev.patrys.customcommands.platform.PlatformSender;
import java.util.List;

@FunctionalInterface
public interface TabCompleter {
    List<String> complete(PlatformSender sender, String[] args);
}