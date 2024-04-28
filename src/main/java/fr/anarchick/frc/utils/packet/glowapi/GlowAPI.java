package fr.anarchick.frc.utils.packet.glowapi;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import fr.anarchick.anapi.bukkit.Scheduling;
import fr.anarchick.frc.FloatingRealmsCore;
import fr.anarchick.frc.utils.packet.Packet;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.*;

public class GlowAPI {

    static {
        // When the player change his metadata, like when he toggle sneak, he lost the glow. This code patch this issue
        Packet.MANAGER.addPacketListener(new PacketAdapter(FloatingRealmsCore.getInstance(), ListenerPriority.NORMAL, PacketType.Play.Server.ENTITY_METADATA) {
            @Override
            public void onPacketSending(PacketEvent event) {
                Player player = event.getPlayer();
                PacketContainer packet = event.getPacket();
                Integer id = packet.getIntegers().read(0);
                Set<Integer> ids = MANAGER.get(player);
                if (ids != null && ids.contains(id)) { // The entity is glowing for the receiver
                    final List<WrappedWatchableObject> packetContents = packet.getWatchableCollectionModifier().read(0);
                    final WrappedWatchableObject watchableObject = packetContents.stream().filter(watchableObj -> watchableObj.getIndex() == 0).findFirst().orElse(null);
                    if (watchableObject != null && watchableObject.getValue() instanceof Byte) {
                        Byte byteData = getDataFromEntityId(player.getWorld(), id);
                        Byte value = (byte) watchableObject.getValue();
                        watchableObject.setValue((byte) (value | byteData | 0x40), false);
                    }
                }
            }
        });
    }

    private static final Map<Player, Set<Integer>> MANAGER = new HashMap<>();
    private static final byte BYTE_INVISIBLE = 64;

    public static void setglowing(Entity entity, Player watcher, GlowTeam team, int duration) {
        String identifier;
        if (entity instanceof Player) {
            identifier = entity.getName();
        } else {
            identifier = entity.getUniqueId().toString();
        }
        setGlowing(entity.getEntityId(), identifier, watcher, team, duration);
    }

    public static void setGlowing(int entityId, String identifier, Player watcher, GlowTeam team, int duration) {
        final Set<Integer> idManager = MANAGER.getOrDefault(watcher, new HashSet<>());
        MANAGER.put(watcher, idManager);
        team.joinPacket(identifier).sendPacket(watcher);
        byte byteData = getDataFromEntityId(watcher.getWorld(), entityId);
        byteData = (byte) (byteData | BYTE_INVISIBLE);
        idManager.add(entityId);
        Packet.metadataPacket(entityId, 0, byteData).sendPacket(watcher);

        if (duration < 0) return;

        Scheduling.syncDelay(duration, () -> {
            byte byteData2 = getDataFromEntityId(watcher.getWorld(), entityId);
            idManager.remove(identifier);
            Packet.metadataPacket(entityId, 0, byteData2).sendPacket(watcher);
        });

    }

    private static byte getDataFromEntityId(@Nonnull World world, int id) {
        Entity entity = Packet.MANAGER.getEntityFromID(world, id);
        return (entity != null) ? WrappedDataWatcher.getEntityWatcher(entity).getByte(0) : 0;
    }



}
