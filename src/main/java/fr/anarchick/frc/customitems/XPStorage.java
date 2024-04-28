package fr.anarchick.frc.customitems;

import fr.anarchick.anapi.bukkit.BukkitUtils;
import fr.anarchick.anapi.bukkit.PlayerUtils;
import fr.anarchick.anapi.bukkit.Scheduling;
import fr.anarchick.anapi.bukkit.customItem.CustomItem;
import fr.anarchick.anapi.bukkit.customItem.CustomItemManager;
import fr.anarchick.anapi.bukkit.inventory.ItemBuilder;
import fr.anarchick.frc.utils.Characters;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownExpBottle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ExpBottleEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public class XPStorage extends AbstractCustomItem implements Listener {
	
	public static final String ID = "xp-storage";
	private static final String name = BukkitUtils.gradientString(
			"Stockage XP",
			new Color(240, 183, 91),
			new Color(178, 237, 90)
	);
	private static final String[] lores = {
			"<gold>Experience contenu : %d",
			"",
			"<gray>Click droit sur une table",
			"<gray>d'enchantement pour remplir"
	};
	private static final String MESSAGE_NOT_ENOUGH_EXP = "<red>Tu n'as plus d'experience";
	private static final String MESSAGE_NOT_ENOUGH_SPACE = "<red>Tu n'as plus de place dans l'inventaire";
	private static final ItemStack GLASS_BOTTLE = new ItemStack(Material.GLASS_BOTTLE);

	private final NamespacedKey namespaceKeyId = getNamespacedKey("stored_xp_id");
	private final NamespacedKey namespaceKeyAmount = getNamespacedKey("stored_xp_amount");

	public XPStorage() {
		super(ID);
	}

	@Override
	@NotNull
	protected ItemStack getItem() {
		final ItemStack item = new ItemBuilder(Material.EXPERIENCE_BOTTLE)
				.setName(name)
				.addLore(lores)
				.setCustomModelData(1)
				// make unstackable
				.setPersistentDataValue(namespaceKeyId, PersistentDataType.STRING, UUID.randomUUID().toString())
				.setPersistentDataValue(namespaceKeyAmount, PersistentDataType.INTEGER, 0)
				.build();
		storeXP(item, 0);
		return item;
	}
	
	@Override
	public void onRightClick(final PlayerInteractEvent event) {
		final Player player = event.getPlayer();
		final @Nullable Block block = event.getClickedBlock();

		if (block != null && block.getType().equals(Material.ENCHANTING_TABLE)) {
			event.setCancelled(true);
			int total = BukkitUtils.getTotalXP(player);
			int amount = (int) Math.ceil(total * 0.1);

			if (amount > 0) {
				final String str = String.format("<green>-%d<white>"+ Characters.XP.getFirst(), amount);
				BukkitUtils.setTotalXP(player, total - amount);
				player.getInventory().setItem(EquipmentSlot.HAND, storeXP(event.getItem(), amount));
				player.playSound(player, Sound.ITEM_BOTTLE_EMPTY, SoundCategory.PLAYERS, 0.5f, 2f);
				PlayerUtils.sendActionBar(player, "", false);
				Scheduling.syncDelay(1, () -> PlayerUtils.sendActionBar(player, str, false));
			} else {
				PlayerUtils.sendActionBar(player, MESSAGE_NOT_ENOUGH_EXP, false);
			}
		}
	}

	/*
	@EventHandler(ignoreCancelled = true)
	public void onUse(final PlayerInteractEvent event) {
		ItemStack item = event.getItem();

		if (GLASS_BOTTLE.isSimilar(item)) {
			event.setCancelled(true);
			Player player = event.getPlayer();
			int amount = item.getAmount();
			item.setAmount(--amount);
			if (!event.getPlayer().getInventory().addItem(CustomItemManager.getItem(this, 1)).isEmpty()) {
				item.setAmount(++amount);
				PlayerUtils.sendActionBar(player, MESSAGE_NOT_ENOUGH_SPACE, false);
			} else {
				player.playSound(player, Sound.ITEM_BOTTLE_EMPTY, SoundCategory.PLAYERS, 0.5f, 0f);
			}
		}
	}
	*/

	@EventHandler
	public void onExpBottle(ExpBottleEvent event) {
		ThrownExpBottle ent = event.getEntity();
		ItemStack item = ent.getItem();
		CustomItem customItem = CustomItemManager.getFromItemStack(item);

		if (customItem != null) {
			int amount = getStoredXP(item);
			event.setExperience(amount);
		}
	}
	
	@SuppressWarnings("deprecation")
	private ItemStack storeXP(@NotNull ItemStack item, int amount) {
		final ItemMeta itemMeta = item.getItemMeta();
		final PersistentDataContainer pdc = itemMeta.getPersistentDataContainer();
		final String[] loresModified = lores.clone();

		if (pdc.has(namespaceKeyAmount, PersistentDataType.INTEGER)) {
			int stored = amount + pdc.getOrDefault(namespaceKeyAmount, PersistentDataType.INTEGER, 0);
			loresModified[0] = String.format(lores[0], stored);
			pdc.set(namespaceKeyAmount, PersistentDataType.INTEGER, stored);

			List<Component> loreComponent = Stream.of(loresModified).map(lore -> deserialize(lore)
					.decoration(TextDecoration.ITALIC, false)).toList();

			itemMeta.lore(loreComponent);
			item.setItemMeta(itemMeta);
		}
		return item;
		
	}
	
	private Integer getStoredXP(@NotNull ItemStack item) {
		ItemMeta itemMeta = item.getItemMeta();
		PersistentDataContainer pdc = itemMeta.getPersistentDataContainer();
		return pdc.getOrDefault(namespaceKeyAmount, PersistentDataType.INTEGER, 0);
	}
	
}
