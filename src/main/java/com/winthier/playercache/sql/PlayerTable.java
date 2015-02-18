package com.winthier.playercache.sql;

import com.avaje.ebean.validation.Length;
import com.avaje.ebean.validation.NotEmpty;
import com.avaje.ebean.validation.NotNull;
import java.util.Date;
import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;
import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

@Entity
@Table(
    name = "players",
    uniqueConstraints = @UniqueConstraint(columnNames={"uuid"})
    )
@Data
public class PlayerTable {
    @Id
    private Integer id;

    @NotNull
    private UUID uuid;

    @NotEmpty
    @Length(max=16)
    private String name;

    @NotNull
    private Date dateUpdated;

    @Version
    private Integer version;

    public PlayerTable() {}
    public PlayerTable(UUID uuid, String name, Date dateUpdated) {
        setUuid(uuid);
        setName(name);
        setDateUpdated(dateUpdated);
    }
}
