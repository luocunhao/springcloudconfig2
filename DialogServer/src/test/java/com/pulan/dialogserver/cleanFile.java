package com.pulan.dialogserver;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class cleanFile {

    public static void main(String[] args) {

        try{
            FileReader fr = new FileReader("E://111.txt");
            BufferedReader bf = new BufferedReader(fr);
            String line = bf.readLine();
            String lastLine = "";
            List<String> list =new ArrayList<>();
            while (line != null){
                String nextLine = bf.readLine();
                if (nextLine !=null){
                    if(Pattern.compile("\\d{1}").matcher(line.substring(0,1)).matches()&& Pattern.compile("\\d{1}").matcher(nextLine.substring(0,1)).matches()){
                        list.add(line.replaceAll("\\n",""));
                        System.out.println(line.replaceAll("\\n",""));
                        line =nextLine;
                    }else{
                        line =line +nextLine;
                    }
                }else {
                   // line =null;
                    System.out.println(line.replaceAll("\\n",""));
                }
            }
            System.out.println("数组大小："+list.size());
//            readFromHdfs();
//            getDirectoryFromHdfs();
        }catch(Exception e){
            //System.out.println(e.getMessage());
        }
    }

    /**从HDFS上读取文件*/
   /** private static void readFromHdfs() {

        String dst = "hdfs://100.11.4.15:8020/ywpt/2017-11-13/hosts_2017-11-13.txt";
        Configuration conf = new Configuration();
        try {
            FileSystem fs = FileSystem.get(URI.create(dst), conf);
            FSDataInputStream hdfsInStream = fs.open(new Path(dst));
            OutputStream out = new FileOutputStream("c://luoly/hosts_2017-11-13.txt");
            byte[] ioBuffer = new byte[1024];
            int readLen = hdfsInStream.read(ioBuffer);
            while(-1 != readLen){
                out.write(ioBuffer, 0, readLen);
                readLen = hdfsInStream.read(ioBuffer);
            }
            out.close();
            hdfsInStream.close();
            fs.close();
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
    /**遍历HDFS上的文件和目录*/
   /** private static void getDirectoryFromHdfs() throws FileNotFoundException,IOException {
        String dst = "hdfs://100.11.4.15:8020/ywpt/2017-11-13";
        Configuration conf = new Configuration();
        FileSystem fs = FileSystem.get(URI.create(dst), conf);
        FileStatus fileList[] = fs.listStatus(new Path(dst));
        int size = fileList.length;
        for(int i = 0; i < size; i++){
            System.out.println("name:" + fileList[i].getPath().getName() );
        }
        fs.close();
    }*/
}
