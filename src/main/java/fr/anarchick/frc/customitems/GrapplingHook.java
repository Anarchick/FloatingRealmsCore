package fr.anarchick.frc.customitems;

import fr.anarchick.anapi.bukkit.BukkitUtils;
import fr.anarchick.anapi.bukkit.inventory.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;

import java.awt.*;

public class GrapplingHook extends AbstractCustomItem {

	public static final String ID = "GrapplingHook";
	private static final ItemStack ITEM;
	private static final int MAX_DISTANCE = 20;

	static {
		final String name = BukkitUtils.gradientString(
				"Grappin",
				new Color(230, 50, 200),
				new Color(30, 175, 240)
		);

		ITEM = new ItemBuilder(Material.PAPER)
				.setName(name)
				.addLore("<gold>Permet de s'accrocher",
						"<gold>à un bloc pour miner en hauteur",
						"",
						"<gray>Maintenir le clic droit",
						"<gray>pour monter/descendre",
						"<gray>selon la direction du regard",
						"",
						"<gray>S'accroupir pour lâcher",
						"",
						"<gray>Peut être combiné",
						"<gray>avec une pioche",
						"<gray>dans une enclume"
				)
				.setCustomModelData(1)
				.build();
	}

	public GrapplingHook() {
		super(ID);
	}

	@Override
	protected ItemStack getItem() {
		return ITEM;
	}

	@Override
	public void onRightClick(PlayerInteractEvent event) {
		// TODO lancer le grappin
	}

	@EventHandler(ignoreCancelled = true)
	public void onSneak(PlayerToggleSneakEvent event) {
		// TODO arreter le grappin
	}
	
}
