package com.winthier.playercache.bukkit;

import com.winthier.playercache.PlayerCache;
import com.winthier.playercache.sql.PlayerTable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.persistence.PersistenceException;
import lombok.val;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerCachePlugin extends JavaPlugin {
    private static PlayerCachePlugin instance;
    private final PlayerListener playerListener = new PlayerListener();
    
    public static PlayerCachePlugin getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        this.instance = this;
        try {
            for (Class<?> clazz : getDatabaseClasses()) {
                getDatabase().find(clazz).findRowCount();
            }
        } catch (PersistenceException ex) {
            System.out.println("Installing database for " + getDescription().getName() + " due to first time usage");
            installDDL();
        }
        getServer().getPluginManager().registerEvents(playerListener, this);
    }

    @Override
    public void onDisable() {
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String args[]) {
        if (args.length == 0) {
            return false;
        } else if (args.length == 2 && "UUID".equalsIgnoreCase(args[0])) {
            String nameArg = args[1];
            PlayerCache cache = PlayerCache.forName(nameArg);
            if (cache == null) {
                sender.sendMessage("Player not found: " + nameArg);
            } else {
                sender.sendMessage(String.format("Player %s has UUID %s", cache.getName(), cache.getUuid()));
            }
        } else if (args.length == 2 && "Name".equalsIgnoreCase(args[0])) {
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
                    sender.sendMessage(String.format("Player %s has UUID %s", cache.getName(), cache.getUuid()));
                }
            }
        } else if (args.length == 2 && "Legacy".equalsIgnoreCase(args[0])) {
            String nameArg = args[1];
            UUID uuid = PlayerCache.uuidForLegacyName(nameArg);
            if (uuid == null) {
                sender.sendMessage("Player not found: " + nameArg);
            } else {
                sender.sendMessage(String.format("Legacy player %s had UUID %s", nameArg, uuid));
            }
        } else {
            return false;
        }
        return true;
    }

    @Override
    public List<Class<?>> getDatabaseClasses() {
        List<Class<?>> result = new ArrayList<>();
        result.add(PlayerTable.class);
        return result;
    }

    public void logPlayer(Player player) {
        PlayerTable row = PlayerTable.forUuid(player.getUniqueId());
        if (row == null) {
            getLogger().info(String.format("Saving player %s with UUID %s", player.getName(), player.getUniqueId()));
            row = new PlayerTable(player.getUniqueId(), player.getName());
            PlayerTable.save(row);
        } else {
            if (!row.getName().equals(player.getName())) {
                getLogger().info(String.format("Player %s with UUID %s changed their name to %s", row.getName(), row.getUuid(), player.getName()));
                row.setName(player.getName());
                PlayerTable.save(row);
            }
        }
    }
}
