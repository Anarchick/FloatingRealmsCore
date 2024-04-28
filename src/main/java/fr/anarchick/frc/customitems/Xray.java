package fr.anarchick.frc.customitems;

import com.destroystokyo.paper.MaterialTags;
import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import fr.anarchick.anapi.bukkit.BukkitUtils;
import fr.anarchick.anapi.bukkit.EntityUtils;
import fr.anarchick.anapi.bukkit.PaperComponentUtils;
import fr.anarchick.anapi.bukkit.Scheduling;
import fr.anarchick.anapi.bukkit.customItem.CustomItemManager;
import fr.anarchick.anapi.bukkit.inventory.ItemBuilder;
import fr.anarchick.anapi.java.Pair;
import fr.anarchick.anapi.java.Utils;
import fr.anarchick.frc.FloatingRealmsCore;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.*;

public class Xray extends AbstractCustomItem implements Listener {

	private static final String ID = "xray";
	private static final ItemStack ITEM;
	private static final String NAME = BukkitUtils.gradientString(
			"Xray",
			new Color(89, 208, 227),
			new Color(89, 227, 169)
	);
	private static final double OFFSET = 0.01;
	private static final PotionEffect EFFECT = new PotionEffect(PotionEffectType.BLINDNESS, 6, 0, false, false, false);
	private static final Set<Player> PLAYERS = new HashSet<>();
	private static final Map<Block, Pair<BlockDisplay, Long>> MANAGER_BLOCK = new HashMap<>();
	private static final int duration = 20*5;

	private static final int TICK = 5;
	private static int TICK_TASK_ID;

	private static final String[] LORE = {
			"<gold>Affiche la position des",
			"<gold>minerais proche"
	};
	private static final String[] ADDITIONAL_LORE = {
			"",
			"<gray>Clic droit pour activer",
			"",
			"<gray>Peut être combiné",
			"<gray>avec un casque",
			"<gray>dans une enclume"
	};

	private static Xray INSTANCE;
	
	static {
		ITEM = new ItemBuilder(Material.PAPER)
				.setName(NAME)
				.setLore(LORE)
				.addLore(ADDITIONAL_LORE)
				.setCustomModelData(5)
				.build();
	}

	public static void init() {
		if (INSTANCE == null) {
			INSTANCE = new Xray();

			for (Player player : Bukkit.getOnlinePlayers()) {
				INSTANCE.reload(player);
			}

			TICK_TASK_ID = Bukkit.getScheduler().scheduleSyncRepeatingTask(FloatingRealmsCore.getInstance(), INSTANCE::tick, 1, TICK);
		}
	}

	private Xray() {
		super(ID);
	}

	@NotNull
	@Override
	protected ItemStack getItem() {
		return ITEM;
	}

	@Override
	public void onRightClick(PlayerInteractEvent event) {
		// Not xray helmet
		if (event.getItem() != null && event.getItem().getType().equals(ITEM.getType()))  {
			event.setCancelled(true);
			use(event.getPlayer(), true);
		}
	}

	private void activationEffect(Player player) {
		player.addPotionEffect(EFFECT);
		player.playSound(player, Sound.BLOCK_BEACON_ACTIVATE, SoundCategory.PLAYERS, 2f, 2f);
		player.playSound(player, Sound.BLOCK_BEACON_POWER_SELECT, SoundCategory.PLAYERS, 2f, 1.5f);
	}

	private void use(Player player, boolean playEffects) {
		int r = 10;
		Long now = Utils.now();
		LinkedList<Block> blocks = new LinkedList<>();

		if (playEffects) {
			activationEffect(player);
		}

		for (Block block : BukkitUtils.ellipsoid(player.getLocation(), r, r, r)) {

			boolean found = switch (block.getType()) {
				case COAL_ORE, DEEPSLATE_COAL_ORE -> glow(block, org.bukkit.Color.BLACK, now);
				case COPPER_ORE, DEEPSLATE_COPPER_ORE -> glow(block, org.bukkit.Color.ORANGE, now);
				case IRON_ORE, DEEPSLATE_IRON_ORE -> glow(block, org.bukkit.Color.SILVER, now);
				case GOLD_ORE, DEEPSLATE_GOLD_ORE, NETHER_GOLD_ORE -> glow(block, org.bukkit.Color.YELLOW, now);
				case REDSTONE_ORE, DEEPSLATE_REDSTONE_ORE -> glow(block, org.bukkit.Color.RED, now);
				case EMERALD_ORE, DEEPSLATE_EMERALD_ORE -> glow(block, org.bukkit.Color.LIME, now);
				case LAPIS_ORE, DEEPSLATE_LAPIS_ORE -> glow(block, org.bukkit.Color.BLUE, now);
				case DIAMOND_ORE, DEEPSLATE_DIAMOND_ORE -> glow(block, org.bukkit.Color.AQUA, now);
				default -> false;
			};

			if (found) {
				blocks.add(block);
			}

		}

		if (playEffects && blocks.isEmpty()) {
			player.playSound(player, Sound.BLOCK_LAVA_EXTINGUISH, SoundCategory.PLAYERS, 2f, 1.5f);
			return;
		}

		Scheduling.syncDelay(duration, () -> {
			if (playEffects) {
				player.playSound(player, Sound.BLOCK_BEACON_DEACTIVATE, SoundCategory.PLAYERS, 2f, 1.5f);
			}
			remove(blocks, now);
		});

	}

	private boolean glow(Block block, org.bukkit.Color color, Long now) {
		final Pair<BlockDisplay, Long> previousPair = MANAGER_BLOCK.get(block);

		Scheduling.syncDelay(2, () -> {
			if (previousPair != null) {
				previousPair.first().remove();
			}
		});

		BlockDisplay display = (BlockDisplay) block.getWorld().spawnEntity(block.getLocation().add(OFFSET, OFFSET, OFFSET), EntityType.BLOCK_DISPLAY, CreatureSpawnEvent.SpawnReason.CUSTOM);
		display.setBlock(block.getBlockData());
		display.setGlowColorOverride(color);
		display.setGlowing(true);
		EntityUtils.mustBeRemovedWhenLoaded(display);
		Pair<BlockDisplay, Long> pair = new Pair<>(display, now);
		MANAGER_BLOCK.put(block, pair);
		return true;
	}

	private void remove(final LinkedList<Block> blocks, final Long now) {
		for (final Block block : blocks) {
			final Pair<BlockDisplay, Long> pair = MANAGER_BLOCK.get(block);

			if (pair != null && pair.second().equals(now)) {
				pair.first().remove();
				MANAGER_BLOCK.remove(block);
			}
		}
	}

	private void remove(final Block block) {
		final Pair<BlockDisplay, Long> pair = MANAGER_BLOCK.remove(block);

		if (pair != null) {
			pair.first().remove();
		}
	}

	public static void removeAll() {
		for (Pair<BlockDisplay, Long> pair : MANAGER_BLOCK.values()) {
			pair.first().remove();
		}

		MANAGER_BLOCK.clear();
	}

	private void reload(Player player) {
		ItemStack helmet = player.getInventory().getItem(EquipmentSlot.HEAD);

		if (CustomItemManager.is(this, helmet)) {
			PLAYERS.add(player);
			use(player, false);
		} else {
			PLAYERS.remove(player);
		}
	}

	private void tick() {
		for (Player player : PLAYERS) {
			use(player, false);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onMine(BlockBreakEvent event) {
		remove(event.getBlock());
	}

	@EventHandler(ignoreCancelled = true)
	public void onPluginDisable(PluginDisableEvent event) {
		if (event.getPlugin().equals(FloatingRealmsCore.getInstance())) {
			removeAll();
			Bukkit.getScheduler().cancelTask(TICK_TASK_ID);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onEquip(PlayerArmorChangeEvent event) {
		Scheduling.syncDelay(1, () -> {
			Player player = event.getPlayer();
			reload(player);

			if (event.getSlotType().equals(PlayerArmorChangeEvent.SlotType.HEAD) && PLAYERS.contains(player)) {
				activationEffect(player);
			}

		});
	}

	private boolean isDefaultXray(ItemStack item) {
		return CustomItemManager.is(this, item) && !MaterialTags.HEAD_EQUIPPABLE.isTagged(item);
	}

	private boolean isHelmetXray(ItemStack item) {
		return CustomItemManager.is(this, item) && MaterialTags.HEAD_EQUIPPABLE.isTagged(item);
	}

	@EventHandler(ignoreCancelled = true)
	public void onAnvil(PrepareAnvilEvent event) {
		AnvilInventory inv = event.getInventory();
		ItemStack first = inv.getFirstItem();
		ItemStack second = inv.getSecondItem();

		// One of the item is not set
		if (first == null || second == null) {
			return;
		}

		// One of the item is not set
		// in 1.20.4 , the event.getResult() can be null
		// but in future it may return a non-null value
		if (first.getType().isAir() || second.getType().isAir()) {
			return;
		}

		if (!MaterialTags.HEAD_EQUIPPABLE.isTagged(first) && !MaterialTags.HEAD_EQUIPPABLE.isTagged(second)) {
			return;
		}

		byte b = 0b000;

		// Minimum one of the item is the default Xray
		if (isDefaultXray(first) || isDefaultXray(second)) {
			b = 0b010;
		}

		//Minimum one of the item is the Xray helmet
		if (isHelmetXray(first) || isHelmetXray(second)) {
			b |= 0b100;
		}

        if (b == 0b010) { // create xray helmet
            inv.setRepairCost(30);
            ItemStack helmet = (isDefaultXray(first) ? second : first).clone();
            event.setResult(transform(helmet));
        }
	}

	/**
	 * transform the default helmet to xray helmet
	 */
	private ItemStack transform(@NotNull ItemStack helmet) {
		if (isHelmetXray(helmet)) {
			return helmet;
		}

		String rename;

		if (helmet.hasItemMeta() && helmet.getItemMeta().hasDisplayName()) {
			// helmet has a custom name
			Component componentName = helmet.getItemMeta().displayName();
            assert componentName != null;
            rename = NAME + " " + PaperComponentUtils.DEFAULT_MINIMESSAGE.serialize(componentName);
		} else {
			// helmet has no custom name
			rename = NAME + " " + String.format("<lang:%s>", helmet.translationKey());
		}

		ItemStack newHelmet = new ItemBuilder(helmet)
				.setName(rename)
				.addLore(LORE)
				.build();

		return adapt(newHelmet);
	}


	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void onAnvilCraft(InventoryClickEvent event) {
		if (event.getClickedInventory() instanceof AnvilInventory inv) {
			if (event.getSlotType().equals(InventoryType.SlotType.RESULT)) {
				ItemStack result = inv.getItem(2);

				if (isHelmetXray(result)) {
					ItemStack first = inv.getFirstItem();
                    assert first != null;
                    first.setAmount(first.getAmount() - 1);
					ItemStack second = inv.getSecondItem();
                    assert second != null;
                    second.setAmount(second.getAmount() - 1);

					Scheduling.syncDelay(1, () -> {
						inv.setFirstItem(first);
						inv.setSecondItem(second);
					});
				}
			}
		}
	}
	
}
