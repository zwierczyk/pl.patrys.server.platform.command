package dev.patrys.customcommands.handler;

import dev.patrys.customcommands.platform.PlatformSender;

@FunctionalInterface
public interface UsageHandler {
    void handle(PlatformSender sender, String usage);
}