package dev.patrys.customcommands.handler;

import dev.patrys.customcommands.platform.PlatformSender;

@FunctionalInterface
public interface ConsoleOnlyHandler {
    void handle(PlatformSender sender);
}