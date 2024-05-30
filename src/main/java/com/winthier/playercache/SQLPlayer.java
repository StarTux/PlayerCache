package com.winthier.playercache;

import com.winthier.sql.SQLRow;
import com.winthier.sql.SQLRow.Name;
import com.winthier.sql.SQLRow.NotNull;
import java.util.Date;
import java.util.UUID;
import lombok.Data;

@Data @NotNull @Name("players")
public final class SQLPlayer implements SQLRow {
    @Id private Integer id;
    @Unique private UUID uuid;
    @VarChar(16) @Keyed private String name;
    private Date dateUpdated;

    public SQLPlayer() { }

    public SQLPlayer(final UUID uuid, final String name) {
        this.uuid = uuid;
        this.name = name;
        this.dateUpdated = new Date();
    }
}
