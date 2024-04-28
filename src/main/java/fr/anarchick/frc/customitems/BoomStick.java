package fr.anarchick.frc.customitems;

import fr.anarchick.anapi.bukkit.BukkitUtils;
import fr.anarchick.anapi.bukkit.inventory.ItemBuilder;
import fr.anarchick.cani.api.inventory.CanISetItemEvent;
import fr.anarchick.cani.api.inventory.slot.InventorySlot;
import fr.anarchick.cani.api.inventory.slot.PlayerEquipmentSlot;
import fr.anarchick.cani.api.inventory.slot.Slot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.LinkedList;
import java.util.TreeMap;

public class BoomStick extends AbstractCustomItem {

	public static final String ID = "BoomStick";
	private static final int minutes = 3;

	public BoomStick() {
		super(ID);
	}

	@Override
	protected ItemStack getItem() {
		final String name = BukkitUtils.gradientString(
				"Bâton boom",
				new Color(230, 50, 200),
				new Color(30, 175, 240)
		);

		return new ItemBuilder(Material.PAPER)
				.setName(name)
				.addLore("<gold>Lance des TNT",
						"",
						"<gray>Consomme 1 poudre à canon"
				)
				.setCustomModelData(6)
				.build();
	}
	
	@Override
	public void onLeftClick(PlayerInteractEvent event) {
		final Player player = event.getPlayer();
		consume(player.getInventory(), new ItemStack(Material.DIAMOND_HELMET), 1);

		if (!event.isCancelled()
				&& player.getInventory().containsAtLeast(new ItemStack(Material.GUNPOWDER), 1)) {
			event.setCancelled(true);
			Vector velocity = player.getEyeLocation().getDirection();
			Location loc = player.getLocation();
			loc.setY(loc.getY() + player.getEyeHeight() / 2);
			TNTPrimed tnt = player.getWorld().spawn(loc, TNTPrimed.class, CreatureSpawnEvent.SpawnReason.CUSTOM, entity -> {
				entity.setFuseTicks(40);
				entity.setVelocity(velocity);
			});

		}

	}

	/**
	 * Get all the indexes of the specified item in the inventory
	 * different from {@link Inventory#all(ItemStack)} because it does not need the exact amount of items
	 */
	public static LinkedList<Slot> all(final @NotNull Inventory inv, final @NotNull ItemStack item) {
		// TreeMap to sort the indexes
		TreeMap<Integer, Slot> map = new TreeMap<>();
		int i = 0;

		for (ItemStack itemstack : inv.getStorageContents()) {
			if (itemstack != null && itemstack.isSimilar(item)) {
				map.put(i, new InventorySlot(inv, i));
			}
			i++;
		}

		if (inv instanceof PlayerInventory playerInv) {
			for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
				if (playerInv.getItem(equipmentSlot).isSimilar(item)) {
					map.put(-100 + equipmentSlot.ordinal(), new PlayerEquipmentSlot(playerInv, equipmentSlot));
				}
			}
		}

		return new LinkedList<>(map.values());
	}

	/**
	 * Consume the amount of the specified item from the inventory
	 */
	public static boolean consume(final @NotNull Inventory inv, final @NotNull ItemStack item, int amount) {
		LinkedList<Slot> consumeList = new LinkedList<>();
		int amountLeft = amount;

		for (Slot slot : all(inv, item)) {
			if (amount <= 0) {
				break;
			} else if (new CanISetItemEvent(null, inv, slot, item, true).ask().isAccepted()) {
				consumeList.add(slot);
				amount -= item.getAmount();
			}
		}

		if (amount > 0) {
			return false;
		}

		for (Slot slot : consumeList) {
			if (amountLeft <= 0) {
				break;
			} else {
				ItemStack itemStack = slot.getItem();
				if (itemStack.getAmount() >= amountLeft) {
					itemStack.setAmount(itemStack.getAmount() - amountLeft);
					slot.setItem(itemStack);
					break;
				} else {
					amountLeft -= itemStack.getAmount();
					slot.setItem(null);
				}
			}
		}

		return true;
	}
	
}
