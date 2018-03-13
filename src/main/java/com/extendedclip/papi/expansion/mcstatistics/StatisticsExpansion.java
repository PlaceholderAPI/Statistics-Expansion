/*
 *
 * Statistics-Expansion
 * Copyright (C) 2018 Ryan McCarthy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */
package com.extendedclip.papi.expansion.mcstatistics;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.EnumSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import me.clip.placeholderapi.expansion.Cacheable;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.clip.placeholderapi.util.TimeFormat;
import me.clip.placeholderapi.util.TimeUtil;

public class StatisticsExpansion extends PlaceholderExpansion implements Cacheable {

	private final Set<Material> mine_block = EnumSet.noneOf(Material.class);
	private final Set<Material> use_item = EnumSet.noneOf(Material.class);
	private final Set<Material> break_item = EnumSet.noneOf(Material.class);
	private final Set<Material> craft_item = EnumSet.noneOf(Material.class);

	private final String VERSION = getClass().getPackage().getImplementationVersion();
	
	public StatisticsExpansion() {
		setup();
	}

	private void setup() {
		String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
		Class<?> craftStatistic;
		try {
			craftStatistic = Class.forName("org.bukkit.craftbukkit." + version + ".CraftStatistic");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return;
		}
		
		Method method;
		try {
			method = craftStatistic.getMethod("getMaterialStatistic", Statistic.class, Material.class);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			return;
		} catch (SecurityException e) {
			e.printStackTrace();
			return;
		}
		for (Material m : Material.values()) {
			try {
				if (method.invoke(null, Statistic.MINE_BLOCK, m) != null) {
					mine_block.add(m);
				}
				if (method.invoke(null, Statistic.USE_ITEM, m) != null) {
					use_item.add(m);
				}
				if (method.invoke(null, Statistic.BREAK_ITEM, m) != null) {
					break_item.add(m);
				}
				if (method.invoke(null, Statistic.CRAFT_ITEM, m) != null) {
					craft_item.add(m);
				}
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public String getAuthor() {
		return "clip";
	}

	@Override
	public String getIdentifier() {
		return "statistic";
	}

	@Override
	public String getPlugin() {
		return null;
	}

	@Override
	public String getVersion() {
		return VERSION;
	}

	@Override
	public String onPlaceholderRequest(Player p, String identifier) {
	
		if (p == null) {
			return "";
		}

		if (identifier.contains("mine_block_")) {
			try {
				String type = identifier.split("mine_block_")[1];
				long mine = p.getStatistic(Statistic.MINE_BLOCK, Material.valueOf(type));
				return String.valueOf(mine);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return "0";
			
		} else if (identifier.contains("use_item_")) {
			try {
				String type = identifier.split("use_item_")[1];
				long use = p.getStatistic(Statistic.USE_ITEM, Material.valueOf(type));
				return String.valueOf(use);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return "0";
			
		} else if (identifier.contains("break_item_")) {
			try {
				String type = identifier.split("break_item_")[1];
				long b = p.getStatistic(Statistic.BREAK_ITEM, Material.valueOf(type));
				return String.valueOf(b);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return "0";
			
		} else if (identifier.contains("craft_item_")) {
			try {
				String type = identifier.split("craft_item_")[1];
				long c = p.getStatistic(Statistic.CRAFT_ITEM, Material.valueOf(type));
				return String.valueOf(c);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return "0";
			
		} else if (identifier.contains("kill_entity_")) {
			try {
				String type = identifier.split("kill_entity_")[1];
				long kills = p.getStatistic(Statistic.KILL_ENTITY, EntityType.valueOf(type));
				return String.valueOf(kills);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return "0";
				
		} else if (identifier.contains("entity_killed_by_")) {
			try {
				String type = identifier.split("entity_killed_by_")[1];
				long kills = p.getStatistic(Statistic.ENTITY_KILLED_BY, EntityType.valueOf(type));
				return String.valueOf(kills);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return "0";
		}
		
		switch (identifier.toLowerCase()) {

		case "mine_block":
			long mine = 0;
			if (mine_block.isEmpty()) {
				return "0";
			}
			for (Material m : mine_block) {
				mine += p.getStatistic(Statistic.MINE_BLOCK, m);
			}
			return String.valueOf(mine);
			
		case "use_item":
			long use = 0;
			if (use_item == null) {
				return "0";
			}
			for (Material m : use_item) {
				use += p.getStatistic(Statistic.USE_ITEM, m);
			}
			return String.valueOf(use);
			
		case "break_item":
			long br = 0;
			if (break_item == null) {
				return "0";
			}
			for (Material m : break_item) {
				br += p.getStatistic(Statistic.BREAK_ITEM, m);
			}
			return String.valueOf(br);
			
		case "craft_item":
			long cr = 0;
			if (craft_item == null) {
				return "0";
			}
			for (Material m : craft_item) {
				cr += p.getStatistic(Statistic.CRAFT_ITEM, m);
			}
			return String.valueOf(cr);
		case "seconds_played":
			return String.valueOf(p.getStatistic(Statistic.PLAY_ONE_TICK) / 20);
		case "minutes_played":
			return String.valueOf((p.getStatistic(Statistic.PLAY_ONE_TICK) / 20) / 60);
		case "hours_played":
			return String.valueOf(((p.getStatistic(Statistic.PLAY_ONE_TICK) / 20) / 60) / 60);
		case "days_played":
			return String.valueOf((((p.getStatistic(Statistic.PLAY_ONE_TICK) / 20) / 60) / 60) / 24);
		case "time_played":
			return TimeUtil.getTime((int) (p.getStatistic(Statistic.PLAY_ONE_TICK) / 20));
		case "days_played_remaining":
			return TimeUtil.getRemaining((int) p.getStatistic(Statistic.PLAY_ONE_TICK) / 20, TimeFormat.DAYS);
		case "hours_played_remaining":
			return TimeUtil.getRemaining((int) p.getStatistic(Statistic.PLAY_ONE_TICK) / 20, TimeFormat.HOURS);
		case "minutes_played_remaining":
			return TimeUtil.getRemaining((int) p.getStatistic(Statistic.PLAY_ONE_TICK) / 20, TimeFormat.MINUTES);
		case "seconds_played_remaining":
			return TimeUtil.getRemaining((int) p.getStatistic(Statistic.PLAY_ONE_TICK) / 20, TimeFormat.SECONDS);
		case "time_since_death":
			return TimeUtil.getTime((int) p.getStatistic(Statistic.TIME_SINCE_DEATH) / 20);
		case "seconds_since_death":
			return String.valueOf(p.getStatistic(Statistic.TIME_SINCE_DEATH) / 20L);
		case "minutes_since_death":
			return String.valueOf((p.getStatistic(Statistic.TIME_SINCE_DEATH) / 20L) / 60L);
		case "hours_since_death":
			return String.valueOf(((p.getStatistic(Statistic.TIME_SINCE_DEATH) / 20L) / 60L) / 60L);
		case "days_since_death":
			return String.valueOf((((p.getStatistic(Statistic.TIME_SINCE_DEATH) / 20L) / 60L) / 60L) / 24L);
		}

		try {
			return String.valueOf(p.getStatistic(Statistic.valueOf(identifier)));
		} catch (Exception ex) {
			return null;
		}	
	}

	@Override
	public void clear() {
		break_item.clear();
		craft_item.clear();
		mine_block.clear();
		use_item.clear();
	}
}
