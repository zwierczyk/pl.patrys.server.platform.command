package dev.patrys.custommenu;

import dev.patrys.custommenu.animation.MenuAnimation;
import org.bukkit.entity.Player;

public abstract class AnimatedMenu extends Menu {

    private MenuAnimation animation;

    public AnimatedMenu(String title, int rows) {
        super(title, rows);
    }

    public void setAnimation(MenuAnimation animation) {
        this.animation = animation;
    }

    @Override
    public void open(Player player) {
        super.open(player);

        if (animation != null) {
            animation.play(player);
        }
    }
}