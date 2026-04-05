package runtime.impl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;


class PropertiesImpl extends java.util.Properties {

    private static final runtime.Logger log = runtime.Logger.getLogger(RuntimeSystem.LoggerName);

    public static final String PROPERTIES_FILE = "application.properties";


    public String get(String key) { return key==null? null : (String) super.get(key); }

    public boolean match(String key, Function<String, Boolean> comperator) {
        return Optional.ofNullable(get(key))
            .map(value -> comperator != null? comperator.apply(value) : false).orElse(false);
    }


    void loadProperties(String propertiesFileName, String[] paths) {
        for(var path : paths) {
            switch(path) {
            case "CLASSPATH":
                try(InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream(propertiesFileName)) {
                    super.load(is);
                    LoggerImpl.configureStaticLoggers(this);
                    log.info(String.format("%s, loaded %d properties from CLASSPATH: '%s'", this.getClass().getSimpleName(), this.size(), propertiesFileName));
                    break;
                } catch (NullPointerException | IOException | IllegalArgumentException e) { } break;
            case "JAR":
                loadPropertiesFromJAR(propertiesFileName); break;
            default:
                String propertyFile = String.format("%s/%s", path, propertiesFileName);
                try (InputStream is = new FileInputStream(new File(propertyFile))) {
                    super.load(is);
                    LoggerImpl.configureStaticLoggers(this);
                    log.info(String.format("%s, loaded %d properties from file: '%s'", this.getClass().getSimpleName(), this.size(), propertyFile));
                    break;
                } catch (IOException e) { }
            }
            if(super.size() > 0) break;
        }
    }

    private void loadPropertiesFromJAR(String propertiesFileName) {
        String jarFileName = "";
        try (ZipInputStream zip = new ZipInputStream(new FileInputStream(new File(
                jarFileName = ClassScanner.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()))))
        {
            for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
                String propertyFile = entry.getName();
                if ( ! entry.isDirectory() && propertyFile.endsWith(propertiesFileName)) {
                    try(ZipFile zipFile = new ZipFile(jarFileName);
                        BufferedInputStream bis = new BufferedInputStream(zipFile.getInputStream(entry)))
                    {
                        super.load(bis);
                        LoggerImpl.configureStaticLoggers(this);
                        log.info(String.format("%s, loaded %d properties from file: '%s' in '%s'", this.getClass().getSimpleName(), this.size(), propertiesFileName, jarFileName.replaceAll(".*/", "")));
                        // 
                        // System.out.print("--------------------");
                        // int byteData;
                        // while ((byteData = bis.read()) != -1) {
                        //     System.out.print((char) byteData);
                        // }
                        // System.out.print("--------------------");
                    }
                }
            }
        } catch (IOException | URISyntaxException e) {
            log.error(String.format("%s, %s exception while reading .jar file '%s'", this.getClass().getSimpleName(), e.getClass().getSimpleName(), e.getMessage()));
        }
    }
}
