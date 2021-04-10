package com.anatawa12.crashReportPublisher;

import java.net.URI;

public class CrashReportPublisherTest {
    public static void main(String[] args) throws Throwable {
        new DiscordSender(URI.create(webhook))
                .report("some.txt", "crash-report\n");
    }
    private static String webhook = "<your url>";
}
