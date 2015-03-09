package com.winthier.playercache.sql;

import com.avaje.ebean.EbeanServer;
import com.winthier.playercache.bukkit.PlayerCachePlugin;

class DB {
    static EbeanServer get() {
        return PlayerCachePlugin.getInstance().getDatabase();
    }
}
