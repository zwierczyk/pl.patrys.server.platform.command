package dev.patrys.customcommands.argument.resolvers;

import dev.patrys.customcommands.argument.ArgumentResolver;
import dev.patrys.customcommands.exception.PlayerNotFoundException;
import dev.patrys.customcommands.platform.PlatformSender;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class OfflinePlayerArgumentResolver implements ArgumentResolver<OfflinePlayer> {

    @Override
    @SuppressWarnings("deprecation")
    public OfflinePlayer resolve(PlatformSender sender, String argument) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(argument);

        if (!player.hasPlayedBefore() && !player.isOnline()) {
            throw new PlayerNotFoundException(argument);
        }

        return player;
    }

    @Override
    public List<String> suggest(PlatformSender sender, String argument) {
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> name.toLowerCase().startsWith(argument.toLowerCase()))
                .collect(Collectors.toList());
    }

    @Override
    public Class<OfflinePlayer> getType() {
        return OfflinePlayer.class;
    }
}