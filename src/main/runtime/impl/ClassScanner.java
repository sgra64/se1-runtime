package runtime.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


class ClassScanner {

    private final runtime.Logger log = runtime.Logger.getLogger(RuntimeSystem.LoggerName);

    private final Function<String, String> classPathMapper = (cls) -> cls
        .replaceAll("\\\\", "/")        // backslashes (Windows) to '/' (Unix)
        .replaceAll("/", ".")           // '/' to '.' (packages)
        .replaceAll(".class$", "");     // remove trailing '.class'

    void scanClasses(List<Class<?>> collect) {
        // 
        // attempt to scan classes from the file system
        List<String> classPaths = new ArrayList<>();
        int len = 0;
        for(final String path : new String[] {
            "target/classes"
        }) {
            File fd = new File(path);
            if(fd.exists()) {
                var pathLen = fd.getAbsolutePath().length() + 1;
                len = scanClassesFromFileSystem(classPaths, fd, pathLen);
                if(len > 0) {
                    log.info(String.format("%s, found %d classes from path: '%s'", this.getClass().getSimpleName(), len, path));
                    break;  // exit loop
                }
            }
        }
        if(len==0) {
            // alternative attempt to scan classes as resources from the Java Class Loader
            String path = ".";
            int pathLen = path.length() + 1;
            len = scanClassesByClassLoader(classPaths, path, pathLen);
            if(len > 0) {
                log.info(String.format("%s, found %d classes from Java Class Loader", this.getClass().getSimpleName(), len));
            }
        }
        if(len==0) {
            // alternative attempt to scan classes from a JAR file
            String jarFileName = scanClassesFromJar(classPaths);
            len = classPaths.size();
            if(len > 0) {
                log.info(String.format("%s, found %d classes from: '%s'", this.getClass().getSimpleName(), len, jarFileName.replaceAll(".*/", "")));
            }
        }
        if(len > 0) {
            // load class names and convert to classes
            classPaths.stream()
                .filter(cls -> ! cls.matches("(module-info|.*package[-_]info.*)"))
                .map(cls -> {
                    Optional<Class<?>> empty = Optional.empty();
                    try {
                        Class<?> clazz = Class.forName(cls);
                        if( ! clazz.toString().startsWith("interface")) {
                            return Optional.of(clazz);
                        }
                    } catch (ClassNotFoundException | NoClassDefFoundError e) {
                        log.error(String.format("%s: %s while scanning classes, %s", this.getClass().getSimpleName(), e.getClass().getSimpleName(), e.getMessage()));
                    }
                    return empty;
                })
                .flatMap(Optional::stream)
                .collect(Collectors.toCollection(() -> collect));
            // 
            log.info(String.format("%s, loaded %d classes", this.getClass().getSimpleName(), collect.size()));
        } else {
            log.error(String.format("%s 0 classes scanned", this.getClass().getSimpleName()));
        }
    }

    private int scanClassesFromFileSystem(List<String> collect, File dir, int startLen) {
        if(dir.isDirectory()) {
            for(File f : dir.listFiles()) {
                if(f.isDirectory()) {
                    scanClassesFromFileSystem(collect, f, startLen);
                } else {
                    String clsPath = classPathMapper.apply(f.getAbsolutePath().substring(startLen));
                    collect.add(clsPath);
                }
            }
        }
        return collect.size();
    }

    private int scanClassesByClassLoader(List<String> collect, String path, int startLen) {
        try(InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream(path)) {
            // 
            if(is != null) {
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String line;
                while((line = br.readLine()) != null) {
                    if(line.endsWith(".class")) {
                        String clsPath = classPathMapper.apply((path + "/" + line).substring(startLen));
                        collect.add(clsPath);
                    } else {
                        scanClassesByClassLoader(collect, path + "/" + line, startLen);
                    }
                }
            }
        } catch (NullPointerException | IOException e) {
            log.error(String.format("%s while scanning classes, %s", e.getClass().getSimpleName(), e.getMessage()));
        }
        return collect.size();
    }

    private String scanClassesFromJar(List<String> collect) {
        String jarFileName = "";
        try (ZipInputStream zip = new ZipInputStream(new FileInputStream(new File(
                jarFileName = ClassScanner.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()))))
        {
            for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
                if ( ! entry.isDirectory() && entry.getName().endsWith(".class")) {
                    String clsPath = entry.getName().replace('/', '.'); // including ".class"
                    collect.add(classPathMapper.apply(clsPath));
                }
            }
        } catch (IOException | URISyntaxException e) {
            log.error(String.format("%s: %s while scanning classes, %s", this.getClass().getSimpleName(), e.getClass().getSimpleName(), e.getMessage()));
        }
        return jarFileName;
    }
}
