package com.gbft.framework.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LogUtils{
    String st;
    String addr;

    HashMap<Integer,String> hm=new HashMap<>();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSSSSSSSS");

    private static String folderPath = "/home/msiddhu/Desktop/LOGS/";

    public static LogUtils errorLog = new LogUtils();
    public static LogUtils commonLog = new LogUtils();

    public LogUtils(){ }

    public LogUtils(int port){
        this.CoreIntialize(port);
    }


    static{
        errorLog.CoreIntialize(9999);
        commonLog.CoreIntialize(8888);
    }

    int portId;

    public void Intialize(int port){
        this.portId = port;
        String filePath = folderPath + port +".txt";
        this.addr = filePath;
        hm.put((port%10),filePath);
        try {
            File file = new File(filePath);
            boolean fileExists = file.exists();
            FileWriter fileWriter = new FileWriter(file,!file.exists());
            String text = "\nrewriting again new test 1234";
            if (fileExists) {
                fileWriter.close(); // Close the existing writer
                fileWriter = new FileWriter(filePath, false);
            }
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(" ");
            bufferedWriter.close();
        } catch (IOException e) {
            // Print an error message if an IOException occurs
            System.out.println("An error occurred while writing to the file: " + e.getMessage());
        }

    }
    public void CoreIntialize(int port){
        this.portId = port;
        String filePath = folderPath + port +".txt";
        this.addr = filePath;
        hm.put((port%10),filePath);
        try {
            File file = new File(filePath);
            boolean fileExists = file.exists();
            FileWriter fileWriter = new FileWriter(file,!file.exists());
            String text = "\nrewriting again new test 1234";
            if (fileExists) {
                fileWriter.close(); // Close the existing writer
                fileWriter = new FileWriter(filePath, false);
            }
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(" ");
            bufferedWriter.close();
        } catch (IOException e) {
            // Print an error message if an IOException occurs
            System.out.println("An error occurred while writing to the file: " + e.getMessage());
        }

    }
    public void PortWrite(int port,String s){
        this.st=s;
        int id = (port%10);
        write(id,s);
    }
    public void write(int id,String s){
        String addr;
        LocalDateTime currentTime = LocalDateTime.now();
        String formattedTime = currentTime.format(formatter);
        if(hm.getOrDefault(id,"-1").equals("-1")){
            addr = folderPath+"core/"+ id +".txt";
        }
        else{
            addr= hm.get(id);
        }

        String filePath = addr;
        //String filePath = "/Users/sai/Desktop/log/output.txt";
        try {
            File file = new File(filePath);
            boolean fileExists = file.exists();
            FileWriter fileWriter = new FileWriter(file,file.exists());
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write("\n"+s +" ["+formattedTime+"]");
            bufferedWriter.close();
            //System.out.println("Text has been written to the file successfully.");
        } catch (IOException e) {
            // Print an error message if an IOException occurs
            //System.out.println("An error occurred while writing to the file: " + e.getMessage());
        }
    }

    public void write(String s){
        this.write(portId, s);
    }

    public void errors(String s){
        errorLog.write(s);
    }

    public static void LogError(String s){
        errorLog.write(s);
    }

    public static void LogCommon(String s){
        commonLog.write(s);
    }


    public void CSVIntialize(int port){
        this.portId = port;
        String filePath = folderPath + "csv/test/" + port +".csv";
        hm.put((port%10),filePath);
        this.addr = filePath;
        try {
            File file = new File(filePath);
            boolean fileExists = file.exists();
            FileWriter fileWriter = new FileWriter(file,!file.exists());
            String text = "\nrewriting again new test 1234";
            if (fileExists) {
                fileWriter.close(); // Close the existing writer
                fileWriter = new FileWriter(filePath, false);
            }
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write("");
            bufferedWriter.close();
        } catch (IOException e) {
            // Print an error message if an IOException occurs
            System.out.println("An error occurred while writing to the file: " + e.getMessage());
        }

    }

    public void CSVwrite(String s){
        String addr;
        LocalDateTime currentTime = LocalDateTime.now();
        String formattedTime = currentTime.format(formatter);

        String filePath = this.addr;
        //String filePath = "/Users/sai/Desktop/log/output.txt";
        try {
            File file = new File(filePath);
            boolean fileExists = file.exists();
            FileWriter fileWriter = new FileWriter(file,file.exists());
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            // bufferedWriter.write("\n"+s +" ["+formattedTime+"]");
            bufferedWriter.write("\n"+s );
            bufferedWriter.close();
            //System.out.println("Text has been written to the file successfully.");
        } catch (IOException e) {
            // Print an error message if an IOException occurs
            //System.out.println("An error occurred while writing to the file: " + e.getMessage());
        }
    }
}