package com.winthier.playercache;

import com.avaje.ebean.SqlRow;
import com.winthier.playercache.bukkit.PlayerCachePlugin;
import com.winthier.playercache.sql.PlayerTable;
import java.util.List;
import java.util.UUID;
import lombok.Value;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@Value
public class PlayerCache {
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

    public static UUID uuidForLegacyName(String name) {
        final String sql = "SELECT `uuid` FROM `legacy` WHERE name = :name";
        final List<SqlRow> list = PlayerCachePlugin.getInstance().getDatabase().createSqlQuery(sql).setParameter("name", name).findList();
        if (list.isEmpty()) return null;
        if (list.size() > 1) PlayerCachePlugin.getInstance().getLogger().warning("Player " + name + " has more than one legacy UUID: " + list);
        String uuidString = list.get(0).getString("uuid");
        try {
            return UUID.fromString(uuidString);
        } catch (IllegalArgumentException iae) {
            iae.printStackTrace();
        }
        return null;
    }    
}
