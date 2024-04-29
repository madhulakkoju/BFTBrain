package com.gbft.framework.utils;

import com.gbft.framework.utils.LogUtils;

public class CustomBenchmarks {
    public static LogUtils benchmarkLogger = new LogUtils();
    public static int fileNum = 8282   ;

    static{
        benchmarkLogger.CSVIntialize(fileNum);
        benchmarkLogger.CSVwrite("episode,protocol,architecture,throughput,duration");
    }
    public static void LogBenchmark(String benchmark){
        benchmarkLogger.CSVwrite(benchmark);

    }



}