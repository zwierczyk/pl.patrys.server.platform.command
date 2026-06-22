package dev.patrys.customcommands.argument.resolvers;

import dev.patrys.customcommands.argument.ArgumentResolver;
import dev.patrys.customcommands.platform.PlatformSender;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PlayerArgumentResolver implements ArgumentResolver<Player> {
    @Override
    public Player resolve(PlatformSender sender, String argument) {
        Player player = Bukkit.getPlayer(argument);
        if (player == null) {
            throw new IllegalArgumentException("§cGracz '" + argument + "' nie został znaleziony!");
        }
        return player;
    }

    @Override
    public Class<Player> getType() {
        return Player.class;
    }
}