package fr.anarchick.frc.customitems;

import fr.anarchick.anapi.bukkit.BukkitUtils;
import fr.anarchick.anapi.bukkit.inventory.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;

import java.awt.*;

public class GrapplingHook extends AbstractCustomItem implements Listener {

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
		// + monter & descendre en fonction du regard du joueur tant que le clic droit est maintenu
		// Le joueur dois chevaucher une entité invisible afin d'avoir l'animation assise lorsqu'il utilise le grapin
		// Le grappin à un effet balancier réaliste (= modifier la vélocité de l'entité et non pas la TP pour éviter les glitch)
	}

	@EventHandler(ignoreCancelled = true)
	public void onSneak(PlayerToggleSneakEvent event) {
		// TODO relacher le grappin
	}

	@EventHandler(ignoreCancelled = true)
	public void onMine(BlockBreakEvent event) {
		// TODO relacher le grappin si le le bloc miné est celui auquel le joueur est accroché
		// Utiliser une HashMAp<Block, Player>
	}
	
}
