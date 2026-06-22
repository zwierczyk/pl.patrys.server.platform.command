package dev.patrys.customcommands.argument;

import dev.patrys.customcommands.platform.PlatformSender;

public interface ArgumentResolver<T> {
    T resolve(PlatformSender sender, String argument);
    Class<T> getType();
}