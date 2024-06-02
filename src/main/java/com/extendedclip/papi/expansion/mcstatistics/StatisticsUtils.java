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

    private static final Statistic SECONDS_PLAYED_STATISTIC = Statistic.PLAY_ONE_MINUTE;
    private static final Statistic SECONDS_SINCE_LAST_DEATH_STATISTIC = Statistic.TIME_SINCE_DEATH;

    //public static final String JAVADOC_BASE_LINK = ServerVersion.IS_LATEST ? "https://hub.spigotmc.org/javadocs/spigot" : "https://docs.docdex.helpch.at/" + ServerVersion.AS_STRING;

    public static int getSecondsPlayed(final OfflinePlayer player) {
        return player.getStatistic(SECONDS_PLAYED_STATISTIC) / 20;
    }

    public static int getSecondsSinceLastDeath(final OfflinePlayer player) {
        return player.getStatistic(SECONDS_SINCE_LAST_DEATH_STATISTIC) / 20;
    }

    @SuppressWarnings("Guava")
    public static String getStatistic(final OfflinePlayer player, final String identifier) {
        final Optional<Statistic> optional = Enums.getIfPresent(Statistic.class, identifier.toUpperCase());

        if (!optional.isPresent()) {
            return "Unknown statistic '" + identifier;
        }

        final Statistic statistic = optional.get();

        if (statistic.getType() != Statistic.Type.UNTYPED) {
            return "The statistic '" + identifier + "' require an argument";
        }

        int value = player.getStatistic(statistic) ;
        return Integer.toString(value);
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
