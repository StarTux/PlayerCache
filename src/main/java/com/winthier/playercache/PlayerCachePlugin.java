package com.winthier.playercache;

import com.cavetale.core.connect.Connect;
import com.cavetale.core.event.connect.ConnectMessageEvent;
import com.cavetale.core.util.Json;
import com.winthier.sql.SQLDatabase;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class PlayerCachePlugin extends JavaPlugin implements Listener {
    private static final String CONNECT_LOG = "playercache:log";
    private static PlayerCachePlugin instance;
    private SQLDatabase database = new SQLDatabase(this);
    private final CoreDataSource coreDataSource = new CoreDataSource();
    private final PlayerCacheCommand playerCacheCommand = new PlayerCacheCommand(this);

    @Override
    public void onLoad() {
        instance = this;
        coreDataSource.register();
    }

    @Override
    public void onEnable() {
        database.registerTable(SQLPlayer.class);
        if (!database.createAllTables()) {
            throw new IllegalStateException("Database setup failed");
        }
        Cache.fill();
        Bukkit.getPluginManager().registerEvents(this, this);
        playerCacheCommand.enable();
    }

    @Override
    public void onDisable() {
        database.waitForAsyncTask();
        database.close();
        coreDataSource.unregister();
        Cache.clear();
        instance = null;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLogin(PlayerLoginEvent event) {
        PlayerCache player = PlayerCache.of(event.getPlayer());
        Cache.log(player); // calls broadcast
    }

    protected static void broadcast(PlayerCache player) {
        Connect.get().broadcastMessage(CONNECT_LOG, Json.serialize(player));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onConnectMessage(ConnectMessageEvent event) {
        if (CONNECT_LOG.equals(event.getChannel())) {
            PlayerCache player = Json.deserialize(event.getPayload(), PlayerCache.class);
            Cache.store(player);
        }
    }

    public static SQLDatabase database() {
        return instance.database;
    }

    public static void info(String msg) {
        instance.getLogger().info(msg);
    }
}
