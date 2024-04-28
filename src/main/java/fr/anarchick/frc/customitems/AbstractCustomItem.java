package fr.anarchick.frc.customitems;

import fr.anarchick.anapi.bukkit.customItem.CustomItem;
import fr.anarchick.frc.FloatingRealmsCore;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractCustomItem extends CustomItem {

    public AbstractCustomItem(@NotNull String id) {
        super(FloatingRealmsCore.getInstance(), id);
    }

}
