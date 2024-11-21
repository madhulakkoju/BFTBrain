package com.gbft.framework.core.architecture;

import com.gbft.framework.core.Entity;
import com.gbft.framework.data.MessageData;
import com.gbft.framework.data.RequestData;
import com.gbft.framework.statemachine.StateMachine;


import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class ArchManager {

//    public HashMap<String, Architecture> architectures;

    public String currentArchitectureKey;

    private Entity entity;

    public HashSet<String> architectures;

    public ArchManager(Entity entity) {
        architectures = (HashSet<String>) Set.of("OX", "OXII", "XOV", "XOV++");

        currentArchitectureKey = "XOV";
        this.entity = entity;
    }

    public void setCurrentArchitecture(String arch) {
        currentArchitectureKey = arch;
    }

    public MessageData createEndorsedMessageToClient(MessageData oldMessage, List<RequestData> requests){
        //Create a new message to be sent to the client
        //Update this with the actual logic
        var targetClients = StateMachine.roles.indexOf("client");

        var clients = this.entity.getRolePlugin().getRoleEntities(
                oldMessage.getSequenceNum(),
                oldMessage.getViewNum(),
                StateMachine.NORMAL_PHASE,
                targetClients);

        var newMessage = this.entity.createMessage(
                oldMessage.getSequenceNum(),
                oldMessage.getViewNum(),
                requests,
                oldMessage.getMessageType(),
                entity.getId(),
                clients
        );
        newMessage = newMessage.toBuilder().setXovState(2)
                .setIsEndorsementRequest(true)
                .build();

        return newMessage;
    }


    public String getCurrentArchitectureKey() {
        return currentArchitectureKey;
    }

    public void setCurrentArchitectureKey(String currentArchitectureKey) {
        this.currentArchitectureKey = currentArchitectureKey;
    }

    public Entity getEntity() {
        return entity;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    public HashSet<String> getArchitectures() {
        return architectures;
    }

    public void setArchitectures(HashSet<String> architectures) {
        this.architectures = architectures;
    }
}
