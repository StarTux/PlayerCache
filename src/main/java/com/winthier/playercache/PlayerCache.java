package com.winthier.playercache;

import com.cavetale.core.command.CommandArgCompleter;
import com.cavetale.core.command.CommandContext;
import com.cavetale.core.command.CommandNode;
import com.cavetale.core.command.CommandWarn;
import com.cavetale.core.connect.Connect;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.Value;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@Value
public final class PlayerCache {
    public final UUID uuid;
    public final String name;

    private static PlayerCache cacheForColumn(SQLPlayer row) {
        if (row == null) return null;
        return new PlayerCache(row.getUuid(), row.getName());
    }

    public static PlayerCache forUuid(UUID uuid) {
        return cacheForColumn(SQLPlayer.forUuid(uuid));
    }

    public static PlayerCache forName(String name) {
        return cacheForColumn(SQLPlayer.forName(name));
    }

    /**
     * Find player for the given argument, which could be a name or
     * uuid.
     */
    public static PlayerCache forArg(String arg) {
        try {
            return forUuid(UUID.fromString(arg));
        } catch (IllegalArgumentException iae) { }
        return forName(arg);
    }

    public static String nameForUuid(UUID uuid) {
        if (PlayerCachePlugin.getInstance() == null) return null;
        final Player player = Bukkit.getServer().getPlayer(uuid);
        if (player != null) return player.getName();
        SQLPlayer row = SQLPlayer.forUuid(uuid);
        if (row == null) return null;
        return row.getName();
    }

    public static UUID uuidForName(String name) {
        if (PlayerCachePlugin.getInstance() == null) return null;
        SQLPlayer row = SQLPlayer.forName(name);
        if (row == null) return null;
        return row.getUuid();
    }

    public static List<PlayerCache> findAll() {
        return SQLPlayer.findAll().stream().map(PlayerCache::cacheForColumn)
            .collect(Collectors.toList());
    }

    public static List<PlayerCache> allCached() {
        return SQLPlayer.allCached().stream().map(PlayerCache::cacheForColumn)
            .collect(Collectors.toList());
    }

    public static final CommandArgCompleter NAME_COMPLETER = new CommandArgCompleter() {
            @Override
            public List<String> complete(CommandContext context, CommandNode node, String arg) {
                final String lower = arg.toLowerCase();
                List<String> list = Connect.get().getOnlinePlayers().stream()
                    .map(PlayerCache::nameForUuid)
                    .filter(Objects::nonNull)
                    .filter(theName -> theName.toLowerCase().contains(lower))
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .collect(Collectors.toList());
                if (!list.isEmpty()) return list;
                return SQLPlayer.allCached().stream()
                    .sorted((a, b) -> b.getDateUpdated().compareTo(a.getDateUpdated()))
                    .map(SQLPlayer::getName)
                    .filter(theName -> theName.toLowerCase().contains(lower))
                    .limit(128)
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .collect(Collectors.toList());
            }
        };

    public static PlayerCache require(String in) {
        PlayerCache result = forArg(in);
        if (result == null) throw new CommandWarn("Player not found: " + in);
        return result;
    }

    public static PlayerCache requireForName(String in) {
        PlayerCache result = forName(in);
        if (result == null) throw new CommandWarn("Player not found: " + in);
        return result;
    }

    public static PlayerCache requireForUuid(UUID in) {
        PlayerCache result = forUuid(in);
        if (result == null) throw new CommandWarn("Player not found: " + in);
        return result;
    }
}
