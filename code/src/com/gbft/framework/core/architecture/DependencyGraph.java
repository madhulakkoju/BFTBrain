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
    public DependencyGraph(Entity entity){
        this.entity = entity;
    }
    public List<RequestDataList> CreateGraph(List<RequestData> block){
        return oxiiGraph.processOXIIReordering(block);
//        dag = new ArrayList<>();
//        Map<Integer,List<RequestData>> reqList = new HashMap<>();
//        for(int i=0;i<block.size();i++){
//            RequestData req = block.get(i);
//            List<RequestData> l = reqList.getOrDefault(RequestUtils.getRecord(req),new ArrayList<>());
//            l.add(req);
//            reqList.put(RequestUtils.getRecord(req),new ArrayList<>(l));
//        }
//        for(Map.Entry<Integer,List<RequestData>> entry : reqList.entrySet()){
//            var builder = RequestDataList.newBuilder();
//            builder.addAllReqDataList(entry.getValue());
//            dag.add(builder.build());
//        }
//        return dag;
    }

    public void setDependencyGraph(List<RequestDataList> dag){
        this.dag = dag;
    }

    public List<RequestDataList> getDependencyGraph(){
        return dag;
    }

}
