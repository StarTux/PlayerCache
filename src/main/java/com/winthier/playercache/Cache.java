package com.winthier.playercache;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import static com.winthier.playercache.PlayerCachePlugin.broadcast;
import static com.winthier.playercache.PlayerCachePlugin.database;
import static com.winthier.playercache.PlayerCachePlugin.info;

public final class Cache {
    private static final Cache INSTANCE = new Cache();
    private final Map<String, PlayerCache> nameMap = new HashMap<>();
    private final Map<UUID, PlayerCache> uuidMap = new HashMap<>();
    private final Set<String> names = new HashSet<>();

    protected static void fill() {
        INSTANCE.names.addAll(database().find(SQLPlayer.class).findValues("name", String.class));
        for (Player player : Bukkit.getOnlinePlayers()) {
            log(PlayerCache.of(player));
        }
    }

    /**
     * Store the instance in the cache.  This will never trigger a
     * database write!
     */
    protected static void store(PlayerCache player) {
        INSTANCE.nameMap.put(player.name, player);
        INSTANCE.uuidMap.put(player.uuid, player);
        INSTANCE.names.add(player.name);
    }

    protected static void clear() {
        INSTANCE.nameMap.clear();
        INSTANCE.uuidMap.clear();
        INSTANCE.names.clear();
    }

    /**
     * Ensure that the given log exists in the cache and the database.
     * Update both if needed, and broadcast the update.
     */
    protected static void log(PlayerCache log) {
        PlayerCache old = forUuid(log.uuid);
        if (old != null && old.name.equals(log.name)) return;
        store(log);
        SQLPlayer row = new SQLPlayer(log.uuid, log.name);
        if (old == null) {
            database().insertAsync(row, r -> {
                    info("New player: " + row + " result=" + r);
                    broadcast(log);
                });
        } else {
            database().saveAsync(row, Set.of("name", "dateUpdated"), r -> {
                    info("Name change: " + row + " result=" + r);
                    broadcast(log);
                });
        }
    }

    public static PlayerCache forUuid(UUID uuid) {
        if (INSTANCE.uuidMap.containsKey(uuid)) {
            return INSTANCE.uuidMap.get(uuid);
        }
        SQLPlayer row = database().find(SQLPlayer.class).eq("uuid", uuid).findUnique();
        if (row == null) {
            INSTANCE.uuidMap.put(uuid, null);
            return null;
        }
        PlayerCache result = new PlayerCache(row.getUuid(), row.getName());
        store(result);
        return result;
    }

    public static PlayerCache forName(String name) {
        if (INSTANCE.nameMap.containsKey(name)) {
            return INSTANCE.nameMap.get(name);
        }
        SQLPlayer row = database().find(SQLPlayer.class).eq("name", name)
            .orderByDescending("dateUpdated")
            .findUnique();
        if (row == null) {
            INSTANCE.nameMap.put(name, null);
            return null;
        }
        PlayerCache result = new PlayerCache(row.getUuid(), row.getName());
        store(result);
        return result;
    }

    public static String nameForUuid(UUID uuid) {
        PlayerCache player = forUuid(uuid);
        return player != null ? player.name : null;
    }

    public static UUID uuidForName(String name) {
        PlayerCache player = forName(name);
        return player != null ? player.uuid : null;
    }

    public static Set<String> names() {
        return INSTANCE.names;
    }

    public static int size() {
        return INSTANCE.uuidMap.size();
    }
}
