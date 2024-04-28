package fr.anarchick.frc.customitems;

import fr.anarchick.anapi.bukkit.BukkitUtils;
import fr.anarchick.anapi.bukkit.EntityUtils;
import fr.anarchick.anapi.bukkit.inventory.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.awt.*;

public class InfinityMilk extends AbstractCustomItem {

	public static final String ID = "InfinityMilk";
	private static final ItemStack ITEM;
	private static final int minutes = 3;
	
	static {
		final String name = BukkitUtils.gradientString(
				"Lait Magique",
				new Color(230, 50, 200),
				new Color(30, 175, 240)
		);

		ITEM = new ItemBuilder(Material.MILK_BUCKET)
				.setName(name)
				.addLore("<gold>Retire les effets négatifs",
						"",
						"<gray>Utilisable toutes",
						String.format("<gray>les %d minutes", minutes))
				.setCustomModelData(1)
				.addEnchantment(Enchantment.ARROW_INFINITE, 1, true)
				.build();
	}

	public InfinityMilk() {
		super(ID);
	}

	@Override
	protected ItemStack getItem() {
		return ITEM;
	}
	
	@Override
	public void onConsume(PlayerItemConsumeEvent event) {
		final Player player = event.getPlayer();

		if (!event.isCancelled() && !player.hasCooldown(ITEM.getType())) {
			event.setCancelled(true);
			EntityUtils.milk(player, PotionEffectType.Category.HARMFUL);
			event.getPlayer().setCooldown(ITEM.getType(), 20*60*minutes);
		}

	}
	
}
