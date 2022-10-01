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
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;

import java.util.StringJoiner;

public class StatisticsUtils {

    private static final Statistic SECONDS_PLAYED_STATISTIC = ServerVersion.IS_LEGACY ? Statistic.valueOf("PLAY_ONE_TICK") : Statistic.PLAY_ONE_MINUTE;
    private static final Statistic SECONDS_SINCE_LAST_DEATH_STATISTIC = ServerVersion.IS_LEGACY ? Statistic.valueOf("TIME_SINCE_DEATH") : Statistic.TIME_SINCE_DEATH;

    public static final String JAVADOC_BASE_LINK = ServerVersion.IS_LATEST ? "https://hub.spigotmc.org/javadocs/spigot" : "https://docs.docdex.helpch.at/" + ServerVersion.AS_STRING;

    public static int getSecondsPlayed(final OfflinePlayer player) {
        return (ServerVersion.SUPPORT_OFFLINE_PLAYERS ? player.getStatistic(SECONDS_PLAYED_STATISTIC) : player.getPlayer().getStatistic(SECONDS_PLAYED_STATISTIC)) / 20;
    }

    public static int getSecondsSinceLastDeath(final OfflinePlayer player) {
        return (ServerVersion.SUPPORT_OFFLINE_PLAYERS ? player.getStatistic(SECONDS_SINCE_LAST_DEATH_STATISTIC) : player.getPlayer().getStatistic(SECONDS_SINCE_LAST_DEATH_STATISTIC)) / 20;
    }

    @SuppressWarnings("Guava")
    public static String getStatistic(final OfflinePlayer player, final String identifier) {
        final Optional<Statistic> optional = Enums.getIfPresent(Statistic.class, identifier.toUpperCase());

        if(!ServerVersion.SUPPORT_OFFLINE_PLAYERS && !player.isOnline()) {
            return "Cannot get statistic, player is offline";
        }

        if (!optional.isPresent()) {
            return "Unknown statistic '" + identifier + "', check " + JAVADOC_BASE_LINK + "/org/bukkit/Statistic.html for more info";
        }

        final Statistic statistic = optional.get();

        if (statistic.getType() != Statistic.Type.UNTYPED) {
            return "The statistic '" + identifier + "' require an argument, check " + JAVADOC_BASE_LINK + "/org/bukkit/Statistic.Type.html for more info" ;
        }

        int value = ServerVersion.SUPPORT_OFFLINE_PLAYERS ? player.getStatistic(statistic) : player.getPlayer().getStatistic(statistic);
        return Integer.toString(value);
    }

    public static boolean isItem(final Material material, final boolean isLegacy) {
        if (isLegacy) {
            switch (material.name()) {
                case "ACACIA_DOOR":
                case "BED_BLOCK":
                case "BEETROOT_BLOCK":
                case "BIRCH_DOOR":
                case "BREWING_STAND":
                case "BURNING_FURNACE":
                case "CAKE_BLOCK":
                case "CARROT":
                case "CAULDRON":
                case "COCOA":
                case "CROPS":
                case "DARK_OAK_DOOR":
                case "DAYLIGHT_DETECTOR_INVERTED":
                case "DIODE_BLOCK_OFF":
                case "DIODE_BLOCK_ON":
                case "DOUBLE_STEP":
                case "DOUBLE_STONE_SLAB2":
                case "ENDER_PORTAL":
                case "END_GATEWAY":
                case "FIRE":
                case "FLOWER_POT":
                case "FROSTED_ICE":
                case "GLOWING_REDSTONE_ORE":
                case "IRON_DOOR_BLOCK":
                case "JUNGLE_DOOR":
                case "LAVA":
                case "MELON_STEM":
                case "NETHER_WARTS":
                case "PISTON_EXTENSION":
                case "PISTON_MOVING_PIECE":
                case "PORTAL":
                case "POTATO":
                case "PUMPKIN_STEM":
                case "PURPUR_DOUBLE_SLAB":
                case "REDSTONE_COMPARATOR_OFF":
                case "REDSTONE_COMPARATOR_ON":
                case "REDSTONE_LAMP_ON":
                case "REDSTONE_TORCH_OFF":
                case "REDSTONE_WIRE":
                case "SIGN_POST":
                case "SKULL":
                case "SPRUCE_DOOR":
                case "STANDING_BANNER":
                case "STATIONARY_LAVA":
                case "STATIONARY_WATER":
                case "SUGAR_CANE_BLOCK":
                case "TRIPWIRE":
                case "WALL_BANNER":
                case "WALL_SIGN":
                case "WATER":
                case "WOODEN_DOOR":
                case "WOOD_DOUBLE_STEP":
                    return false;
                default:
                    return true;
            }
        } else {
            return material.isItem();
        }
    }

    /**
     * @author Sxtanna
     */
    public static String formatTime(final long time) {
        if (time < 1) {
            return "";
        }

        if (time < 60) {
            return time + "s";
        }

        long seconds = time;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        long weeks = days / 7;

        seconds %= 60;
        minutes %= 60;
        hours %= 24;
        days %= 7;

        final StringJoiner joiner = new StringJoiner(" ");
        appendTime(joiner, weeks, "w");
        appendTime(joiner, days, "d");
        appendTime(joiner, hours, "h");
        appendTime(joiner, minutes, "m");
        appendTime(joiner, seconds, "s");
        return joiner.toString();
    }

    private static void appendTime(final StringJoiner joiner, final long value, final String unit) {
        if (value > 0) {
            joiner.add(value + unit);
        }
    }

}
