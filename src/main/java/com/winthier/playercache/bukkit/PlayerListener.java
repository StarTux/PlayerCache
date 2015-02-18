package com.winthier.playercache.bukkit;

import java.util.UUID;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import static org.bukkit.event.EventPriority.*;

class PlayerListener implements Listener {
    @EventHandler(priority = LOWEST)
    public void onPlayerLogin(PlayerLoginEvent event) {
        PlayerCachePlugin.getInstance().logPlayer(event.getPlayer());
    }
}
