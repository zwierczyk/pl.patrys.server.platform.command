package dev.patrys.customcommands.handler;

import dev.patrys.customcommands.platform.PlatformSender;

@FunctionalInterface
public interface PlayerOnlyHandler {
    void handle(PlatformSender sender);
}