package com.gbft.framework.core.architecture.impls;

import com.gbft.framework.core.Entity;
import com.gbft.framework.core.architecture.BlockExecResponse;
import com.gbft.framework.core.architecture.OrderResponse;
import com.gbft.framework.core.architecture.impls.util.OXIIDependencyGraph;

public class OXIIArchitecture extends OXArchitecture {
    public OXIIArchitecture(Entity entity) {
        super(entity);
    }

    @Override
    //Default Block Execution : OXII Architecture
    public BlockExecResponse executeBlock(long seqNum){
        var block = getBlock(seqNum);

        OXIIDependencyGraph dependencyGraph = new OXIIDependencyGraph(entity, block);

        return new BlockExecResponse(
                dependencyGraph.success,
                new OrderResponse(dependencyGraph.success, dependencyGraph.block),
                null
        );
    }

}
