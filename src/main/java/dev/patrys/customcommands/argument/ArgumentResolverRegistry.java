package dev.patrys.customcommands.argument;

import dev.patrys.customcommands.argument.resolvers.*;

import java.util.HashMap;
import java.util.Map;

public class ArgumentResolverRegistry {
    private final Map<Class<?>, ArgumentResolver<?>> resolvers = new HashMap<>();

    public ArgumentResolverRegistry() {
        registerDefaultResolvers();
    }

    private void registerDefaultResolvers() {
        register(new StringArgumentResolver());
        register(new IntegerArgumentResolver());
        register(new DoubleArgumentResolver());
        register(new PlayerArgumentResolver());
        register(new OfflinePlayerArgumentResolver());
    }

    public <T> void register(ArgumentResolver<T> resolver) {
        resolvers.put(resolver.getType(), resolver);
    }

    @SuppressWarnings("unchecked")
    public <T> ArgumentResolver<T> getResolver(Class<T> type) {
        return (ArgumentResolver<T>) resolvers.get(type);
    }

    public boolean hasResolver(Class<?> type) {
        return resolvers.containsKey(type);
    }
}