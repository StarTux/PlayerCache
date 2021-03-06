package com.winthier.playercache;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "players",
       indexes = {@Index(name = "id_name", columnList = "name")})
@Getter
@Setter
@NoArgsConstructor
public final class PlayerTable {
    private static Map<UUID, PlayerTable> uuidCache = new HashMap<>();
    private static Map<String, PlayerTable> nameCache = new HashMap<>();

    @Id
    private Integer id;

    @Column(nullable = false, unique = true)
    private UUID uuid;

    @Column(nullable = false, length = 16)
    private String name;

    @Column(nullable = false)
    private Date dateUpdated;

    PlayerTable(final UUID uuid, final String name) {
        this.uuid = uuid;
        this.name = name;
    }

    static PlayerTable forUuid(UUID uuid) {
        if (uuid == null) throw new NullPointerException("UUID cannot be null");
        if (uuidCache.containsKey(uuid)) return uuidCache.get(uuid);
        PlayerTable result = PlayerCachePlugin.getInstance().getSqldb().find(PlayerTable.class)
            .eq("uuid", uuid).orderByDescending("dateUpdated").findUnique();
        if (result == null) return null;
        uuidCache.put(uuid, result);
        return result;
    }

    static PlayerTable forName(String name) {
        if (name == null) throw new NullPointerException("Name cannot be null");
        name = name.toLowerCase();
        if (nameCache.containsKey(name)) return nameCache.get(name);
        PlayerTable result = PlayerCachePlugin.getInstance().getSqldb().find(PlayerTable.class)
            .eq("name", name).orderByDescending("dateUpdated").findUnique();
        if (result == null) return null;
        nameCache.put(name, result);
        return result;
    }

    void save() {
        setDateUpdated(new Date());
        PlayerCachePlugin.getInstance().getSqldb().save(this);
        nameCache.put(getName().toLowerCase(), this);
        uuidCache.put(getUuid(), this);
    }

    static void fillCache() {
        for (PlayerTable row: PlayerCachePlugin.getInstance().getSqldb().find(PlayerTable.class)
                 .orderByAscending("dateUpdated").findList()) {
            uuidCache.put(row.uuid, row);
            nameCache.put(row.name, row);
        }
    }

    static void clearCache() {
        uuidCache.clear();
        nameCache.clear();
    }

    static List<PlayerTable> findAll() {
        return PlayerCachePlugin.getInstance().getSqldb().find(PlayerTable.class).findList();
    }

    protected static Collection<PlayerTable> allCached() {
        return uuidCache.values();
    }
}
