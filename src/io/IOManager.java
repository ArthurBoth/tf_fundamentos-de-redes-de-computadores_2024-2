package io;

import constants.ConfigurationConstants;
import io.fileIO.FileIO;

public class IOManager {

    public IOManager() {
    }

    public String[] getDefaultRoutes() {
        return FileIO.read(ConfigurationConstants.CONFIG_FILE_PATH +
                ConfigurationConstants.DEFAULT_ROUTES_FILE);
    }
}
