package com.extendedclip.papi.expansion.mcstatistics;

import com.google.common.primitives.Ints;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.entity.EntityType;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class for detecting server version for legacy support :(
 * @author Matt
 */
public final class ServerVersion {

    private static final int CURRENT_VERSION = getCurrentVersion();
    private static final int LATEST_VERSION = 1_19_2;

    public static final String AS_STRING = Bukkit.getBukkitVersion().split("-")[0];

    public static final boolean IS_LEGACY = CURRENT_VERSION < 1_13_0;
    /**
     * {@link org.bukkit.OfflinePlayer#getStatistic(Statistic)} was added in 1.15
     * @see org.bukkit.OfflinePlayer#getStatistic(Statistic, Material)
     * @see org.bukkit.OfflinePlayer#getStatistic(Statistic, EntityType)
     */
    public static final boolean SUPPORT_OFFLINE_PLAYERS = CURRENT_VERSION >= 1_15_0;
    public static final boolean IS_LATEST = CURRENT_VERSION >= LATEST_VERSION;

    private static int getCurrentVersion() {
        // No need to cache since will only run once
        final Matcher matcher = Pattern.compile("(?<version>\\d+\\.\\d+)(?<patch>\\.\\d+)?").matcher(AS_STRING);

        final StringBuilder stringBuilder = new StringBuilder();
        if (matcher.find()) {
            stringBuilder.append(matcher.group("version").replace(".", ""));
            final String patch = matcher.group("patch");
            if (patch == null) stringBuilder.append("0");
            else stringBuilder.append(patch.replace(".", ""));
        }

        //noinspection UnstableApiUsage
        final Integer version = Ints.tryParse(stringBuilder.toString());

        // Should never fail
        if (version == null) throw new IllegalArgumentException("Could not retrieve server version!");

        return version;
    }

}
