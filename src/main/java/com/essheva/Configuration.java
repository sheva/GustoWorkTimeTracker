package com.essheva;


import lombok.AccessLevel;
import lombok.Getter;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class Configuration {

    private static final String RESOURCE_DIR_PATH = "src/main/resources";
    private static final String BI_WEEKLY_STATUS = "BiWeeklyStatus.xlsx";
    private static final String USER_LOGIN = "secret.properties";

    @Getter
    final Path driverPath;

    @Getter(AccessLevel.PACKAGE)
    final Path statusFilePath;

    @Getter(AccessLevel.PACKAGE)
    final String userEmail;

    @Getter(AccessLevel.PACKAGE)
    final String userPassword;

    Configuration() throws IOException {
        this.driverPath = getWebDriverFolderPathByOS();
        this.statusFilePath = Paths.get(RESOURCE_DIR_PATH, BI_WEEKLY_STATUS);

        Properties userSecret = loadPropertiesFromFile();
        userEmail = getValue(userSecret, "user_email");
        userPassword = getValue(userSecret, "user_password");
    }

    private static Path getWebDriverFolderPathByOS() {
        String os = System.getProperty("os.name").toLowerCase();
        final String dirName;
        if (os.contains("win")) {
            dirName = "win32";
        } else if (os.contains("mac")) {
            dirName = "mac64";
        } else if (os.contains("nux")) {
            dirName = "linux64";
        } else {
            throw new UnsupportedOperationException(os + " is not supported");
        }
        return Paths.get(RESOURCE_DIR_PATH, "chomedriver", dirName, "chromedriver");
    }

    private String getValue(Properties props, String s)  {
        final String value = props.getProperty(s);
        if (value == null) {
            throw new IllegalArgumentException("Property not set " + s);
        }
        return value;
    }

    private static FileReader getReader() throws FileNotFoundException {
        return new FileReader(Paths.get(RESOURCE_DIR_PATH, Configuration.USER_LOGIN).toFile());
    }

    private static Properties loadPropertiesFromFile() throws IOException {
        Properties props = new Properties();
        props.load(getReader());
        return props;
    }
}
