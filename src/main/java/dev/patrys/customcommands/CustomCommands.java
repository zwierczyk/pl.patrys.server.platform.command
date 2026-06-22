package dev.patrys.customcommands;

import dev.patrys.customcommands.argument.ArgumentResolver;
import dev.patrys.customcommands.handler.CooldownHandler;
import dev.patrys.customcommands.handler.PermissionHandler;
import dev.patrys.customcommands.handler.PlayerNotFoundHandler;
import dev.patrys.customcommands.handler.UsageHandler;
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

    public CustomCommands permissionHandler(PermissionHandler handler) {
        registry.getHandlerRegistry().setPermissionHandler(handler);
        return this;
    }

    public CustomCommands usageHandler(UsageHandler handler) {
        registry.getHandlerRegistry().setUsageHandler(handler);
        return this;
    }

    public CustomCommands cooldownHandler(CooldownHandler handler) {
        registry.getHandlerRegistry().setCooldownHandler(handler);
        return this;
    }

    // TUTAJ JEST BRAKUJĄCA METODA:
    public CustomCommands playerNotFoundHandler(PlayerNotFoundHandler handler) {
        registry.getHandlerRegistry().setPlayerNotFoundHandler(handler);
        return this;
    }

    public Platform getPlatform() {
        return platform;
    }

    public CommandRegistry getRegistry() {
        return registry;
    }
}