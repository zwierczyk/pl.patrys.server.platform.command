package dev.patrys.customcommands.argument;

import dev.patrys.customcommands.platform.PlatformSender;

import java.util.Collections;
import java.util.List;

public interface ArgumentResolver<T> {

    // Zamienia tekst (np. "patrys") na obiekt (np. Player)
    T resolve(PlatformSender sender, String argument);

    // Odpowiada za podpowiedzi w Tabie (domyślnie pusta lista)
    default List<String> suggest(PlatformSender sender, String argument) {
        return Collections.emptyList();
    }

    Class<T> getType();
}