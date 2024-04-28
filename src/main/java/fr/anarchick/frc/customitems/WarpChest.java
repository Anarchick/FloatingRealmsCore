package fr.anarchick.frc.customitems;

import fr.anarchick.anapi.bukkit.BukkitUtils;
import fr.anarchick.anapi.bukkit.PlayerUtils;
import fr.anarchick.anapi.bukkit.customItem.CustomItemManager;
import fr.anarchick.anapi.bukkit.inventory.ItemBuilder;
import fr.anarchick.anapi.bukkit.morepersistentdatatypes.DataType;
import fr.anarchick.anapi.java.NumberUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class WarpChest extends AbstractCustomItem implements Listener {
	
	private static final String ID = "warp-chest";
	private static final ItemStack ITEM;
	
	final private static String FULL = "<red>Le coffre de téléportation est plein !";
	final private static String IS_DEFINE = "<green>Le coffre de téléportation est défini";
	final private static String IS_NOT_DEFINE = "<red>Le coffre de téléportation n'est pas défini";
	final private static String NOT_CONTAINER = "<red>Le bloc visé n'a pas d'inventaire";
	final private static String DEFINE = "<green>Le coffre de téléportation a été défini";
	final private static String REMOVE = "<red>Le coffre de téléportation a été détruit";

	private static final Map<Player, Block> CACHE = new HashMap<>();

	static {
		String name = BukkitUtils.gradientString("Coffre de téléportation", new Color(77, 135, 87), new Color(78, 78, 242));
		
		ITEM = new ItemBuilder(Material.ENDER_CHEST)
				.setName(name)
				.addLore("<gold>Quand il est tenue en main",
						 "<gold>tous les items récupérés sont",
						 "<gold>transférés dans ton coffre",
						 "",
						 "<gray>Clic droit sur ton coffre",
						 "<gray>pour le définir comme cible")
				.setCustomModelData(1)
				.addEnchantment(Enchantment.ARROW_INFINITE, 1, true)
				.build();
	}

	private final NamespacedKey namespaceKey = getNamespacedKey(ID);

	public WarpChest() {
		super(ID);
	}

	@Override
	protected ItemStack getItem() {
		return ITEM;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void onRightClick(PlayerInteractEvent event) {
		Player player = event.getPlayer();

		if (event.isCancelled() && !event.getAction().equals(Action.RIGHT_CLICK_AIR) || player.hasCooldown(ITEM.getType())) {
			return;
		}

		event.setCancelled(true);
		Block block = event.getClickedBlock();


		if (block == null) {

			if (getWarpInventory(player) == null) {
				PlayerUtils.sendMessage(player, IS_NOT_DEFINE, false);
			} else {
				PlayerUtils.sendMessage(player, IS_DEFINE, false);
			}

		} else if (!(block.getState() instanceof Container)) {
			PlayerUtils.sendMessage(player, NOT_CONTAINER, false);
			player.playSound(player, Sound.ITEM_SHIELD_BREAK, SoundCategory.PLAYERS, 1f, 1.5f);
		} else if (!player.hasCooldown(ITEM.getType())) {
			PersistentDataContainer pdc = player.getPersistentDataContainer();
			pdc.set(namespaceKey, DataType.LOCATION, block.getLocation());
			CACHE.put(player, block);
			PlayerUtils.sendActionBar(player, DEFINE, false);
			player.getWorld().spawnParticle(Particle.SPELL_WITCH, block.getLocation(), 10, 1, 1, 1);
			player.playSound(block.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_HIT, SoundCategory.PLAYERS, 1f, 0.5f);
		}

		player.setCooldown(ITEM.getType(), 20*1);
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler(ignoreCancelled = true)
    public void onItemPickUp(EntityPickupItemEvent event) {
		if (!(event.getEntity() instanceof Player)) {
			return;
		}

		Player player = (Player) event.getEntity();
		ItemStack mainItem = player.getInventory().getItemInMainHand();
		ItemStack offItem = player.getInventory().getItemInOffHand();

		if (CustomItemManager.is(this, mainItem) || CustomItemManager.is(this, offItem)) {
			Item entItem = event.getItem();
			Inventory inv = getWarpInventory(player);

			if (inv != null) {
				event.setCancelled(true);
				ItemStack item = event.getItem().getItemStack();
				Map<Integer, ItemStack> result = inv.addItem(item);

				if (result.isEmpty()) {
					player.playSound(entItem, Sound.BLOCK_LAVA_POP, SoundCategory.PLAYERS, 1f, NumberUtils.getRandomDouble(0, 2).floatValue());
					entItem.getWorld().spawnParticle(Particle.SPELL_WITCH, entItem.getLocation(), 5);
					entItem.remove();
				} else {
					entItem.setItemStack(result.get(0));
					PlayerUtils.sendActionBar(player, FULL,false);
					event.setCancelled(false);
				}

			} else {
				PersistentDataContainer pdc = player.getPersistentDataContainer();
				pdc.remove(namespaceKey);
				if (CACHE.remove(player) != null) {
					PlayerUtils.sendMessage(player, REMOVE, false);
				}
			}
		}
	}
    
	@Nullable
	private Inventory getWarpInventory(Player player) {
		@Nullable Block block = CACHE.get(player);

		if (block == null) {
			PersistentDataContainer pdc = player.getPersistentDataContainer();

			if (pdc.has(namespaceKey, DataType.LOCATION)) {
				block = pdc.get(namespaceKey, DataType.LOCATION).getBlock();
				CACHE.put(player, block);
			}

		}

		if (block != null && block.getState() instanceof Container) {
			return ((Container)block.getState()).getInventory();
		}

		return null;
	}
	
}
