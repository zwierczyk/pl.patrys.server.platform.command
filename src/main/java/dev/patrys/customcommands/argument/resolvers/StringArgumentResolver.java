package dev.patrys.customcommands.argument.resolvers;

import dev.patrys.customcommands.argument.ArgumentResolver;
import dev.patrys.customcommands.platform.PlatformSender;

public class StringArgumentResolver implements ArgumentResolver<String> {
    @Override
    public String resolve(PlatformSender sender, String argument) {
        return argument;
    }

    @Override
    public Class<String> getType() {
        return String.class;
    }
}