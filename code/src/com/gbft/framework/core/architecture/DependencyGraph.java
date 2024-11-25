package com.gbft.framework.core.architecture;

import com.gbft.framework.core.Entity;
import com.gbft.framework.data.RequestData;
import com.gbft.framework.data.RequestDataList;
import com.gbft.framework.utils.RequestUtils;
import com.gbft.framework.core.architecture.OXIIGraph;

import java.util.*;

public class DependencyGraph {
    public Entity entity;
    public List<RequestDataList> dag = new ArrayList<>();
    public OXIIGraph oxiiGraph = new OXIIGraph();
    public XOVGraph xovGraph = new XOVGraph();
    public DependencyGraph(Entity entity){
        this.entity = entity;
    }
    public List<RequestDataList> CreateGraph(List<RequestData> block){
        return oxiiGraph.processOXIIReordering(block);
    }

    public List<RequestDataList> earlyAbort(List<RequestData> block){
        List<RequestDataList> graph = new ArrayList<>();
//        this.entity.logger.write("before graph "+block);
        graph.add(xovGraph.processXOVReordering(block,false,true));
//        this.entity.logger.write("graph created ");
        return graph;
    }

    public void setDependencyGraph(List<RequestDataList> dag){
        this.dag = dag;
    }

    public List<RequestDataList> getDependencyGraph(){
        return dag;
    }

}
