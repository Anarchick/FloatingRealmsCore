package fr.anarchick.frc.customitems;

import fr.anarchick.anapi.bukkit.BukkitUtils;
import fr.anarchick.anapi.bukkit.EntityUtils;
import fr.anarchick.anapi.bukkit.PaperComponentUtils;
import fr.anarchick.anapi.bukkit.PlayerUtils;
import fr.anarchick.anapi.bukkit.inventory.ItemBuilder;
import fr.anarchick.anapi.java.NumberUtils;
import fr.anarchick.anapi.java.Utils;
import fr.anarchick.frc.FloatingRealmsCore;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Horse.Style;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class HorseHorn extends AbstractCustomItem implements Listener {
	
	private static final String ID = "horse-horn";
	private static final Material MATERIAL = Material.PAPER;
	private static final int CUSTOM_MODEL_DATA = 12;
	private static final String NO_SPACE = "<red>Tu n'as pas de place dans ton inventaire";
	private static final String DISCOVER = "<#e0be4c>%s a obtenu : ";

	private static final String[] DEFAULT_LORE = {
			"<#FA412B>Vie : <white>? coeurs",
			"<#FA412B>Vitesse : <white>? b/s",
			"<#FA412B>Saut : <white>? blocs",
			"<#FA412B>Couleur : <white>?",
			"<#FA412B>Style : <white>?",
			"",
			"<gray>Clic droit pour",
			"<gray>invoquer ton cheval"
			};

	private static ItemBuilder getItemBuilder() {
		String name = BukkitUtils.gradientString(
				"Cheval portatif",
				new Color(250, 65, 43),
				new Color(179, 43, 250)
		);

		return new ItemBuilder(MATERIAL)
				.setName(name)
				//.addEnchantment(Enchantment.ARROW_INFINITE, 1, true)
				.setCustomModelData(CUSTOM_MODEL_DATA);
	}

	public HorseHorn() {
		super(ID);
	}

	@NotNull
	@Override
	protected ItemStack getItem() {
		return getItemBuilder()
				.addLore(DEFAULT_LORE)
				.build();
	}

	@NotNull
	private ItemStack generate(final @NotNull Player player) {
		float health = randomAttribute(player, 15, 30);
		float speed = randomAttribute(player, 0.1125f, 0.3375f);
		float jump = randomAttribute(player, 0.4f, 1.0f);
		String color = randomColor().name();
		String style = randomStyle().name();

		final List<String> lore = new ArrayList<>(List.of(DEFAULT_LORE));
		lore.set(0, String.format(DEFAULT_LORE[0].replace("?", "%d"), (int) (health /2)));
		lore.set(1, String.format(DEFAULT_LORE[1].replace("?", "%.2f"), speed *43.17f));
		lore.set(2, String.format(DEFAULT_LORE[2].replace("?", "%.2f"), (Math.pow(jump, 1.7))*5.293f));
		lore.set(3, String.format(DEFAULT_LORE[3].replace("?", "%s"), color));
		lore.set(4, String.format(DEFAULT_LORE[4].replace("?", "%s"), style));

		ItemStack item = getItemBuilder()
				.addLore(lore)
				.setPersistentDataValue(keyHealth, PersistentDataType.FLOAT, health)
				.setPersistentDataValue(keySpeed, PersistentDataType.FLOAT, speed)
				.setPersistentDataValue(keyJump, PersistentDataType.FLOAT, jump)
				.setPersistentDataValue(keyColor, PersistentDataType.STRING, color)
				.setPersistentDataValue(keyStyle, PersistentDataType.STRING, style)
				.build();
		return adapt(item);
	}

	private void spawn(final @NotNull Player player, final @NotNull PersistentDataContainer pdc) {
		World world = player.getWorld();
		world.playSound(player.getEyeLocation(), Sound.ENTITY_HORSE_AMBIENT, SoundCategory.AMBIENT, 0.5f, 1f);
		world.playSound(player.getEyeLocation(), Sound.ENTITY_HORSE_ARMOR, SoundCategory.AMBIENT, 0.5f, 1f);
		Horse horse = (Horse) world.spawnEntity(player.getLocation(), EntityType.HORSE);
		horse.setAdult();
		horse.setTamed(true);
		horse.setOwner(player);
		horse.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(pdc.get(keyHealth, PersistentDataType.FLOAT));
		horse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(pdc.get(keySpeed, PersistentDataType.FLOAT));
		horse.setJumpStrength(pdc.get(keyJump, PersistentDataType.FLOAT));
		horse.setStyle(Style.valueOf(pdc.get(keyStyle, PersistentDataType.STRING)) );
		horse.setColor(Horse.Color.valueOf(pdc.get(keyColor, PersistentDataType.STRING)));
		horse.getInventory().setSaddle(new ItemStack(Material.SADDLE));
		horse.addPassenger(player);
		horse.setMetadata(ID, new FixedMetadataValue(FloatingRealmsCore.getInstance(), true));
		EntityUtils.mustBeRemovedWhenLoaded(horse, true);
	}
	
	private final NamespacedKey keyHealth = getNamespacedKey("horse_horn_health");
	private final NamespacedKey keySpeed = getNamespacedKey("horse_horn_speed");
	private final NamespacedKey keyJump = getNamespacedKey("horse_horn_jump");
	private final NamespacedKey keyColor = getNamespacedKey("horse_horn_color");
	private final NamespacedKey keyStyle = getNamespacedKey("horse_horn_style");

	@Override
	public void onRightClick(PlayerInteractEvent event) {
		if (!event.getAction().equals(Action.RIGHT_CLICK_AIR)) {
			return;
		}

		Player player = event.getPlayer();
		PlayerInventory inv = player.getInventory();
		ItemStack item = event.getItem();

		if (!player.isOnGround() || player.hasCooldown(Material.BELL)) {
			return;
		}

		event.setCancelled(true);

		ItemMeta itemMeta = event.getItem().getItemMeta();
		PersistentDataContainer pdc = itemMeta.getPersistentDataContainer();

		if (pdc.has(keyHealth, PersistentDataType.FLOAT)) {
			spawn(player, pdc);
			return;
		}

		// GENERATE

		if (item.getAmount() != 1 && inv.firstEmpty() == -1) {
			PlayerUtils.sendMessage(player, NO_SPACE, false);
			player.playSound(player, Sound.ITEM_SHIELD_BREAK, SoundCategory.PLAYERS, 1f, 1.5f);
			return;
		}

		PlayerUtils.playTotemAnimation(player, CUSTOM_MODEL_DATA);
		ItemStack newitem = generate(player);

		player.sendMessage(newitem.getItemMeta().getDisplayName());
		for (Component component : newitem.lore().stream().limit(5).toList()) {
			player.sendMessage(component);
		}

		Component broadcast = PaperComponentUtils.DEFAULT_MINIMESSAGE.deserialize(String.format(DISCOVER, player.getName()))
				.append(newitem.displayName());
		PlayerUtils.broadcastMessage(broadcast);

		if (item.getAmount() == 1) {
			item.setItemMeta(newitem.getItemMeta());
		} else {
			item.setAmount(item.getAmount() - 1);
			inv.addItem(newitem);
		}

	}

	private float getLuck(Player player) {
		int luck = 0;
		@Nullable PotionEffect luckPotion = player.getPotionEffect(PotionEffectType.LUCK);
		if (luckPotion != null) {
			luck += luckPotion.getAmplifier();
		}
		@Nullable PotionEffect unluckPotion = player.getPotionEffect(PotionEffectType.UNLUCK);
		if (unluckPotion != null) {
			luck -= unluckPotion.getAmplifier();
		}
		return luck;
	}
	
	private Style randomStyle() {
		return Utils.getRandom(Style.values());
	}
	
	private Horse.Color randomColor() {
		return Utils.getRandom(Horse.Color.values());
	}
	
	private Float randomAttribute(Player player, float min, float max) {
		float luck = getLuck(player);
		while (true) {
			Double r = NumberUtils.getRandomDouble(min, max);
			double chance = 100*Math.exp( (r*r)/((max*max)/-(4f - luck) ) );
			if (NumberUtils.chance(chance)) {
				return r.floatValue();
			}
		}
	}

	private boolean isCustomHorse(final @NotNull Entity entity) {
		return entity.hasMetadata(ID);
	}



	@EventHandler(ignoreCancelled = true)
	public void onDismount(EntityDismountEvent event) {
		Entity mount = event.getDismounted();
		if (isCustomHorse(mount)) {
			if (event.getEntity() instanceof Player player) {
				player.setCooldown(MATERIAL, 20*10);
			}
			mount.getWorld().playSound(mount.getLocation(), Sound.ENTITY_HORSE_SADDLE, SoundCategory.AMBIENT, 0.5f, 1f);
			mount.remove();
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onInventoryOpen(InventoryOpenEvent event) {
		InventoryHolder holder = event.getInventory().getHolder();
		if (holder instanceof Horse horse) {
			event.setCancelled(isCustomHorse(horse));
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onHorseDeath(EntityDeathEvent event) {
		if (isCustomHorse(event.getEntity())) {
			event.setDroppedExp(0);
			event.getDrops().clear();
		}
	}
	
	@EventHandler(ignoreCancelled = true, priority =  EventPriority.LOW)
	public void onBreed(EntityBreedEvent event) {
		if (isCustomHorse(event.getEntity())) {
			event.setCancelled(true);
		}
	}
	// TODO desactivate dispenser place armor
	
}
