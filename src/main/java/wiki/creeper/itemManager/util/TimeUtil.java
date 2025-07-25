package wiki.creeper.itemManager.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeUtil {
    
    private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Pattern DURATION_PATTERN = Pattern.compile("(\\d+)([smhd])");
    
    private static final Map<String, Long> TIME_UNITS = new HashMap<>();
    
    static {
        TIME_UNITS.put("s", 1000L);
        TIME_UNITS.put("m", 60000L);
        TIME_UNITS.put("h", 3600000L);
        TIME_UNITS.put("d", 86400000L);
    }
    
    @NotNull
    public static Timestamp parseDurationToTimestamp(@NotNull String duration) {
        long totalMillis = parseDurationToMillis(duration);
        return new Timestamp(System.currentTimeMillis() + totalMillis);
    }
    
    public static long parseDurationToMillis(@NotNull String duration) {
        long totalMillis = 0;
        Matcher matcher = DURATION_PATTERN.matcher(duration.toLowerCase());
        
        while (matcher.find()) {
            int value = Integer.parseInt(matcher.group(1));
            String unit = matcher.group(2);
            
            Long unitMillis = TIME_UNITS.get(unit);
            if (unitMillis != null) {
                totalMillis += value * unitMillis;
            }
        }
        
        if (totalMillis == 0) {
            throw new IllegalArgumentException("Invalid duration format: " + duration);
        }
        
        return totalMillis;
    }
    
    @NotNull
    public static String formatTimestamp(@NotNull Timestamp timestamp) {
        LocalDateTime dateTime = timestamp.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        return dateTime.format(DEFAULT_FORMATTER);
    }
    
    @NotNull
    public static String formatTimestamp(@NotNull Timestamp timestamp, @NotNull String pattern) {
        LocalDateTime dateTime = timestamp.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        return dateTime.format(DateTimeFormatter.ofPattern(pattern));
    }
    
    @NotNull
    public static String formatRemainingTime(@NotNull Timestamp expireTime) {
        long remaining = expireTime.getTime() - System.currentTimeMillis();
        
        if (remaining <= 0) {
            return "만료됨";
        }
        
        Duration duration = Duration.ofMillis(remaining);
        
        long days = duration.toDays();
        long hours = duration.toHours() % 24;
        long minutes = duration.toMinutes() % 60;
        long seconds = duration.getSeconds() % 60;
        
        StringBuilder result = new StringBuilder();
        
        if (days > 0) {
            result.append(days).append("일 ");
        }
        if (hours > 0) {
            result.append(hours).append("시간 ");
        }
        if (minutes > 0) {
            result.append(minutes).append("분 ");
        }
        if (seconds > 0 || result.length() == 0) {
            result.append(seconds).append("초");
        }
        
        return result.toString().trim();
    }
    
    public static boolean isExpired(@NotNull Timestamp timestamp) {
        return System.currentTimeMillis() > timestamp.getTime();
    }
    
    @Nullable
    public static Timestamp parseTimestamp(@NotNull String dateTimeString) {
        try {
            LocalDateTime dateTime = LocalDateTime.parse(dateTimeString, DEFAULT_FORMATTER);
            return Timestamp.valueOf(dateTime);
        } catch (Exception e) {
            return null;
        }
    }
    
    @Nullable
    public static Timestamp parseTimestamp(@NotNull String dateTimeString, @NotNull String pattern) {
        try {
            LocalDateTime dateTime = LocalDateTime.parse(dateTimeString, DateTimeFormatter.ofPattern(pattern));
            return Timestamp.valueOf(dateTime);
        } catch (Exception e) {
            return null;
        }
    }
}