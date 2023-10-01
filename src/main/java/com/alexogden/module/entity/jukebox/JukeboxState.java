package com.alexogden.module.entity.jukebox;

import org.bukkit.Location;
import org.bukkit.Material;

public record JukeboxState(Location location, Material playingDisc) {
}