package com.gbft.framework.core.architecture;

import com.gbft.framework.core.Entity;
import com.gbft.framework.core.architecture.impls.*;
import com.gbft.framework.data.RequestData;

import java.util.HashMap;
import java.util.List;

public class ArchManager {

    public HashMap<String, Architecture> architectures;

    public String currentArchitectureKey;
    public Architecture currentArchitecture;

    private Entity entity;

    public ArchManager(Entity entity) {
        architectures = new HashMap<>();
        architectures.put("OX", new OXArchitecture(entity));
        architectures.put("XOV", new XOVArchitecture(entity));
        architectures.put("OXII", new OXIIArchitecture(entity));
        architectures.put("XOV++", new XOVppArchitecture(entity));
        architectures.put("XOV#", new XOVSerialArchitecture(entity));
        architectures.put("SXOV", new StreamXOVArchitecture(entity));

        currentArchitectureKey = "OX";


        this.entity = entity;
    }

    public Architecture getCurrentArchitecture() {
        return architectures.get(currentArchitectureKey);
    }

    public Architecture getArchitecture(String arch) {
        return architectures.get(arch);
    }

    public void setCurrentArchitecture(String arch) {
        currentArchitectureKey = arch;
        currentArchitecture = architectures.get(arch);
    }

    public OrderResponse performOrdering(List<RequestData> block) {
        return currentArchitecture.performOrdering(block);
    }

    public ValidatorResponse performValidation(List<RequestData> block) {
        return currentArchitecture.performValidation(block);
    }



}
