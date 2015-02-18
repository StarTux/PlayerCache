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
        return new PlayerCache(table.getUuid(), table.getName());
    }

    private static List<PlayerTable> columnsForUuid(UUID uuid) {
        if (uuid == null) throw new NullPointerException("UUID can't be null");
        return PlayerCachePlugin.getInstance().getDatabase().find(PlayerTable.class).where().eq("uuid", uuid).findList();
    }

    private static List<PlayerTable> columnsForName(String name) {
        if (name == null) throw new NullPointerException("Name can't be null");
        return PlayerCachePlugin.getInstance().getDatabase().find(PlayerTable.class).where().eq("name", name).orderBy("date_updated desc").findList();
    }

    public static PlayerCache forUuid(UUID uuid) {
        val result = columnsForUuid(uuid);
        if (result.isEmpty()) return null;
        return cacheForColumn(result.get(0));
    }

    public static PlayerCache forName(String name) {
        val result = columnsForName(name);
        if (result.isEmpty()) return null;
        return cacheForColumn(result.get(0));
    }

    public static String nameForUuid(UUID uuid) {
        final Player player = Bukkit.getServer().getPlayer(uuid);
        if (player != null) return player.getName();
        val list = columnsForUuid(uuid);
        if (list.isEmpty()) return null;
        return list.get(0).getName();
    }

    public static UUID uuidForName(String name) {
        val list = columnsForName(name);
        if (list.isEmpty()) return null;
        return list.get(0).getUuid();
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
