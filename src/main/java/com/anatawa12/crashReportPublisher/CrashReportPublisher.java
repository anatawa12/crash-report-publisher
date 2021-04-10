package com.anatawa12.crashReportPublisher;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Properties;

public final class CrashReportPublisher {
    private static Logger LOGGER = LogManager.getLogger(CrashReportPublisher.class);
    private static IMessageSender reporter;

    private CrashReportPublisher() {}

    public static void loadConfig(File gameDir) {
        File propsFile = new File(gameDir, "config/crash-report-publisher.properties");
        Properties props = new Properties();
        FileReader reader = null;
        try {
            reader = new FileReader(propsFile);
            props.load(reader);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null)
                    reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        String service = getConfig(props, "service-kind");
        if ("discord".equals(service)) {
            reporter = new DiscordSender(URI.create(getConfig(props, "hook-url")));
        } else {
            throw new IllegalStateException("unknown service-kind found: " + service);
        }
    }

    private static String getConfig(Properties props, String key) {
        String value = props.getProperty(key);
        if (value == null)
            throw new IllegalStateException(key + " not found in config/crash-report-publisher.properties!");
        return value;
    }

    @SuppressWarnings("unused")
    public static void onSaveToFile(File crashReportFile, File toFile, String body) {
        if (crashReportFile != null) return;
        try {
            if (reporter != null)
                reporter.report(toFile.getName(), body);
        } catch (Throwable throwable) {
            LOGGER.error("Could not send crash report to {}", reporter, throwable);
        }
    }

    public static String toUnsignedString(long i) {
        if (i >= 0) {
            return Long.toString(i);
        } else {
            // quot = i / 10
            long quot = (i >>> 1) / 5;
            long rem = i - quot * 10;
            return Long.toString(quot) + rem;
        }
    }

    public static String escapeStr(String s) {
        StringBuilder builder = new StringBuilder();
        int start = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c < '\u001F' || c == '"' || c == '\\') {
                builder.append(s, start, i);
                builder.append("\\u");
                hexStr(builder, c);
                start = i + 1;
            }
        }
        builder.append(s, start, s.length());
        return builder.toString();
    }

    private static void hexStr(StringBuilder b, int c) {
        b.append(hexChars[c >>> 12 & 0xff]);
        b.append(hexChars[c >>> 8 & 0xff]);
        b.append(hexChars[c >>> 4 & 0xff]);
        b.append(hexChars[c & 0xff]);
    }

    private static final char[] hexChars = "0123456789abcdef".toCharArray();

    public static String readToString(InputStream inputStream) throws IOException {
        StringWriter writer = new StringWriter();
        BufferedReader reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(inputStream)));

        char[] buffer = new char[8 * 1024];
        int chars = reader.read(buffer);
        while (chars >= 0) {
            writer.write(buffer, 0, chars);
            chars = reader.read(buffer);
        }

        return writer.toString();
    }

    public static InputStream getIS(HttpURLConnection conn) {
        try {
            return conn.getInputStream();
        } catch (IOException ignored) {
            return conn.getErrorStream();
        }
    }
}

interface IMessageSender {
    void report(String name, String body) throws Throwable;
}

final class DiscordSender implements IMessageSender {
    private final URI hookUrl;

    DiscordSender(URI hookUrl) {
        this.hookUrl = hookUrl;
    }

    @Override
    public void report(String name, String body) throws Throwable {
        HttpURLConnection conn = (HttpURLConnection) hookUrl.toURL().openConnection();
        try {
            HttpEntity entity = MultipartEntityBuilder.create()
                    .setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
                    .addTextBody("payload_json",
                            "{\"content\":\"" + CrashReportPublisher.escapeStr("Crash Report " + name) + "\"}",
                            ContentType.TEXT_PLAIN
                    )
                    .addBinaryBody(
                            "file",
                            body.getBytes(UTF_8),
                            ContentType.TEXT_PLAIN,
                            name
                    )
                    .build();

            conn.setDoOutput(true);  //POST可能にする
            conn.setRequestMethod("POST");

            conn.setRequestProperty("Content-Type", entity.getContentType().getValue());
            conn.setRequestProperty("User-Agent", "CrashReportPublisher/2.0");

            conn.connect();

            BufferedOutputStream out = new BufferedOutputStream(conn.getOutputStream());
            try {
                entity.writeTo(out);
            } finally {
                out.close();
            }
            if (conn.getResponseCode() < 200 || 299 < conn.getResponseCode())
                throw new IllegalStateException("failed to upload: " + conn.getResponseCode() + ": " + hookUrl + "\n"
                        + CrashReportPublisher.readToString(CrashReportPublisher.getIS(conn)));
        } finally {
            conn.disconnect();
        }
    }

    public static final Charset UTF_8 = Charset.forName("UTF-8");
}
