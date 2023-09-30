package com.alexogden.module.server;

import com.alexogden.core.SCEssentials;
import com.alexogden.core.logging.ServerLog;
import com.alexogden.module.CommandHandler;
import com.alexogden.module.ServerModule;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Core implements ServerModule {

	private final CommandHandler commandHandler;

	public Core() {
		this.commandHandler = createCommandHandler();
	}

	@Override
	public void init() {
		commandHandler.register();
	}

	private CommandHandler createCommandHandler() {
		return CommandHandler.builder("sce")
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
						case "reload":
							SCEssentials.getInstance().reloadModules();
							break;
						default:
							ServerLog.sendPlayerMessage(commandSender, "<red>Unknown sub-command: " + subCommand + "</red>");
							return true;
					}

					return true;
				})
				.tabCompleter((commandSender, command, subcommand, args) -> {
					if (commandSender instanceof Player && args.length == 1) {
						return StringUtil.copyPartialMatches(args[args.length - 1], List.of("reload"), new ArrayList<>());
					}
					return Collections.emptyList();
				})
				.build();
	}

	@Override
	public String getName() {
		return "Core";
	}

	@Override
	public void execute() {
		// Not Required
	}

	@Override
	public void shutdown() {
		// Not Required
	}

	@Override
	public void reloadConfig() {
		// Not Required
	}
}
