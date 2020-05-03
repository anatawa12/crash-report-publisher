import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class RuntimeLibsLauncher {
    private static boolean verboseEnabled;
    private static File temp;

    public static void main(String[] args) throws URISyntaxException, IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        args = isVerbose(args);
        {
            int version = getVersion();
            verbose("java version: %d", version);
            if (version < 8)
                throw new IllegalStateException("Java 8 or higher is required");
        }
        temp = createTempDirectory();
        verbose("temp dir: %s", temp);

        ClassLoader loader;

        if (!libraryContainsInClasspath()) {
            File thisClassPath = new File(RuntimeLibsLauncher.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            verbose("this classpath: %s", thisClassPath);
            List<URL> runtimeJars = putJarsToTemp(getRuntimeJars(thisClassPath));

            runtimeJars.add(0, thisClassPath.toURI().toURL());

            verbose("making URLClassLoader with urls:");
            if (verboseEnabled) {
                for (URL runtimeJar : runtimeJars) {
                    verbose(runtimeJar.toString());
                }
            }

            loader = new URLClassLoader(runtimeJars.toArray(new URL[0]), null);
        } else {
            verbose("not adding classpath is required");
            loader = RuntimeLibsLauncher.class.getClassLoader();
        }

        try {
            loader.loadClass("com.anatawa12.crashReportPublisher.CrashReportPublisherKt")
                    .getMethod("main", String[].class)
                    .invoke(null, (Object) args);
        } finally {
            for (File file : temp.listFiles()) {
                file.delete();
            }
            temp.delete();
        }
    }

    private static List<URL> putJarsToTemp(List<URL> runtimeJars) throws IOException {
        List<URL> files = new ArrayList<>();
        for (URL runtimeJar : runtimeJars) {
            verbose("added to temp dir: %s", runtimeJar);
            File file = new File(temp, fileName(runtimeJar.getPath()));
            copy(runtimeJar.openStream(), new FileOutputStream(file));
            files.add(file.toURI().toURL());
        }
        return files;
    }

    private static void copy(InputStream from, OutputStream to) throws IOException {
        byte[] buf = new byte[1024];
        int size;
        while ((size = from.read(buf)) != -1) {
            to.write(buf, 0, size);
        }
    }

    private static String fileName(String path) {
        int index = path.lastIndexOf('/');
        return path.substring(index + 1);
    }

    private static void verbose(String format, Object... args) {
        if (verboseEnabled)
            System.err.println(String.format(format, args));
    }

    private static String[] isVerbose(String[] args) {
        int verboseI = -1;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--runtime-libs-launcher-verbose")) {
                verboseEnabled = true;
                verboseI = i;
                break;
            }
            if (args[i].equals("--runtime-libs-launcher-help")) {
                System.err.println("runtime libs launcher options:");
                System.err.println("    --runtime-libs-launcher-verbose    verbose log for launcher");
                System.err.println("    --runtime-libs-launcher-help       show this message");
                System.exit(0);
            }
        }
        if (verboseEnabled) {
            String[] newArgs = new String[args.length - 1];
            System.arraycopy(args, 0, newArgs, 0, verboseI);
            System.arraycopy(args, verboseI + 1, newArgs, verboseI, newArgs.length - verboseI);
            return newArgs;
        }
        return args;
    }

    private static int getVersion() {
        String version = System.getProperty("java.version");
        if(version.startsWith("1.")) {
            version = version.substring(2, 3);
        } else {
            int dot = version.indexOf(".");
            if(dot != -1) { version = version.substring(0, dot); }
        } return Integer.parseInt(version);
    }

    private static boolean libraryContainsInClasspath() {
        try {
            Class.forName("kotlin.Unit");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static File createTempDirectory() throws IOException {
        final File temp;

        temp = File.createTempFile("temp", Long.toString(System.nanoTime()));

        if (!temp.delete()) {
            throw new IOException("Could not delete temp file: " + temp.getAbsolutePath());
        }

        if (!temp.mkdir()) {
            throw new IOException("Could not create temp directory: " + temp.getAbsolutePath());
        }

        return temp;
    }

    private static List<URL> getRuntimeJars(File thisClassPath) throws IOException {
        List<URL> urls = new ArrayList<>();
        if (thisClassPath.isFile()) {
            verbose("this classpath is jar file");
            ZipFile zipFile = new ZipFile(thisClassPath);
            for (ZipEntry entry : iterable(zipFile.stream().iterator())) {
                if (!entry.getName().startsWith("runtime-libs/")) continue;
                if (!entry.getName().endsWith(".jar")) continue;
                verbose("found runtime libs: %s", entry.getName());
                urls.add(new URL("jar:" + thisClassPath.toURI().toURL() + "!/" + entry.getName()));
            }
        } else {
            verbose("this classpath is directory");
            for (File file : new File(thisClassPath, "runtime-libs").listFiles()) {
                if (!file.getName().endsWith(".jar")) continue;
                verbose("found runtime libs: %s", file);
                urls.add(file.toURI().toURL());
            }
        }
        return urls;
    }

    public static <E> Iterable<E> iterable(final Iterator<E> iterable) {
        return () -> iterable;
    }
}
