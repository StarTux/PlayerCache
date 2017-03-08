package com.winthier.playercache.sql;

import com.avaje.ebean.EbeanServer;
import com.winthier.playercache.bukkit.PlayerCachePlugin;

final class DB {
    private DB() { }

    static EbeanServer get() {
        return PlayerCachePlugin.getInstance().getDatabase();
    }
}
