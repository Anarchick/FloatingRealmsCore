package fr.anarchick.frc.utils;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;

public class Chars {
	
	public static char getEntity(EntityType type) {
		return switch (type) {
			case HORSE -> '\uE031';
			case DONKEY -> '\uE026';
			case RABBIT -> '\uE037';
			case PANDA -> '\uE035';
			case BEE -> '\uE021';
			case COW -> '\uE025';
			case MUSHROOM_COW -> '\uE033';
			case AXOLOTL -> '\uE020';
			case TURTLE -> '\uE041';
			case STRIDER -> '\uE039';
			case PIG -> '\uE036';
			case SHEEP -> '\uE038';
			case CAT -> '\uE023';
			case OCELOT -> '\uE034';
			case FOX -> '\uE027';
			case HOGLIN -> '\uE030';
			case CHICKEN -> '\uE024';
			case GOAT -> '\uE029';
			case WOLF -> '\uE042';
			case LLAMA -> '\uE032';
			case TRADER_LLAMA -> '\uE040';
			default -> ' ';
		};
	}
	
	public static char getMaterial(Material type) {
		return switch (type) {
			case WHEAT -> '\uE100';
			case CARROTS, CARROT -> '\uE101';
			case POTATOES, POTATO -> '\uE102';
			case BEETROOTS, BEETROOT -> '\uE103';
			case SUGAR_CANE -> '\uE104';
			case BAMBOO -> '\uE105';
			case SWEET_BERRY_BUSH, SWEET_BERRIES -> '\uE106';
			case RED_MUSHROOM -> '\uE107';
			case BROWN_MUSHROOM -> '\uE108';
			case CACTUS -> '\uE109';
			case MELON_STEM, MELON, MELON_SLICE -> '\uE10A';
			case PUMPKIN_STEM, PUMPKIN -> '\uE10B';
			case NETHER_PORTAL -> '\uE110';
			default -> ' ';
		};
	}
}
