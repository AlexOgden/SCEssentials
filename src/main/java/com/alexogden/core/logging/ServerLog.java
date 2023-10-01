package com.alexogden.core.logging;

import com.alexogden.core.SCEssentials;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class ServerLog {

	private ServerLog() {
		throw new AssertionError("Static Class");
	}

	public static void sendPlayerMessage(CommandSender player, String message) {
		if (!message.isEmpty()) {
			Component parsed = MiniMessage.miniMessage().deserialize(message);
			player.sendMessage(parsed);
		}
	}

	public static void sendConsoleMessage(String message) {
		sendConsoleMessage(Level.INFO, message);
	}

	public static void sendConsoleMessage(Level logLevel, String message) {
		if (!message.isEmpty()) {
			Logger consoleLogger = SCEssentials.getInstance().getLogger();
			consoleLogger.log(logLevel, message);
		}
	}

	public static void broadcast(String message) {
		broadcast(message, "SCEssentials");
	}

	public static void broadcast(String message, String tag) {
		if (!message.isEmpty()) {
			String prefix = String.format("<aqua>[%s]</aqua>: ", tag);
			Component parsed = MiniMessage.miniMessage().deserialize(prefix + message);

			Bukkit.getServer().getOnlinePlayers().forEach(player -> player.sendMessage(parsed));
			Bukkit.getConsoleSender().sendMessage(parsed);
		}
	}
}
