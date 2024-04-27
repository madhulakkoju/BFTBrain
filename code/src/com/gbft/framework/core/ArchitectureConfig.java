package com.gbft.framework.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import com.gbft.framework.utils.ConfigObject;
import com.gbft.framework.utils.LogUtils;

public class ArchitectureConfig {

    public static Map<String, String> configContent;

    public static List<String> availableArchitectures;
    static{

        try {

            configContent = new HashMap<>();

            var archConfig = Files.readString(Path.of("../config/config.architectures.yaml"));
            var architecturePool = new ConfigObject(archConfig, "").stringList("switching.protocol-pool");

            availableArchitectures = architecturePool;

            configContent.put("arch", archConfig);

           LogUtils.LogCommon("Architecuture Pool: \n"+architecturePool.toString());
        } catch (IOException e) {
            System.err.println("Error reading config files.");
            LogUtils.LogError("Error reading config files." + e.getMessage());
            System.exit(1);
        } catch(Exception e) {
            LogUtils.LogError("Error :" + e.getMessage());
            System.exit(1);
        }
    }


}
