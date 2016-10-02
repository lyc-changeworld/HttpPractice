package com.example.achuan.httppractice_0;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by achuan on 16-10-2.
 * 功能：实现多线程并发下载文件
 */
public class DownLoad {

    private Handler handler;//全局处理者,属于整个类
    //我们将在创建类对象的时候将处理者实例传递到类中,方便方法中使用
    public DownLoad(Handler handler) {
        this.handler = handler;
    }
    //创建线程池,并设置最大线程并发数为３
    private  Executor threadPool= Executors.newFixedThreadPool(3);

    //创建一个下载进程类
     class DownLoadRunnable implements Runnable {
        private String httpUrl;//网络请求链接
        private String pathName;//进行存储数据的文件的绝对路径
        private long start;//起始位置
        private long end;//结束位置
        private Handler handler;
        //构造方法
        public DownLoadRunnable(String httpUrl, String pathName, long start, long end
        ,Handler handler) {
            this.httpUrl = httpUrl;
            this.pathName = pathName;
            this.start = start;
            this.end = end;
            this.handler=handler;
        }
        @Override
        public void run() {
            HttpURLConnection connection=null;
            RandomAccessFile accessFile=null;
            InputStream inputStream=null;
            try{
                URL url=new URL(httpUrl);//新建一个网络地址
                connection= (HttpURLConnection) url.openConnection();//打开网址链接
                //设置HTTP请求
                connection.setRequestMethod("GET");//配置请求方式为：获取数据
                connection.setConnectTimeout(8000);//设置连接超时时间
                connection.setReadTimeout(8000);//设置读取超时时间

                //设置请求头信息中的"范围"信息,获取流信息
                connection.setRequestProperty("Range","bytes="+start+"-"+end);
                //设置资源吸入的位置
                accessFile=new RandomAccessFile(new File(pathName),"rwd");
                accessFile.seek(start);//跳转到start位置开始读写
                inputStream=connection.getInputStream();//获取服务器返回的输入流

                byte[] b=new byte[4*1024];//创建一个长度为4*1024的"竹筒"
                int len;//保存实际读取的字节数目
                //循环重复"取水"将输入流引入到输出的存储文件中
                //read()方法是从输入流中读取单个字节,相当于从"竹筒"中取出一滴水
                while ((len=inputStream.read(b))!=-1) {
                        //从"竹筒"中取出一滴水
                        accessFile.write(b,0,len);
                }
                Message message=new Message();
                message.what=1;
                handler.sendMessage(message);
            }catch (Exception e)
            {
                e.printStackTrace();
            }finally {
                if(connection!=null)
                {
                    connection.disconnect();//关闭HTTP连接
                }
                //将输出流关闭
                if(accessFile!=null) {
                    try {
                        accessFile.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                //关闭输入流
                if(inputStream!=null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    //同时执行多个线程来实现并发下载一个文件
    public  void downLoadFile(String HttpUrl)
    {
        HttpURLConnection connection=null;
        try{
            URL url=new URL(HttpUrl);//新建一个网络地址
            connection= (HttpURLConnection) url.openConnection();//打开网址链接
            //设置HTTP请求
            connection.setRequestMethod("GET");//配置请求方式为：获取数据
            connection.setConnectTimeout(8000);//设置连接超时时间
            connection.setReadTimeout(8000);//设置读取超时时间

            String fileName=getFileName(HttpUrl);//设置文件的名称
            //声明一个引用变量指向外部sd卡存储路径
            File parent=Environment.getExternalStorageDirectory();
            //新建一个文件对象来存储网络请求返回的数据
            File downloadFile=new File(parent,fileName);//参数介绍：文件生成的路径和文件名

             //获取资源的总的长度
            int count=connection.getContentLength();
            //计算每个线程需要执行的下载的长度
            int block=count/3;
            //开始创建多个线程进行并发下载操作
            for (int i = 0; i <3 ; i++) {
                //先计算每个线程执行的起始和终止位置
                long start=i*block;
                long end=(i+1)*block-1;
                if(i==2)
                {
                    end=count;
                }
                //创建一个线程来执行下载操作,包含文件访问的路径\起始和末尾位置
                DownLoadRunnable runnable=new DownLoadRunnable(HttpUrl,
                        downloadFile.getAbsolutePath(),start,end,handler);
                //Log.d("achuan",downloadFile.getAbsolutePath());
                threadPool.execute(runnable);//通过线程池来进行线程的控制和启动等操作
            }
        }catch (Exception e)
        {

        }finally {
            if(connection!=null)
            {
                connection.disconnect();//关闭HTTP连接
            }
        }
    }

    //设置存储的文件的文件名称
    public  String getFileName(String url)
    {
        //获取url中最后的/后面的字符,将其作为文件名
        return url.substring(url.lastIndexOf("/")+1);
    }


}
