package com.gbft.framework.utils;

public class CustomBenchmarks {
    public static LogUtils benchmarkLogger = new LogUtils();
    public static int fileNum = 8282   ;

    public static int fileNum_TotalCommittedTnxs = 8000;
    public static LogUtils totalTnxsLogger = new LogUtils();

    private static long previousTimeMillis = System.currentTimeMillis();

    static{
        benchmarkLogger.CSVIntialize(fileNum);
        benchmarkLogger.CSVwrite("episode,protocol,architecture,throughput,duration");

        totalTnxsLogger.CSVIntialize(fileNum_TotalCommittedTnxs);
        totalTnxsLogger.CSVwrite("time_elapsed,TotalCommittedTransactions");
    }
    public static void LogBenchmark(String benchmark){
        benchmarkLogger.CSVwrite(benchmark);
    }

    public static void LogTotalCommittedTnxs(long transactionsCount){
        long timeDifferenceMillis = System.currentTimeMillis() - previousTimeMillis;
        long secondsElapsed = timeDifferenceMillis / (1000);
        totalTnxsLogger.CSVwrite( ""+ secondsElapsed + ","+transactionsCount);
    }

 

}