package fr.anarchick.frc.utils;

public class Characters extends fr.anarchick.anapi.bukkit.Characters {

	final public static Characters RUBIS = new Characters("E000");
	final public static Characters LOWAR = new Characters("E001");
	final public static Characters GUI_CALENDAR = new Characters("F000");
	final public static Characters GUI_BANK = new Characters("F001");

	final public static Characters XP = new Characters("E111");

	private Characters(String unicode) {
		super(unicode);
	}

	Characters(String unicodeStart, String unicodeEnd) {
		super(unicodeStart, unicodeEnd);
	}
	
}
