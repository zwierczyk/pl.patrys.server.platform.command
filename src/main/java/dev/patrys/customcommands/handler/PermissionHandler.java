package dev.patrys.customcommands.handler;

import dev.patrys.customcommands.platform.PlatformSender;

@FunctionalInterface
public interface PermissionHandler {
    void handle(PlatformSender sender, String permission);
}