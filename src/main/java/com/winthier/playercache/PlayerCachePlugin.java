package com.winthier.playercache;

import com.winthier.sql.SQLDatabase;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class PlayerCachePlugin extends JavaPlugin implements Listener {
    @Getter private static PlayerCachePlugin instance;
    private SQLDatabase sqldb;
    private final CoreDataSource coreDataSource = new CoreDataSource();

    @Override
    public void onLoad() {
        instance = this;
        coreDataSource.register();
    }

    @Override
    public void onEnable() {
        setupDatabase();
        getServer().getPluginManager().registerEvents(this, this);
        for (Player player: getServer().getOnlinePlayers()) {
            logPlayer(player);
        }
    }

    private void setupDatabase() {
        sqldb = new SQLDatabase(this);
        sqldb.registerTable(SQLPlayer.class);
        if (!sqldb.createAllTables()) {
            getLogger().warning("Failed to create tables. Abort");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        SQLPlayer.clearCache();
        SQLPlayer.fillCache();
    }

    @Override
    public void onDisable() {
        SQLPlayer.clearCache();
        sqldb = null;
        instance = null;
        coreDataSource.unregister();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String cmd = args.length > 0 ? args[0].toLowerCase() : null;
        if (cmd == null) {
            return false;
        } else if (args.length == 2 && "uuid".equals(cmd)) {
            String nameArg = args[1];
            PlayerCache cache = PlayerCache.forName(nameArg);
            if (cache == null) {
                sender.sendMessage("Player not found: " + nameArg);
            } else {
                sender.sendMessage(String.format("Player %s has UUID %s",
                                                 cache.getName(), cache.getUuid()));
            }
        } else if (args.length == 2 && "name".equals(cmd)) {
            String uuidArg = args[1];
            UUID uuid = null;
            try {
                uuid = UUID.fromString(uuidArg);
            } catch (IllegalArgumentException iae) {
                sender.sendMessage("Invalid uuid: " + uuidArg);
            }
            if (uuid != null) {
                PlayerCache cache = PlayerCache.forUuid(uuid);
                if (cache == null) {
                    sender.sendMessage(String.format("Player with UUID %s not found", uuid));
                } else {
                    sender.sendMessage(String.format("Player %s has UUID %s",
                                                     cache.getName(), cache.getUuid()));
                }
            }
        } else if (args.length == 2 && "match".equals(cmd)) {
            String matchArg = args[1];
            List<SQLPlayer> list = sqldb.find(SQLPlayer.class).like("name", matchArg)
                .orderByAscending("name").findList();
            sender.sendMessage(String.format("Found %d player names matching %s.",
                                             list.size(), matchArg));
            for (SQLPlayer row : list) {
                sender.sendMessage("  " + row.getName() + " - " + row.getUuid());
            }
        } else if (args.length == 1 && "reload".equals(cmd)) {
            setupDatabase();
            sender.sendMessage("Database reloaded");
        } else {
            return false;
        }
        return true;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLogin(PlayerLoginEvent event) {
        logPlayer(event.getPlayer());
    }

    private void logPlayer(Player player) {
        SQLPlayer row = SQLPlayer.forUuid(player.getUniqueId());
        if (row == null) {
            getLogger().info(String.format("Saving player %s with UUID %s",
                                           player.getName(), player.getUniqueId()));
            row = new SQLPlayer(player.getUniqueId(), player.getName());
            row.save();
        } else {
            if (!row.getName().equals(player.getName())) {
                getLogger().info(String.format("Player %s with UUID %s changed their name to %s",
                                               row.getName(), row.getUuid(), player.getName()));
                row.setName(player.getName());
                row.save();
            }
        }
    }
}
