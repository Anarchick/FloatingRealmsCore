package fr.anarchick.frc;

import fr.anarchick.anapi.bukkit.Logger;
import fr.anarchick.anapi.bukkit.customItem.CustomItemManager;
import fr.anarchick.frc.customitems.*;
import fr.anarchick.frc.overworld.Listeners;
import fr.anarchick.frc.customitems.BoomStick;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class FloatingRealmsCore extends JavaPlugin {

    private static Logger LOGGER;
    private static FloatingRealmsCore INSTANCE = null;

    @Override
    public void onEnable() {
        INSTANCE = this;
        LOGGER = new Logger(this);

        final PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new Listeners(), this);

        //getCommand("valheimfood").setExecutor(new ValheimCommand());

        reload(Bukkit.getConsoleSender());

    }

    @Override
    public void onDisable() {
        CustomItemManager.unregister(this);
    }

    public void reload(final @NotNull CommandSender sender) {
        saveDefaultConfig();
        reloadConfig();

        // CustomItems init
        CustomItemManager.unregister(this);
        new XPStorage();
        new InfinityMilk();
        new WarpChest();
        new HorseHorn();
        Xray.init();
        new BoomStick();

        info(sender, "<green>FloatingRealmsCore has been reloaded");
    }

    public static FloatingRealmsCore getInstance() {
        return INSTANCE;
    }


    public static void info(CommandSender sender, String message) {
        LOGGER.info(sender, message);
    }

    public static void warn(CommandSender sender, String message) {
        LOGGER.warn(sender, message);
    }

}
