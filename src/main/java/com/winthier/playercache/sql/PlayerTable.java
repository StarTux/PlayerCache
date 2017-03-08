package com.winthier.playercache.sql;

import com.avaje.ebean.validation.Length;
import com.avaje.ebean.validation.NotEmpty;
import com.avaje.ebean.validation.NotNull;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PersistenceException;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "players",
    uniqueConstraints = @UniqueConstraint(columnNames = {"uuid"})
    )
@Data
@NoArgsConstructor
public class PlayerTable {
    private static Map<UUID, PlayerTable> uuidCache = new HashMap<>();
    private static Map<String, PlayerTable> nameCache = new HashMap<>();

    @Id
    private Integer id;

    @NotNull
    private UUID uuid;

    @NotEmpty
    @Length(max = 16)
    private String name;

    @NotNull
    private Date dateUpdated;

    @Version
    private Integer version;

    public PlayerTable(UUID uuid, String name) {
        setUuid(uuid);
        setName(name);
    }

    public static PlayerTable forUuid(UUID uuid) {
        if (uuid == null) throw new NullPointerException("UUID cannot be null");
        if (uuidCache.containsKey(uuid)) return uuidCache.get(uuid);
        List<PlayerTable> list = DB.get().find(PlayerTable.class).where().eq("uuid", uuid).orderBy("date_updated desc").findList();
        if (list.isEmpty()) return null;
        PlayerTable result = list.get(0);
        uuidCache.put(uuid, result);
        return result;
    }

    public static PlayerTable forName(String name) {
        if (name == null) throw new NullPointerException("Name cannot be null");
        name = name.toLowerCase();
        if (nameCache.containsKey(name)) return nameCache.get(name);
        List<PlayerTable> list = DB.get().find(PlayerTable.class).where().eq("name", name).orderBy("date_updated desc").findList();
        if (list.isEmpty()) return null;
        final PlayerTable result = list.get(0);
        nameCache.put(name, result);
        return result;
    }

    public static void save(PlayerTable player) {
        player.setDateUpdated(new Date());
        try {
            DB.get().save(player);
        } catch (PersistenceException pe) {
            pe.printStackTrace();
        }
        nameCache.remove(player.getName());
        uuidCache.remove(player.getUuid());
    }
}
