package com.alexogden.module;

public interface ServerModule {

	void init();

	String getName();

	void execute();

	void shutdown();

	void reloadConfig();
}
