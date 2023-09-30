package com.alexogden.command;

import com.alexogden.core.SCEssentials;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;

import java.util.Objects;

public class CommandHandler {
	final SCEssentials plugin = SCEssentials.getInstance();
	private final String command;
	private CommandExecutor executor;
	private TabCompleter tabCompleter;

	private CommandHandler(String command) {
		this.command = command;
	}

	public static Builder builder(String command) {
		return new CommandHandler(command).new Builder();
	}

	public void register() {
		// Check if required fields are initialized
		if (executor == null) {
			throw new IllegalStateException("Executor is not initialized.");
		}

		if (tabCompleter == null) {
			throw new IllegalStateException("TabCompleter is not initialized.");
		}

		Objects.requireNonNull(plugin.getCommand(command)).setExecutor(executor);
		Objects.requireNonNull(plugin.getCommand(command)).setTabCompleter(tabCompleter);
	}

	public class Builder {
		public Builder executor(CommandExecutor executor) {
			CommandHandler.this.executor = executor;
			return this;
		}

		public Builder tabCompleter(TabCompleter tabCompleter) {
			CommandHandler.this.tabCompleter = tabCompleter;
			return this;
		}

		public CommandHandler build() {
			// Check if required fields are initialized
			if (executor == null) {
				throw new IllegalStateException("Executor is not initialized.");
			}

			if (tabCompleter == null) {
				throw new IllegalStateException("TabCompleter is not initialized.");
			}

			return CommandHandler.this;
		}

		public void register() {
			Objects.requireNonNull(plugin.getCommand(command)).setExecutor(executor);
			Objects.requireNonNull(plugin.getCommand(command)).setTabCompleter(tabCompleter);
		}
	}
}
