package dev.patrys.customcommands;

import dev.patrys.customcommands.argument.ArgumentResolver;
import dev.patrys.customcommands.platform.Platform;

public class CustomCommands {
    private final Platform platform;
    private final CommandRegistry registry;

    private CustomCommands(Platform platform) {
        this.platform = platform;
        this.registry = new CommandRegistry(platform);
    }

    public static CustomCommands create(Platform platform) {
        return new CustomCommands(platform);
    }

    public CustomCommands register(Object... commands) {
        for (Object command : commands) {
            registry.register(command);
        }
        return this;
    }

    public <T> CustomCommands registerArgumentResolver(ArgumentResolver<T> resolver) {
        registry.getResolverRegistry().register(resolver);
        return this;
    }

    public Platform getPlatform() {
        return platform;
    }

    public CommandRegistry getRegistry() {
        return registry;
    }
}