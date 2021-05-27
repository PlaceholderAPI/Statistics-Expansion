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
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.entity.EntityType;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;

public class StatisticsExpansion extends PlaceholderExpansion implements Cacheable {

    private final ListMultimap<Statistic, Material> ignoredMaterials = ArrayListMultimap.create();
    private final String VERSION = getClass().getPackage().getImplementationVersion();
    private final boolean isLegacy = !Enums.getIfPresent(Material.class, "TURTLE_HELMET").isPresent();
    public static final String SERVER_VERSION = Bukkit.getBukkitVersion().split("-")[0];
    private final boolean supportOfflinePlayers = Enums.getIfPresent(Material.class, "BEEHIVE").isPresent();
    
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

        if (!supportOfflinePlayers && !player.isOnline()) {
            return "";
        }

        final int secondsPlayed = StatisticsUtils.getSecondsPlayed(player, isLegacy, supportOfflinePlayers);
        final int secondsSinceLastDeath = StatisticsUtils.getSecondsSinceLastDeath(player, isLegacy, supportOfflinePlayers);

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
                return StatisticsUtils.formatTime(Duration.of(secondsPlayed, ChronoUnit.SECONDS));
            }

            case "time_played:seconds": {
                //return seconds != 0 ? Long.toString(TimeUnit.MINUTES.toSeconds(1) - seconds) : "0";
                return Integer.toString(secondsPlayed % 60);
            }

            case "time_played:minutes": {
                //return minutes != 0 ? Long.toString(TimeUnit.HOURS.toMinutes(1) - minutes) : "0";
                return Long.toString(TimeUnit.SECONDS.toMinutes(secondsPlayed) % 60);
            }

            case "time_played:hours": {
                //return hours != 0 ? Long.toString(TimeUnit.DAYS.toHours(1) - hours) : "0";
                return Long.toString(TimeUnit.SECONDS.toHours(secondsPlayed) % 24);
            }

            case "seconds_played": {
                return Integer.toString(secondsPlayed);
            }

            case "minutes_played": {
                return Long.toString(TimeUnit.SECONDS.toMinutes(secondsPlayed));
            }

            case "hours_played": {
                return Long.toString(TimeUnit.SECONDS.toHours(secondsPlayed));
            }

            case "time_played:days":
            case "days_played": {
                return Long.toString(TimeUnit.SECONDS.toDays(secondsPlayed));
            }

            /*
             * Time since last death
             */
            case "time_since_death": {
                return StatisticsUtils.formatTime(Duration.of(secondsSinceLastDeath, ChronoUnit.SECONDS));
            }

            case "seconds_since_death": {
                return Integer.toString(secondsSinceLastDeath);
            }

            case "minutes_since_death": {
                return Long.toString(TimeUnit.SECONDS.toMinutes(secondsSinceLastDeath));
            }

            case "hours_since_death": {
                return Long.toString(TimeUnit.SECONDS.toHours(secondsSinceLastDeath));
            }

            case "days_since_death": {
                return Long.toString(TimeUnit.SECONDS.toDays(secondsSinceLastDeath));
            }
        }

        final int splitterIndex = identifier.indexOf(':');
        // %statistic_<Statistic>%
        if (splitterIndex == -1) {
            return StatisticsUtils.getStatistic(player, identifier, supportOfflinePlayers);
        }

        final String statisticIdentifier = identifier.substring(0, splitterIndex).toUpperCase();
        final String types = identifier.substring(splitterIndex + 1).toUpperCase();

        if (types.trim().isEmpty()) {
            return StatisticsUtils.getStatistic(player, statisticIdentifier, supportOfflinePlayers);
        }

        final Optional<Statistic> statisticOptional = Enums.getIfPresent(Statistic.class, statisticIdentifier);

        if (!statisticOptional.isPresent()) {
            return "Unknown statistic '" + statisticIdentifier + "', check https://helpch.at/docs/" + SERVER_VERSION + "/org/bukkit/Statistic.html for more info";
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
                        return Integer.toString(supportOfflinePlayers ? player.getStatistic(statistic, material.get()) : player.getPlayer().getStatistic(statistic, material.get()));
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
                        return Integer.toString(supportOfflinePlayers ? player.getStatistic(statistic, entityType.get()) : player.getPlayer().getStatistic(statistic, entityType.get()));
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
                        total.addAndGet(supportOfflinePlayers ? player.getStatistic(statistic, material.get()) : player.getPlayer().getStatistic(statistic, material.get()));
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
                        total.addAndGet(supportOfflinePlayers ? player.getStatistic(statistic, entityType.get()) : player.getPlayer().getStatistic(statistic, entityType.get()));
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
                total.addAndGet(supportOfflinePlayers ? player.getStatistic(statistic, material) : player.getPlayer().getStatistic(statistic, material));
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
