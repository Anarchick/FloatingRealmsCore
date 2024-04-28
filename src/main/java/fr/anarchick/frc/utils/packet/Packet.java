package fr.anarchick.frc.utils.packet;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.InternalStructure;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import fr.anarchick.frc.utils.packet.glowapi.GlowTeam;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.*;

public class Packet {

    public static final ProtocolManager MANAGER = ProtocolLibrary.getProtocolManager();

    private final PacketContainer packet;
    private Packet(PacketContainer packet) {
        this.packet = packet;
    }

    public void sendPacket(Player... players) {
        try {
            for (Player player : players) {
                MANAGER.sendServerPacket(player, this.packet);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // type  https://wiki.vg/Entity_metadata#Mobs
    public static Packet createEntityPacket(Location loc, int id, UUID uuid, EntityType type) {
        PacketContainer packet = MANAGER.createPacket(PacketType.Play.Server.SPAWN_ENTITY, true);
        StructureModifier<Object> modifier = packet.getModifier();
        modifier.writeSafely(0, id);
        modifier.writeSafely(1, uuid);
        //modifier.writeSafely(2, type);
        packet.getEntityTypeModifier().writeSafely(0, type);
        modifier.writeSafely(3, loc.getX());
        modifier.writeSafely(4, loc.getY());
        modifier.writeSafely(5, loc.getZ());
        return new Packet(packet);
    }

    public static Packet destroyPacket(@Nonnull Integer... ids) {
        PacketContainer packet = MANAGER.createPacket(PacketType.Play.Server.ENTITY_DESTROY, true);
        packet.getIntLists().writeSafely(0, List.of(ids));
        return new Packet(packet);
    }

    public static Packet metadataPacket(int id, int index, Object value) {
        PacketContainer packet = MANAGER.createPacket(PacketType.Play.Server.ENTITY_METADATA, true);
        packet.getIntegers().writeSafely(0, id);
        WrappedDataWatcher watcher = new WrappedDataWatcher();
        watcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(index, WrappedDataWatcher.Registry.get(value.getClass())), value);
        packet.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects());
        return new Packet(packet);
    }

    public static Packet createTeam(String teamName, GlowTeam color, Entity... entities) {
        PacketContainer packet = MANAGER.createPacket(PacketType.Play.Server.SCOREBOARD_TEAM, true);
        packet.getStrings().writeSafely(0, teamName);
        packet.getIntegers().writeSafely(0, 0);
        List<String> identifiers = new ArrayList<>();
        for (Entity entity : entities) {
            if (entity instanceof Player) {
                identifiers.add(entity.getName());
            } else {
                identifiers.add(entity.getUniqueId().toString());
            }
        }
        packet.getModifier().write(2, identifiers);
        Optional<InternalStructure> optStruct = packet.getOptionalStructures().read(0); // Get field 'k', which is an optional internal structure
        if (optStruct.isPresent()) { // Make sure the structure exists (it always does)
            InternalStructure struct = optStruct.get();
            struct.getChatComponents().write(0, WrappedChatComponent.fromText("")); // The team display name is needed but can be empty
            struct.getIntegers().write(0, 2); // Bit mask. 0x01: Allow friendly fire, 0x02: can see invisible entities on same team
            struct.getEnumModifier(ChatColor.class, MinecraftReflection.getMinecraftClass("EnumChatFormat")).write(0, color.getColor()); // This new team will be the desired color
            packet.getOptionalStructures().write(0, Optional.of(struct));
        }
        return new Packet(packet);
    }

    public static Packet removeTeam(String teamName) {
        PacketContainer packet = MANAGER.createPacket(PacketType.Play.Server.SCOREBOARD_TEAM, true);
        packet.getStrings().writeSafely(0, teamName);
        packet.getIntegers().writeSafely(0, 1);
        return new Packet(packet);
    }

    public static Packet joinTeam(String teamName, Entity... entities) {
        List<String> identifiers = new ArrayList<>();
        for (Entity entity : entities) {
            if (entity instanceof Player) {
                identifiers.add(entity.getName());
            } else {
                identifiers.add(entity.getUniqueId().toString());
            }
        }
        return joinTeam(teamName, identifiers);
    }

    public static Packet joinTeam(String teamName, String... identifiers) {
        return joinTeam(teamName, Arrays.asList(identifiers));
    }

    public static Packet joinTeam(String teamName, List<String> identifiers) {
        PacketContainer packet = MANAGER.createPacket(PacketType.Play.Server.SCOREBOARD_TEAM, true);
        packet.getStrings().writeSafely(0, teamName);
        packet.getIntegers().writeSafely(0, 3);
        packet.getModifier().write(2, identifiers);
        return new Packet(packet);
    }

}
