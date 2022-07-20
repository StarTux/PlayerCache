package com.winthier.playercache;

import com.cavetale.core.command.AbstractCommand;
import com.cavetale.core.command.CommandWarn;
import java.util.List;
import java.util.UUID;
import org.bukkit.command.CommandSender;
import static com.winthier.playercache.PlayerCachePlugin.database;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;

public final class PlayerCacheCommand extends AbstractCommand<PlayerCachePlugin> {
    protected PlayerCacheCommand(final PlayerCachePlugin plugin) {
        super(plugin, "playercache");
    }

    @Override
    protected void onEnable() {
        rootNode.addChild("uuid").arguments("<name>")
            .description("UUID for name")
            .senderCaller(this::uuid);
        rootNode.addChild("name").arguments("<uuid>")
            .description("Name for UUID")
            .senderCaller(this::name);
        rootNode.addChild("match").arguments("<pattern>")
            .description("Find matching players")
            .senderCaller(this::match);
        rootNode.addChild("reload").denyTabCompletion()
            .description("Flush all caches")
            .senderCaller(this::reload);
        rootNode.addChild("debug").denyTabCompletion()
            .description("Show debug info")
            .senderCaller(this::debug);
    }

    private boolean uuid(CommandSender sender, String[] args) {
        if (args.length != 1) return false;
        String nameArg = args[0];
        PlayerCache cache = PlayerCache.forName(nameArg);
        if (cache == null) {
            throw new CommandWarn("Player not found: " + nameArg);
        }
        sender.sendMessage(text("Player " + cache.name + " has UUID " + cache.uuid, AQUA)
                           .insertion(cache.uuid.toString()));
        return true;
    }

    private boolean name(CommandSender sender, String[] args) {
        if (args.length != 1) return false;
        String uuidArg = args[0];
        UUID uuid = null;
        try {
            uuid = UUID.fromString(uuidArg);
        } catch (IllegalArgumentException iae) {
            throw new CommandWarn("Invalid uuid: " + uuidArg);
        }
        PlayerCache cache = PlayerCache.forUuid(uuid);
        if (cache == null) {
            throw new CommandWarn("Player with UUID " + uuid + "  not found");
        }
        sender.sendMessage(text("Player " + cache.name + " has UUID " + cache.uuid, AQUA)
                           .insertion(cache.name));
        return true;
    }

    private boolean match(CommandSender sender, String[] args) {
        if (args.length != 1) return false;
        String matchArg = args[0];
        List<SQLPlayer> list = database().find(SQLPlayer.class).like("name", matchArg)
            .orderByAscending("name").findList();
        if (list.isEmpty()) {
            throw new CommandWarn("Not found: " + matchArg);
        }
        sender.sendMessage(text("Found " + list.size() + " player names matching " + matchArg, AQUA));
        for (SQLPlayer row : list) {
            sender.sendMessage(text("  " + row.getName() + " - " + row.getUuid(), YELLOW)
                               .insertion(row.getUuid().toString()));
        }
        return true;
    }

    private void reload(CommandSender sender) {
        Cache.clear();
        Cache.fill();
        sender.sendMessage(text("Database reloaded", AQUA));
    }

    private void debug(CommandSender sender) {
        sender.sendMessage("Debug"
                           + " names:" + Cache.names().size()
                           + " players:" + Cache.size());
    }
}
