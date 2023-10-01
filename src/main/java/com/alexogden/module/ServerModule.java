package com.alexogden.module;

public interface ServerModule {

	void init();

	String getName();

	void shutdown();

	void reloadConfig();
}
