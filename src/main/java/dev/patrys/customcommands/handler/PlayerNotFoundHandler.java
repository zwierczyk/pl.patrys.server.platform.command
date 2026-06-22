package dev.patrys.customcommands.handler;

import dev.patrys.customcommands.platform.PlatformSender;

@FunctionalInterface
public interface PlayerNotFoundHandler {
    void handle(PlatformSender sender, String targetName);
}