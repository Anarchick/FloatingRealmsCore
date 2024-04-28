package fr.anarchick.frc.utils.packet.glowapi;

import fr.anarchick.frc.utils.packet.Packet;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;
import org.bukkit.scoreboard.Team.Option;
import org.bukkit.scoreboard.Team.OptionStatus;

public enum GlowTeam {

    DARK_PURPLE(NamedTextColor.DARK_PURPLE),
    GOLD(NamedTextColor.GOLD),
    GREEN(NamedTextColor.GREEN),
    RED(NamedTextColor.RED),
    NONE(NamedTextColor.WHITE);

    private ChatColor color;
    private Team team;
    private GlowTeam(NamedTextColor color) {
        try {
            this.color = ChatColor.valueOf(color.toString().toUpperCase());
            final ScoreboardManager manager = Bukkit.getScoreboardManager();
            final Scoreboard board = manager.getMainScoreboard();
            for (Team team : board.getTeams()) {
                if (team.getName().equals(getTeamName())) {
                    team.unregister();
                }
            }
            this.team = board.registerNewTeam(getTeamName());
            team.color(color);
            team.setCanSeeFriendlyInvisibles(true);
            team.setOption(Option.NAME_TAG_VISIBILITY, OptionStatus.NEVER);
            team.setOption(Option.COLLISION_RULE, OptionStatus.ALWAYS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ChatColor getColor() {
        return this.color;
    }

    public String getTeamName() {
        String name = String.format("%s", name());
        if (name.length() > 16) {
            name = name.substring(0, 16);
        }
        return name;
    }

    public void join(Entity entity) {
        team.addEntity(entity);
    }

    public Packet joinPacket(Entity... entities) {
        return Packet.joinTeam(getTeamName(), entities);
    }

    public Packet joinPacket(String... identifiers) {
        return Packet.joinTeam(getTeamName(), identifiers);
    }


}