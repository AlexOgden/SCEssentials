package com.alexogden.util;

import com.alexogden.core.logging.ServerLog;
import org.bukkit.Material;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

public class BlockUtils {

	private BlockUtils() {
		throw new IllegalStateException("Static Class");
	}
	public static List<Material> getMusicDiscMaterials() {
		List<Material> musicDiscMaterials = new ArrayList<>();

		Field[] fields = Material.class.getDeclaredFields();
		for (Field field : fields) {
			if (field.getName().startsWith("MUSIC_DISC") && Material.class.isAssignableFrom(field.getType())) {
				try {
					Material material = (Material) field.get(null);
					musicDiscMaterials.add(material);
				} catch (IllegalAccessException e) {
					ServerLog.sendConsoleMessage(Level.SEVERE, "IllegalAccessException in getMusicDiscs: " + e.getMessage());
				}
			}
		}

		return Collections.unmodifiableList(musicDiscMaterials);
	}
}
