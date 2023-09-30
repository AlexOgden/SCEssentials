package com.alexogden.module;

public abstract class AbstractModule {
	private final String name;
	private boolean enabled = false;

	protected AbstractModule(String name) {
		this.name = name;
	}

	protected String getName() {
		return name;
	}

	protected boolean isEnabled() {
		return enabled;
	}

	protected void enable() {
		this.enabled = true;
	}

	protected void disable() {
		this.enabled = false;
	}
}
