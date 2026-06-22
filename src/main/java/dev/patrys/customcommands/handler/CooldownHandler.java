package dev.patrys.customcommands.handler;

import dev.patrys.customcommands.platform.PlatformSender;

@FunctionalInterface
public interface CooldownHandler {
    void handle(PlatformSender sender, long remainingSeconds);
}