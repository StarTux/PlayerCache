package com.winthier.playercache;

import java.util.UUID;
import lombok.Value;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@Value
public final class PlayerCache {
    private final UUID uuid;
    private final String name;

    private static PlayerCache cacheForColumn(PlayerTable table) {
        if (table == null) return null;
        return new PlayerCache(table.getUuid(), table.getName());
    }

    public static PlayerCache forUuid(UUID uuid) {
        return cacheForColumn(PlayerTable.forUuid(uuid));
    }

    public static PlayerCache forName(String name) {
        return cacheForColumn(PlayerTable.forName(name));
    }

    public static String nameForUuid(UUID uuid) {
        final Player player = Bukkit.getServer().getPlayer(uuid);
        if (player != null) return player.getName();
        PlayerTable row = PlayerTable.forUuid(uuid);
        if (row == null) return null;
        return row.getName();
    }

    public static UUID uuidForName(String name) {
        PlayerTable row = PlayerTable.forName(name);
        if (row == null) return null;
        return row.getUuid();
    }
}
