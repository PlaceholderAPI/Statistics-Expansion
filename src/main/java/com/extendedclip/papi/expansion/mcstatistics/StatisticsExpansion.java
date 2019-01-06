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

import java.util.EnumSet;
import java.util.Set;
import me.clip.placeholderapi.expansion.Cacheable;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.clip.placeholderapi.util.TimeUtil;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class StatisticsExpansion extends PlaceholderExpansion implements Cacheable {

  private final Set<Material> mine_block = EnumSet.noneOf(Material.class);
  private final Set<Material> use_item = EnumSet.noneOf(Material.class);
  private final Set<Material> break_item = EnumSet.noneOf(Material.class);
  private final Set<Material> craft_item = EnumSet.noneOf(Material.class);
  private final String VERSION = getClass().getPackage().getImplementationVersion();

  public StatisticsExpansion() {
    setup();
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
  public String getVersion() {
    return VERSION;
  }

  private void setup() {
    for (Material m : Material.values()) {
      // md_5 laughed at using reflection for this and said below works so lets find out...
      if (m.isBlock()) {
        mine_block.add(m);
      }
      if (m.isItem()) {
        use_item.add(m);
        craft_item.add(m);
        break_item.add(m);
      }
    }
  }

  @Override
  public String onPlaceholderRequest(Player p, String identifier) {
    if (p == null) {
      return "";
    }
    switch (identifier.toLowerCase()) {
      case "mine_block":
        long mine = 0;
        if (mine_block.isEmpty()) {
          return "0";
        }
        for (Material m : mine_block) {
          long value;
          try {
            value = p.getStatistic(Statistic.MINE_BLOCK, m);
          } catch (Exception e) {
            // System.out.println(String.format("Error: papi statistic for %s mine_block %s (%d): %s", p.getName(), m.name(), m.getId(), e.getMessage()));
            value = 0;
          }
          mine += value;
        }
        return String.valueOf(mine);

      case "use_item":
        long use = 0;
        if (use_item.isEmpty()) {
          return "0";
        }
        for (Material m : use_item) {
          use += p.getStatistic(Statistic.USE_ITEM, m);
        }
        return String.valueOf(use);

      case "break_item":
        long br = 0;
        if (break_item.isEmpty()) {
          return "0";
        }
        for (Material m : break_item) {
          br += p.getStatistic(Statistic.BREAK_ITEM, m);
        }
        return String.valueOf(br);

      case "craft_item":
        long cr = 0;
        if (craft_item.isEmpty()) {
          return "0";
        }
        for (Material m : craft_item) {
          cr += p.getStatistic(Statistic.CRAFT_ITEM, m);
        }
        return String.valueOf(cr);
      case "seconds_played":
        try {
          return String.valueOf(p.getStatistic(Statistic.PLAY_ONE_MINUTE) / 20);
        } catch (NoSuchFieldError error) {
          return String.valueOf(p.getStatistic(Statistic.valueOf("PLAY_ONE_TICK")) / 20);
        }
      case "minutes_played":
        try {
          return String.valueOf((p.getStatistic(Statistic.PLAY_ONE_MINUTE) / 20) / 60);
        } catch (NoSuchFieldError error) {
          return String.valueOf((p.getStatistic(Statistic.valueOf("PLAY_ONE_TICK")) / 20) / 60);
        }

      case "hours_played":
        try {
          return String.valueOf(((p.getStatistic(Statistic.PLAY_ONE_MINUTE) / 20) / 60) / 60);
        } catch (NoSuchFieldError error) {
          return String.valueOf(((p.getStatistic(Statistic.valueOf("PLAY_ONE_TICK")) / 20) / 60) / 60);
        }

      case "days_played":
        try {
          return String.valueOf((((p.getStatistic(Statistic.PLAY_ONE_MINUTE) / 20) / 60) / 60) / 24);
        } catch (NoSuchFieldError error) {
          return String.valueOf((((p.getStatistic(Statistic.valueOf("PLAY_ONE_TICK")) / 20) / 60) / 60) / 24);
        }

      case "time_played":
        try {
          return TimeUtil.getTime((p.getStatistic(Statistic.PLAY_ONE_MINUTE) / 20));
        } catch (NoSuchFieldError error) {
          return TimeUtil.getTime((p.getStatistic(Statistic.valueOf("PLAY_ONE_TICK")) / 20));
        }
      case "time_since_death":
        return TimeUtil.getTime(p.getStatistic(Statistic.TIME_SINCE_DEATH) / 20);
      case "seconds_since_death":
        return String.valueOf(p.getStatistic(Statistic.TIME_SINCE_DEATH) / 20L);
      case "minutes_since_death":
        return String.valueOf((p.getStatistic(Statistic.TIME_SINCE_DEATH) / 20L) / 60L);
      case "hours_since_death":
        return String.valueOf(((p.getStatistic(Statistic.TIME_SINCE_DEATH) / 20L) / 60L) / 60L);
      case "days_since_death":
        return String.valueOf((((p.getStatistic(Statistic.TIME_SINCE_DEATH) / 20L) / 60L) / 60L) / 24L);
    }

    Statistic stat;
    int t = identifier.indexOf(":");
    // %statistic_<Statistic>%
    if (t == -1) {
      try {
        stat = Statistic.valueOf(identifier.toUpperCase());
        if (stat.getType() == Statistic.Type.UNTYPED) {
          return String.valueOf(p.getStatistic(stat));
        }
      } catch (IllegalArgumentException | NullPointerException ex) {
        ex.printStackTrace();
      }
      return "invalid stat";
    }

    String name = identifier.substring(0, t).toUpperCase();
    String types = identifier.substring(t + 1).toUpperCase();

    try {
      stat = Statistic.valueOf(name);
      if (stat.getType() == Statistic.Type.UNTYPED) {
        return String.valueOf(p.getStatistic(stat));
      }
    } catch (IllegalArgumentException | NullPointerException ex) {
      ex.printStackTrace();
      return "invalid stat";
    }

    // %statistic_<Statistic>:<Material/Entity>%
    if (!types.contains(",")) {
      switch (stat.getType()) {
        case BLOCK:
        case ITEM:
          try {
            return String.valueOf(p.getStatistic(stat, Material.getMaterial(types)));
          } catch (IllegalArgumentException | NullPointerException ex) {
            ex.printStackTrace();
            return "invalid material param";
          }
        case ENTITY:
          try {
            return String.valueOf(p.getStatistic(stat, EntityType.valueOf(types)));
          } catch (IllegalArgumentException | NullPointerException ex) {
            ex.printStackTrace();
            return "invalid entity param";
          }
        default:
          break;
      }
    }

    // %statistic_<Statistic>:<Material/Entity>,<Material/Entity>,<Material/Entity>%
    String[] args = types.split(",");
    int total = 0;
    switch (stat.getType()) {
      case BLOCK:
      case ITEM:
        for (String arg : args) {
          try {
            total += p.getStatistic(stat, Material.getMaterial(arg));
          } catch (IllegalArgumentException | NullPointerException ex) {
            ex.printStackTrace();
          }
        }
        break;
      case ENTITY:
        for (String arg : args) {
          try {
            total += p.getStatistic(stat, EntityType.valueOf(arg));
          } catch (IllegalArgumentException | NullPointerException ex) {
            ex.printStackTrace();
          }
        }
        break;
      default:
        break;
    }

    return String.valueOf(total);
  }

  @Override
  public void clear() {
    break_item.clear();
    craft_item.clear();
    mine_block.clear();
    use_item.clear();
  }
}
