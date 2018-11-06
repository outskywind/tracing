package com.dafy.skye.zipkin;

import zipkin2.internal.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by quanchengyun on 2018/10/10.
 */
public class IndexNameFormatter  {

    private final String index;
    private final char dateSeparator;
    private final ThreadLocal<SimpleDateFormat> dateFormat;
    //private static final TimeZone UTC8 = TimeZone.getTimeZone("UTC+8:00");

    private IndexNameFormatter(
            String index,
            char dateSeparator,
            ThreadLocal<SimpleDateFormat> dateFormat) {
        this.index = index;
        this.dateSeparator = dateSeparator;
        this.dateFormat = dateFormat;
    }


    public String index() {
        return index;
    }


    public char dateSeparator() {
        return dateSeparator;
    }


    ThreadLocal<SimpleDateFormat> dateFormat() {
        return dateFormat;
    }


    public static Builder newBuilder() {
        return new IndexNameFormatter.Builder();
    }

    public List<String> formatTypeAndRange(@Nullable String type, long beginMillis, long endMillis) {
        GregorianCalendar current = midnight(beginMillis);
        GregorianCalendar end = midnight(endMillis);
        if (current.equals(end)) {
            return Collections.singletonList(formatTypeAndTimestamp(type, current.getTimeInMillis()));
        }

        String prefix = prefix(type);
        List<String> indices = new ArrayList<>();
        while (current.compareTo(end) <= 0) {
            if (current.get(Calendar.MONTH) == 0 && current.get(Calendar.DATE) == 1) {
                // attempt to compress a year
                current.set(Calendar.DAY_OF_YEAR, current.getActualMaximum(Calendar.DAY_OF_YEAR));
                if (current.compareTo(end) <= 0) {
                    indices.add(
                            String.format("%s-%s%c*", prefix, current.get(Calendar.YEAR), dateSeparator()));
                    current.add(Calendar.DATE, 1); // rollover to next year
                    continue;
                } else {
                    current.set(Calendar.DAY_OF_YEAR, 1); // rollback to first of the year
                }
            } else if (current.get(Calendar.DATE) == 1) {
                // attempt to compress a month
                current.set(Calendar.DATE, current.getActualMaximum(Calendar.DATE));
                if (current.compareTo(end) <= 0) {
                    indices.add(String.format("%s-%s%c%02d%c*", prefix,
                            current.get(Calendar.YEAR), dateSeparator(),
                            current.get(Calendar.MONTH) + 1, dateSeparator()
                    ));
                    current.add(Calendar.DATE, 1); // rollover to next month
                    continue;
                } else {
                    current.set(Calendar.DATE, 1); // rollback to first of the month
                }
            }
            indices.add(formatTypeAndTimestamp(type, current.getTimeInMillis()));
            current.add(Calendar.DATE, 1);
        }
        return indices;
    }

    static GregorianCalendar midnight(long epochMillis) {
        GregorianCalendar result = new GregorianCalendar();
        Calendar day = Calendar.getInstance();
        day.setTimeInMillis(epochMillis);
        day.set(Calendar.MILLISECOND, 0);
        day.set(Calendar.SECOND, 0);
        day.set(Calendar.MINUTE, 0);
        day.set(Calendar.HOUR_OF_DAY, 0);
        result.setTimeInMillis(day.getTimeInMillis());
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

    public String formatType(@Nullable String type) {
        return prefix(type) + "-*";
    }




    @Override
    public String toString() {
        return "IndexNameFormatter{"
                + "index=" + index + ", "
                + "dateSeparator=" + dateSeparator + ", "
                + "dateFormat=" + dateFormat
                + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof IndexNameFormatter) {
            IndexNameFormatter that = (IndexNameFormatter) o;
            return (this.index.equals(that.index()))
                    && (this.dateSeparator == that.dateSeparator())
                    && (this.dateFormat.equals(that.dateFormat()));
        }
        return false;
    }

    @Override
    public int hashCode() {
        int h = 1;
        h *= 1000003;
        h ^= this.index.hashCode();
        h *= 1000003;
        h ^= this.dateSeparator;
        h *= 1000003;
        h ^= this.dateFormat.hashCode();
        return h;
    }


    public static final class Builder {
        private String index;
        private Character dateSeparator;
        private ThreadLocal<SimpleDateFormat> dateFormat;

        private final String DAILY_INDEX_FORMAT = "yyyy-MM-dd";
        Builder() {
        }

        public final IndexNameFormatter build() {
            return dateFormat(new ThreadLocal<SimpleDateFormat>() {
                @Override protected SimpleDateFormat initialValue() {
                    SimpleDateFormat result =
                            new SimpleDateFormat(DAILY_INDEX_FORMAT.replace('-', dateSeparator()));
                    //result.setTimeZone(TimeZone.getTimeZone("UTC"));
                    return result;
                }
            }).autoBuild();
        }

        public Builder index(String index) {
            if (index == null) {
                throw new NullPointerException("Null index");
            }
            this.index = index;
            return this;
        }

        public Builder dateSeparator(char dateSeparator) {
            this.dateSeparator = dateSeparator;
            return this;
        }

        public char dateSeparator() {
            if (dateSeparator == null) {
                throw new IllegalStateException("Property \"dateSeparator\" has not been set");
            }
            return dateSeparator;
        }

        public Builder dateFormat(ThreadLocal<SimpleDateFormat> dateFormat) {
            if (dateFormat == null) {
                throw new NullPointerException("Null dateFormat");
            }
            this.dateFormat = dateFormat;
            return this;
        }

        public IndexNameFormatter autoBuild() {
            String missing = "";
            if (this.index == null) {
                missing += " index";
            }
            if (this.dateSeparator == null) {
                missing += " dateSeparator";
            }
            if (this.dateFormat == null) {
                missing += " dateFormat";
            }
            if (!missing.isEmpty()) {
                throw new IllegalStateException("Missing required properties:" + missing);
            }
            return new IndexNameFormatter(
                    this.index,
                    this.dateSeparator,
                    this.dateFormat);
        }
    }
}
