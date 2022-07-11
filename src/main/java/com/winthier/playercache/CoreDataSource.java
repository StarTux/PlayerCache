package com.winthier.playercache;

import com.cavetale.core.command.RemotePlayer;
import com.cavetale.core.connect.Connect;
import com.cavetale.core.playercache.PlayerCache;
import com.cavetale.core.playercache.PlayerCacheDataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class CoreDataSource implements PlayerCacheDataSource {
    private static PlayerCache cacheForColumn(SQLPlayer row) {
        if (row == null) return null;
        return new PlayerCache(row.getUuid(), row.getName());
    }

    @Override
    public PlayerCache forUuid(UUID uuid) {
        return cacheForColumn(SQLPlayer.forUuid(uuid));
    }

    @Override
    public PlayerCache forName(String name) {
        return cacheForColumn(SQLPlayer.forName(name));
    }

    @Override
    public List<String> completeNames(String arg) {
        final String lower = arg.toLowerCase();
        List<String> list = new ArrayList<>();
        for (RemotePlayer player : Connect.get().getRemotePlayers()) {
            if (player.getName().toLowerCase().contains(lower)) {
                list.add(player.getName());
            }
        }
        if (!list.isEmpty()) return list;
        for (SQLPlayer player : SQLPlayer.allCached()) {
            if (player.getName().toLowerCase().contains(lower)) {
                list.add(player.getName());
            }
            if (list.size() >= 128) break;
        }
        return list;
    }
}
