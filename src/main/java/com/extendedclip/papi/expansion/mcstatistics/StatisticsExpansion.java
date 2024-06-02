/*
 *
 * Statistics-Expansion
 * Copyright (C) 2020 Ryan McCarthy
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

import com.google.common.base.Enums;
import com.google.common.base.Optional;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.Cacheable;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.entity.EntityType;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;

public class StatisticsExpansion extends PlaceholderExpansion implements Cacheable {

    private final ListMultimap<Statistic, Material> ignoredMaterials = ArrayListMultimap.create();
    private final String VERSION = getClass().getPackage().getImplementationVersion();

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

    @SuppressWarnings({"DuplicatedCode", "Guava"})
    @Override
    public String onRequest(OfflinePlayer player, String identifier) {
        if (player == null) {
            return "";
        }

        switch (identifier.toLowerCase()) {
            case "mine_block": {
                return calculateTotal(player, Statistic.MINE_BLOCK);
            }

            case "use_item": {
                return calculateTotal(player, Statistic.USE_ITEM);
            }

            case "break_item": {
                return calculateTotal(player, Statistic.BREAK_ITEM);
            }

            case "craft_item": {
                return calculateTotal(player, Statistic.CRAFT_ITEM);
            }

            /*
             * Time played
             */
            case "time_played": {
                return StatisticsUtils.formatTime(StatisticsUtils.getSecondsPlayed(player));
            }

            case "time_played:seconds": {
                return Integer.toString(StatisticsUtils.getSecondsPlayed(player) % 60);
            }

            case "time_played:minutes": {
                return Long.toString(TimeUnit.SECONDS.toMinutes(StatisticsUtils.getSecondsPlayed(player)) % 60);
            }

            case "time_played:hours": {
                return Long.toString(TimeUnit.SECONDS.toHours(StatisticsUtils.getSecondsPlayed(player)) % 24);
            }

            case "seconds_played": {
                return Integer.toString(StatisticsUtils.getSecondsPlayed(player));
            }

            case "minutes_played": {
                return Long.toString(TimeUnit.SECONDS.toMinutes(StatisticsUtils.getSecondsPlayed(player)));
            }

            case "hours_played": {
                return Long.toString(TimeUnit.SECONDS.toHours(StatisticsUtils.getSecondsPlayed(player)));
            }

            case "time_played:days":
            case "days_played": {
                return Long.toString(TimeUnit.SECONDS.toDays(StatisticsUtils.getSecondsPlayed(player)));
            }

            /*
             * Time since last death
             */
            case "time_since_death": {
                return StatisticsUtils.formatTime(StatisticsUtils.getSecondsSinceLastDeath(player));
            }

            case "seconds_since_death": {
                return Integer.toString(StatisticsUtils.getSecondsSinceLastDeath(player));
            }

            case "minutes_since_death": {
                return Long.toString(TimeUnit.SECONDS.toMinutes(StatisticsUtils.getSecondsSinceLastDeath(player)));
            }

            case "hours_since_death": {
                return Long.toString(TimeUnit.SECONDS.toHours(StatisticsUtils.getSecondsSinceLastDeath(player)));
            }

            case "days_since_death": {
                return Long.toString(TimeUnit.SECONDS.toDays(StatisticsUtils.getSecondsSinceLastDeath(player)));
            }
        }

        final int splitterIndex = identifier.indexOf(':');
        // %statistic_<Statistic>%
        if (splitterIndex == -1) {
            return StatisticsUtils.getStatistic(player, identifier);
        }

        final String statisticIdentifier = identifier.substring(0, splitterIndex).toUpperCase();
        final String types = identifier.substring(splitterIndex + 1).toUpperCase();

        if (types.trim().isEmpty()) {
            return StatisticsUtils.getStatistic(player, statisticIdentifier);
        }

        final Optional<Statistic> statisticOptional = Enums.getIfPresent(Statistic.class, statisticIdentifier);

        if (!statisticOptional.isPresent()) {
            return "Unknown statistic '" + statisticIdentifier;
        }

        final Statistic statistic = statisticOptional.get();

        // %statistic_<Statistic>:<Material/Entity>%
        if (!types.contains(",")) {
            switch (statistic.getType()) {
                case BLOCK:
                case ITEM: {
                    final Optional<Material> material = Enums.getIfPresent(Material.class, types);

                    if (!material.isPresent()) {
                        return "Invalid material " + types;
                    }

                    try {
                        return Integer.toString(player.getStatistic(statistic, material.get()));
                    } catch (IllegalArgumentException e) {
                        errorLog("Could not get the statistic '" + statistic.name() + "' for '" + material.get().name() + "'", e);
                        return "Could not get the statistic '" + statistic.name() + "' for '" + material.get().name() + "'";
                    }
                }

                case ENTITY: {
                    final Optional<EntityType> entityType = Enums.getIfPresent(EntityType.class, types);

                    if (!entityType.isPresent()) {
                        return "Invalid entity " + types;
                    }

                    try {
                        return Integer.toString(player.getStatistic(statistic, entityType.get()));
                    } catch (IllegalArgumentException e) {
                        errorLog("Could not get the statistic '" + statistic.name() + "' for '" + entityType.get().name() + "'", e);
                        return "Could not get the statistic '" + statistic.name() + "' for '" + entityType.get().name() + "'";
                    }
                }

                default:
                    break;
            }
        }

        // %statistic_<Statistic>:<Material/Entity>,<Material/Entity>,<Material/Entity>%
        final String[] args = types.split(",");
        final AtomicInteger total = new AtomicInteger();

        switch (statistic.getType()) {
            case BLOCK:
            case ITEM: {
                for (String arg : args) {
                    final Optional<Material> material = Enums.getIfPresent(Material.class, arg);

                    if (!material.isPresent()) {
                        continue;
                    }

                    try {
                        total.addAndGet(player.getStatistic(statistic, material.get()));
                    } catch (IllegalArgumentException e) {
                        errorLog("Could not get the statistic '" + statistic.name() + "' for '" + material.get().name() + "'", e);
                        break;
                    }
                }

                break;
            }

            case ENTITY: {
                for (String arg : args) {
                    final Optional<EntityType> entityType = Enums.getIfPresent(EntityType.class, arg);

                    if (!entityType.isPresent()) {
                        continue;
                    }

                    try {
                        total.addAndGet(player.getStatistic(statistic, entityType.get()));
                    } catch (IllegalArgumentException e) {
                        errorLog("Could not get the statistic '" + statistic.name() + "' for '" + entityType.get().name() + "'", e);
                        break;
                    }
                }

                break;
            }

            default:
                break;
        }

        return Integer.toString(total.get());
    }

    private String calculateTotal(final OfflinePlayer player, final Statistic statistic) {
        final AtomicLong total = new AtomicLong();

        for (Material material : Material.values()) {
            if (ignoredMaterials.get(statistic).contains(material)) {
                continue;
            }

            if (statistic == Statistic.MINE_BLOCK && (material.name().equals("GRASS") || material.name().equals("SOIL"))) {
                continue;
            }

            try {
                total.addAndGet(player.getStatistic(statistic, material));
            } catch (IllegalArgumentException ignored) {
                ignoredMaterials.put(statistic, material);
            }
        }

        return Long.toString(total.get());
    }

    private void errorLog(final String message, final Throwable exception) {
        PlaceholderAPIPlugin.getInstance().getLogger().log(Level.SEVERE, "[Statistic Expansion] " + message, exception);
    }

    @Override
    public void clear() {
        ignoredMaterials.clear();
    }
}
