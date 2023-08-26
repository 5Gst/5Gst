package ru.fivegst.speedtest.domain;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class LogMessage {
    private final String tag;
    private final String message;
    private final String stringifiedException;
    private final long logTimestampMillis;

    LogMessage(String tag, String message, Exception exception, long logTimestampMillis) {
        this.tag = tag;
        this.message = message;
        this.logTimestampMillis = logTimestampMillis;

        if (exception == null) {
            this.stringifiedException = null;
        } else {
            this.stringifiedException = "("
                    + exception.getClass().getSimpleName()
                    + ": "
                    + exception.getMessage()
                    + ")";
        }
    }

    public LogMessage(String tag, String message, Exception exception) {
        this(tag, message, exception, System.currentTimeMillis());
    }

    public static LogMessage fromFatalError(String message, Exception exception) {
        return new LogMessage("FatalError", message, exception);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LogMessage that = (LogMessage) o;
        return Objects.equals(tag, that.tag) &&
                Objects.equals(message, that.message) &&
                Objects.equals(stringifiedException, that.stringifiedException) &&
                Objects.equals(logTimestampMillis, that.logTimestampMillis);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tag, message, stringifiedException, logTimestampMillis);
    }

    @NonNull
    @Override
    public String toString() {
        SimpleDateFormat logDateFormat = new SimpleDateFormat(
                "HH:mm:ss.SSS z",
                Locale.getDefault(Locale.Category.FORMAT)
        );

        String formattedDate = logDateFormat.format(new Date(logTimestampMillis));

        if (stringifiedException == null) {
            return formattedDate +
                    "\n" + tag + ":" +
                    "\n" + message;
        } else {
            return formattedDate +
                    "\n" + tag + ":" +
                    "\n" + message +
                    "\n" + stringifiedException;
        }
    }
}
