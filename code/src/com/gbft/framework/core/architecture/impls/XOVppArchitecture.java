package com.gbft.framework.core.architecture.impls;

import com.gbft.framework.core.Entity;
import com.gbft.framework.core.architecture.OrderResponse;
import com.gbft.framework.core.architecture.impls.util.XOVppDependencyGraph;
import com.gbft.framework.data.RequestData;
import com.gbft.framework.utils.MiscUtils;

import java.util.List;

public class XOVppArchitecture extends XOVArchitecture {
    public XOVppArchitecture(Entity entity) {
        super(entity);
    }


    @Override
    public OrderResponse performOrdering(List<RequestData> block){
        //Perform Ordering of the block
        //Update this with the actual ordering logic

        block.sort((o1, o2) -> {
            var o1Time = MiscUtils.fromGoogleTimestampUTC(o1.getTimestamp());
            var o2Time = MiscUtils.fromGoogleTimestampUTC(o2.getTimestamp());
            return o1Time.compareTo(o2Time);
        });

        XOVppDependencyGraph dependencyGraph = new XOVppDependencyGraph(block);
        if(!dependencyGraph.success){
            return new OrderResponse(false, block);
        }
        else{
            block = dependencyGraph.getOrderedBlock();
        }

        //TODO: Perform Consensus Here

        return new OrderResponse(true, block);
    }

}
