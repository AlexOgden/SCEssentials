package com.alexogden.module.server;

import com.alexogden.core.SCEssentials;
import com.alexogden.core.logging.ServerLog;
import com.alexogden.command.CommandHandler;
import com.alexogden.module.ServerModule;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.StringUtil;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

public class CloseServer implements ServerModule {
	private final CommandHandler commandHandler;
	private final Listener eventHandler;
	Plugin plugin = SCEssentials.getInstance();
	private boolean closed = false;

	private String openMOTD;
	private final String closedMOTD;

	public CloseServer() {
		this.commandHandler = createCommandHandler();
		this.eventHandler = createEventHandler();

		this.openMOTD = getDefaultMOTD();
		this.closedMOTD = "<red>SERVER CLOSED</red>";
	}

	@Override
	public void init() {
		plugin.getServer().getPluginManager().registerEvents(eventHandler, plugin);
		commandHandler.register();
	}

	private CommandHandler createCommandHandler() {
		return CommandHandler.builder("server")
				.executor((commandSender, command, subcommand, args) -> {
					if (!commandSender.hasPermission("sce.admin")) {
						ServerLog.sendPlayerMessage(commandSender, "You do not have permission to use this command!");
						return false;
					}

					if (args.length == 0) {
						ServerLog.sendPlayerMessage(commandSender, "<red>No subcommand provided</red>");
						return false;
					}

					String subCommand = args[0];
					Runnable action;
					String message;

					switch (subCommand) {
						case "open":
							action = this::open;
							message = "Server is now open for new players.";
							break;
						case "close":
							action = this::close;
							message = "Server is now closed for new players.";
							break;
						default:
							ServerLog.sendPlayerMessage(commandSender, "<red>Unknown sub-command: " + subCommand + "</red>");
							return true;
					}

					action.run();
					ServerLog.sendPlayerMessage(commandSender, message);

					return true;
				})
				.tabCompleter((commandSender, command, subcommand, args) -> {
					if (commandSender instanceof Player && args.length == 1) {
						return StringUtil.copyPartialMatches(args[args.length - 1], List.of("open", "close"), new ArrayList<>());
					}
					return Collections.emptyList();
				})
				.build();
	}


	private Listener createEventHandler() {
		return new Listener() {
			@EventHandler
			public void onPlayerJoin(PlayerJoinEvent event) {
				Player player = event.getPlayer();

				if (isClosed()) {

					if (!player.isOnline()) {
						return;
					}

					event.joinMessage(null);
					var miniMessage = MiniMessage.miniMessage();
					String message = "Server is closed!";
					Component parsed = miniMessage.deserialize(message);

					Bukkit.getScheduler().runTaskLater(plugin, () -> player.kick(parsed), 10L);
				}
			}

			@EventHandler
			public void onServerListPing(ServerListPingEvent event) {
				var miniMessage = MiniMessage.miniMessage();
				Component parsed;
				if (closed) {
					parsed = miniMessage.deserialize(closedMOTD);
				} else {
					parsed = miniMessage.deserialize(openMOTD);
				}
				event.motd(parsed);
			}
		};
	}

	public String getDefaultMOTD() {
		String motd = null;
		try (BufferedReader reader = new BufferedReader(new FileReader("server.properties"))) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("motd=")) {
					motd = line.substring(5); // Extract the MOTD value
					break;
				}
			}
		} catch (IOException e) {
			ServerLog.sendConsoleMessage(Level.SEVERE, "Could not read server.properties!");
		}
		return motd;
	}

	@Override
	public String getName() {
		return "CloseServer";
	}

	public void open() {
		closed = false;
	}

	public void close() {
		closed = true;
	}

	public boolean isClosed() {
		return closed;
	}

	@Override
	public void shutdown() {
		close();
	}

	@Override
	public void reloadConfig() {
		openMOTD = getDefaultMOTD();
	}
}
