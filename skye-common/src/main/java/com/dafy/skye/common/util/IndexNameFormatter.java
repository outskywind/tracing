package com.dafy.skye.common.util;

import com.google.auto.value.AutoValue;
import org.elasticsearch.common.inject.internal.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Caedmon on 2017/5/2.
 */
@AutoValue
public abstract class IndexNameFormatter {
    public static Builder builder() {
        return new AutoValue_IndexNameFormatter.Builder();
    }

    abstract Builder toBuilder();

    private static final String DAILY_INDEX_FORMAT = "yyyy-MM-dd";

    public abstract String index();

    public abstract char dateSeparator();

    public abstract ThreadLocal<SimpleDateFormat> dateFormat(); // SimpleDateFormat isn't thread-safe

    public @AutoValue.Builder static abstract class Builder {
        public abstract Builder index(String index);

        public abstract Builder dateSeparator(char dateSeparator);

        public abstract Builder dateFormat(ThreadLocal<SimpleDateFormat> dateFormat);

        public abstract char dateSeparator();

        public final IndexNameFormatter build() {
            return dateFormat(new ThreadLocal<SimpleDateFormat>() {
                @Override protected SimpleDateFormat initialValue() {
                    SimpleDateFormat result =
                            new SimpleDateFormat(DAILY_INDEX_FORMAT.replace('-', dateSeparator()));
                    return result;
                }
            }).autoBuild();
        }

        public abstract IndexNameFormatter autoBuild();
    }

    /**
     * Returns a set of index patterns that represent the range provided. Notably, this compresses
     * months or years using wildcards (in order to send smaller API calls).
     *
     * <p>For example, if {@code beginMillis} is 2016-11-30 and {@code endMillis} is 2017-01-02, the
     * result will be 2016-11-30, 2016-12-*, 2017-01-01 and 2017-01-02.
     */
    public List<String> formatTypeAndRange(@Nullable String type, long beginMillis, long endMillis) {
        Calendar current = midnightUTC(beginMillis);
        Calendar end = midnightUTC(endMillis);
        if (current.equals(end)) {
            return Collections.singletonList(formatTypeAndTimestamp(type,current.getTimeInMillis()));
        }

        List<String> indices = new ArrayList<>();
        while (current.compareTo(end) <= 0) {
            if (current.get(Calendar.MONTH) == 0 && current.get(Calendar.DATE) == 1) {
                // attempt to compress a year
                current.set(Calendar.DAY_OF_YEAR, current.getActualMaximum(Calendar.DAY_OF_YEAR));
                if (current.compareTo(end) <= 0) {
                    indices.add(
                            String.format("%s-%s%c*", index(), current.get(Calendar.YEAR), dateSeparator()));
                    current.add(Calendar.DATE, 1); // rollover to next year
                    continue;
                } else {
                    current.set(Calendar.DAY_OF_YEAR, 1); // rollback to first of the year
                }
            } else if (current.get(Calendar.DATE) == 1) {
                // attempt to compress a month
                current.set(Calendar.DATE, current.getActualMaximum(Calendar.DATE));
                if (current.compareTo(end) <= 0) {
                    indices.add(String.format("%s-%s%c%02d%c*", index(),
                            current.get(Calendar.YEAR), dateSeparator(),
                            current.get(Calendar.MONTH) + 1, dateSeparator()
                    ));
                    current.add(Calendar.DATE, 1); // rollover to next month
                    continue;
                } else {
                    current.set(Calendar.DATE, 1); // rollback to first of the month
                }
            }
            indices.add(formatTypeAndTimestamp(type,current.getTimeInMillis()));
            current.add(Calendar.DATE, 1);
        }
        return indices;
    }

    public static Calendar midnightUTC(long epochMillis) {
        Calendar result = Calendar.getInstance();
        result.setTimeInMillis(epochMillis);
        return result;
    }

    public String formatTypeAndTimestamp(@Nullable String type, long timestampMillis) {
        return prefix(type) + "-" + dateFormat().get().format(new Date(timestampMillis));
    }

    private String prefix(@Nullable String type) {
        return type != null ? index() + ":" + type : index();
    }

    // for testing
    public long parseDate(String timestamp) {
        try {
            return dateFormat().get().parse(timestamp).getTime();
        } catch (ParseException e) {
            throw new AssertionError(e);
        }
    }

    public String allIndices() {
        return index() + "-*";
    }
}
