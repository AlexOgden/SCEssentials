package com.alexogden.module.entity.jukebox;

import com.alexogden.command.CommandHandler;
import com.alexogden.core.SCEssentials;
import com.alexogden.core.logging.ServerLog;
import com.alexogden.module.ServerModule;
import com.alexogden.util.BlockUtils;
import com.alexogden.util.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Jukebox;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InfiniJukebox implements ServerModule {

	Plugin plugin = SCEssentials.getInstance();

	private int taskID;
	private final CommandHandler commandHandler;
	private final Listener eventHandler;

	private final List<JukeboxState> jukeBoxes;
	private boolean enabled;

	public InfiniJukebox() {
		commandHandler = createCommandHandler();
		eventHandler = createEventHandler();
		jukeBoxes = new ArrayList<>();
	}

	@Override
	public void init() {
		commandHandler.register();
		plugin.getServer().getPluginManager().registerEvents(eventHandler, plugin);

		long checkInterval = TimeUtil.convertMinutesToTicks(1);

		enable();

		taskID = Bukkit.getServer().getScheduler()
				.scheduleSyncRepeatingTask(plugin, this::checkAndRestart, checkInterval, checkInterval);
	}

	@Override
	public String getName() {
		return "InfiniJukebox";
	}

	private CommandHandler createCommandHandler() {
		return CommandHandler.builder("jukebox")
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
						return StringUtil.copyPartialMatches(args[args.length - 1], List.of("enable", "disable"), new ArrayList<>());
					}
					return Collections.emptyList();
				})
				.build();
	}

	private Listener createEventHandler() {
		return new Listener() {
			@EventHandler
			public void onPlayerInteract(PlayerInteractEvent event) {
				Player player = event.getPlayer();
				Block clickedBlock = event.getClickedBlock();

				if (event.getAction() == Action.RIGHT_CLICK_BLOCK && clickedBlock != null && (clickedBlock.getType() == Material.JUKEBOX)) {
						Jukebox jukebox = (Jukebox) clickedBlock.getState();
						Material discType = player.getInventory().getItemInMainHand().getType();

						// Check if the player is holding a music disc
						if (isMusicDisc(discType)) {
							// Set the disc in the Jukebox and play it
							jukebox.setPlaying(discType);
							jukebox.update();
						}

						ServerLog.broadcast("Adding Jukebox to InfiniBox", getName());
						jukeBoxes.add(new JukeboxState(jukebox.getLocation(), discType));
				}
			}
		};
	}

	private void checkAndRestart() {
		if(!isEnabled()) {
			return;
		}
		for(var jukeboxState : jukeBoxes) {
			Jukebox jukebox = (Jukebox) jukeboxState.location().getBlock().getState();

			if (jukebox.getPlaying() == Material.AIR) {
				// Restart the music using the saved state
				ServerLog.broadcast("Restarting Jukebox with: " + jukeboxState.playingDisc(), getName());
				jukebox.setPlaying(jukeboxState.playingDisc());
			} else {
				ServerLog.broadcast("Jukebox is still playing: " + jukebox.getPlaying().name(), getName());
			}
		}
	}

	private boolean isMusicDisc(Material material) {
		return BlockUtils.getMusicDiscMaterials().contains(material);
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

	@Override
	public void shutdown() {
		plugin.getServer().getScheduler().cancelTask(taskID);
	}

	@Override
	public void reloadConfig() {
		// No Action Required
	}
}
