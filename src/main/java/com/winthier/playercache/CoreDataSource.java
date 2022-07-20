package com.winthier.playercache;

import com.cavetale.core.command.RemotePlayer;
import com.cavetale.core.connect.Connect;
import com.cavetale.core.playercache.PlayerCache;
import com.cavetale.core.playercache.PlayerCacheDataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class CoreDataSource implements PlayerCacheDataSource {
    @Override
    public PlayerCache forUuid(UUID uuid) {
        com.winthier.playercache.PlayerCache player = Cache.forUuid(uuid);
        return player != null
            ? new PlayerCache(player.uuid, player.name)
            : null;
    }

    @Override
    public PlayerCache forName(String name) {
        com.winthier.playercache.PlayerCache player = Cache.forName(name);
        return player != null
            ? new PlayerCache(player.uuid, player.name)
            : null;
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
        for (String name : Cache.names()) {
            if (name.toLowerCase().contains(lower)) {
                list.add(name);
            }
            if (list.size() >= 128) break;
        }
        return list;
    }
}
