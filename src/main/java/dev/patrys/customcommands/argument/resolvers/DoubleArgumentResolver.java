package dev.patrys.customcommands.argument.resolvers;

import dev.patrys.customcommands.argument.ArgumentResolver;
import dev.patrys.customcommands.platform.PlatformSender;

public class DoubleArgumentResolver implements ArgumentResolver<Double> {
    @Override
    public Double resolve(PlatformSender sender, String argument) {
        try {
            return Double.parseDouble(argument);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("§c'" + argument + "' nie jest prawidłową liczbą!");
        }
    }

    @Override
    public Class<Double> getType() {
        return Double.class;
    }
}