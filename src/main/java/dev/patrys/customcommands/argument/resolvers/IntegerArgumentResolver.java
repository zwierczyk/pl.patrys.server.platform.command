package dev.patrys.customcommands.argument.resolvers;

import dev.patrys.customcommands.argument.ArgumentResolver;
import dev.patrys.customcommands.platform.PlatformSender;

public class IntegerArgumentResolver implements ArgumentResolver<Integer> {
    @Override
    public Integer resolve(PlatformSender sender, String argument) {
        try {
            return Integer.parseInt(argument);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("§c'" + argument + "' nie jest prawidłową liczbą!");
        }
    }

    @Override
    public Class<Integer> getType() {
        return Integer.class;
    }
}