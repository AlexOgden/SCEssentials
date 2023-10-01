package com.alexogden.core;

import com.alexogden.core.logging.ServerLog;
import com.alexogden.module.ServerModule;
import com.alexogden.module.entity.EntityCleaner;
import com.alexogden.module.entity.jukebox.InfiniJukebox;
import com.alexogden.module.server.CloseServer;
import com.alexogden.module.server.Core;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SCEssentials extends JavaPlugin {
	private static SCEssentials instance;

	private final List<ServerModule> modules;

	public SCEssentials() {
		instance = this;
		modules = new ArrayList<>();
	}

	public static SCEssentials getInstance() {
		if (instance == null) {
			throw new IllegalStateException("Instance access before init");
		}
		return instance;
	}

	@Override
	public void onEnable() {
		if (!Bukkit.isPrimaryThread()) {
			throw new IllegalStateException("Init not fom main thread");
		}

		saveDefaultConfig();

		// Always add the core module
		modules.add(new Core());

		if (getConfig().getBoolean("entity-cleaner.enabled"))
			modules.add(new EntityCleaner());

		if (getConfig().getBoolean("close-server.enabled"))
			modules.add(new CloseServer());

		if(getConfig().getBoolean("infini-jukebox.enabled"))
			modules.add(new InfiniJukebox());


		for (ServerModule m : modules) {
			ServerLog.sendConsoleMessage("Enabling Module: " + m.getName());
			m.init();
		}
	}

	@Override
	public void onDisable() {
		for (ServerModule m : modules) {
			ServerLog.sendConsoleMessage("Stopping Module: " + m.getName());
			m.shutdown();
		}
	}

	public void reloadModules() {
		for (ServerModule m : modules) {
			ServerLog.sendConsoleMessage("Reloading Module: " + m.getName());
			m.reloadConfig();
		}
		super.reloadConfig();

		ServerLog.sendConsoleMessage("Reloaded all plugins");
	}

	@Override
	public @NotNull Logger getLogger() {
		return super.getLogger();
	}
}
