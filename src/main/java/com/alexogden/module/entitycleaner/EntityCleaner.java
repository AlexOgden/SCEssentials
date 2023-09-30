package com.alexogden.module.entitycleaner;

import com.alexogden.core.SCEssentials;
import com.alexogden.core.logging.ServerLog;
import com.alexogden.module.CommandHandler;
import com.alexogden.module.ServerModule;
import com.alexogden.util.Pair;
import com.alexogden.util.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.StringUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class EntityCleaner implements ServerModule {

	private final Listener eventHandler;
	private final CommandHandler commandHandler;
	private final Map<Item, Pair<String, Long>> entityMap = new ConcurrentHashMap<>();
	Plugin plugin = SCEssentials.getInstance();
	private long cleanInterval;
	private int maxEntityAge;
	private int taskID;
	private boolean enabled = false;

	public EntityCleaner() {
		this.eventHandler = createEventHandler();
		this.commandHandler = createCommandHandler();

		cleanInterval = TimeUtil.convertMinutesToTicks(plugin.getConfig().getInt("entity-cleaner.clean-interval"));
		maxEntityAge = plugin.getConfig().getInt("entity-cleaner.max-entity-age");
	}

	public void init() {
		plugin.getServer().getPluginManager().registerEvents(eventHandler, plugin);
		commandHandler.register();
		scheduleTask();
		enable();
	}

	private void scheduleTask() {
		taskID = Bukkit.getServer().getScheduler()
				.scheduleSyncRepeatingTask(plugin, this::execute, cleanInterval, cleanInterval);
	}

	private CommandHandler createCommandHandler() {
		return CommandHandler.builder("eclean")
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

					switch (subCommand) {
						case "now":
							execute();
							break;
						case "enable":
							enable();
							break;
						case "disable":
							disable();
							break;
						default:
							ServerLog.sendPlayerMessage(commandSender, "<red>Unknown sub-command: " + subCommand + "</red>");
							return true;
					}

					return true;
				})
				.tabCompleter((commandSender, command, subcommand, args) -> {
					if (commandSender instanceof Player && args.length == 1) {
						return StringUtil.copyPartialMatches(args[args.length - 1], List.of("now", "enable", "disable"), new ArrayList<>());
					}
					return Collections.emptyList();
				})
				.build();
	}

	private Listener createEventHandler() {
		return new Listener() {
			@EventHandler
			public void onPlayerDropItem(PlayerDropItemEvent event) {
				Player player = event.getPlayer();
				Item droppedItem = event.getItemDrop();
				entityMap.put(droppedItem, new Pair<>(player.getName(), System.currentTimeMillis()));
			}

			@EventHandler
			public void onPlayerQuit(PlayerQuitEvent event) {
				if(!Bukkit.getOnlinePlayers().isEmpty())
					cleanEntities(0);
			}
		};
	}

	@Override
	public void execute() {
		if (!enabled) {
			return;
		}

		if(!Bukkit.getOnlinePlayers().isEmpty())
			cleanEntities(maxEntityAge);
	}

	private void cleanEntities(int maxAge) {
		long currentTime = System.currentTimeMillis();
		var iterator = entityMap.entrySet().iterator();

		ServerLog.sendConsoleMessage(Level.INFO, "Cleaning Entities...");

		Map<String, Integer> deletionCountMap = new HashMap<>();
		while (iterator.hasNext()) {
			var entry = iterator.next();
			String player = entry.getValue().first();
			Item item = entry.getKey();
			long dropTime = entry.getValue().second();

			if (currentTime - dropTime >= (long) maxAge * 60 * 1000) {
				if (deletionCountMap.containsKey(player)) {
					int count = deletionCountMap.get(player);
					count += item.getItemStack().getAmount();
					deletionCountMap.put(player, count);
				} else {
					deletionCountMap.put(player, item.getItemStack().getAmount());
				}

				iterator.remove();
				item.remove();
			}
		}

		if (!deletionCountMap.isEmpty()) {
			String deletionStats = deletionCountMap.entrySet()
					.stream()
					.map(entry -> entry.getKey() + ": " + entry.getValue())
					.collect(Collectors.joining(", ", "[", "]"));
			ServerLog.broadcast("Cleaned up dropped entities: " + deletionStats, getName());
		} else {
			ServerLog.sendConsoleMessage(Level.INFO, "No entities to clean");
		}
	}

	public void shutdown() {
		plugin.getServer().getScheduler().cancelTask(taskID);
	}

	@Override
	public void reloadConfig() {
		cleanInterval = TimeUtil.convertMinutesToTicks(plugin.getConfig().getInt("entity-cleaner.clean-interval"));
		maxEntityAge = plugin.getConfig().getInt("entity-cleaner.max-entity-age");

		shutdown();
		scheduleTask();
	}

	@Override
	public String getName() {
		return "EntityCleaner";
	}

	public void enable() {
		enabled = true;
	}

	public void disable() {
		enabled = false;
	}

	public boolean isEnabled() {
		return enabled;
	}
}
