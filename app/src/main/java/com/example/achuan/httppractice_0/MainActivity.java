package com.example.achuan.httppractice_0;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class MainActivity extends AppCompatActivity {

    public static final int SHOW_RESPONSE=0;//网络数据编号

    private ImageView mImageView;
    private Button mButton;

    private int count=0;

    /***调用主线程来接受获取的网络数据，并显示出来****/
    private Handler mHandler=new Handler(){
        public void handleMessage(Message msg) {
            int result=msg.what;
            count+=result;
            if(count==3)
            {
                Toast.makeText(MainActivity.this, "网络加载成功", Toast.LENGTH_SHORT).show();
                //通过文件的路径获取图片资源（编码解析的方式）
                Bitmap bitmap= BitmapFactory.decodeFile("/storage/emulated/0/bdlogo.png");
                mImageView.setImageBitmap(bitmap);
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mImageView= (ImageView) findViewById(R.id.my_iv);
        mButton= (Button) findViewById(R.id.my_bt);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //通过文件的路径获取图片资源（编码解析的方式）
                Bitmap bitmap= BitmapFactory.decodeFile("/storage/emulated/0/bdlogo.png");
                //如果本地存在资源,直接加载出来
                if(bitmap!=null)
                {
                    mImageView.setImageBitmap(bitmap);
                    Toast.makeText(MainActivity.this, "本地加载成功", Toast.LENGTH_SHORT).show();
                }
                else {
                    //创建一个子线程来进行网络访问请求,多线程并发将图片下载下来
                    new Thread()
                    {
                        @Override
                        public void run() {
                            DownLoad downLoad = new DownLoad(mHandler);
                            downLoad.downLoadFile("https://www.baidu.com/img/bdlogo.png");
                        }
                    }.start();
                }
            }
        });

    }

    /****发起网络请求的方法,将访问地址对应的资源存储到本地文件中****/
    private void sendRequestWithHttpURLConnection(final String address)
    {
        //开启线程来发起网络请求
        new Thread(new Runnable() {
            public void run() {
                HttpURLConnection connection=null;

                String name="林宇川";
                String my_url= null;
                try {
                    my_url = address+"?name="+ URLEncoder.encode(name,"utf-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }


                try{
                    URL url=new URL(my_url);//新建一个网络地址
                    connection= (HttpURLConnection) url.openConnection();//打开网址链接
                    //设置HTTP请求
                    connection.setRequestMethod("GET");//配置请求方式为：获取数据
                    connection.setConnectTimeout(8000);//设置连接超时时间
                    connection.setReadTimeout(8000);//设置读取超时时间
                    InputStream inputStream=connection.getInputStream();//获取服务器返回的输入流


                    FileOutputStream outputStream=null;//输出流,用来将数据引入到存储文件中
                    File downloadFile=null;//输出生成的文件
                    //文件名取为当前时间
                    String fileName= String.valueOf(System.currentTimeMillis());
                    /***检测是否存储外部sd卡存储空间***/
                    if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
                    {
                        //声明一个引用变量指向外部sd卡存储路径
                        File parent=Environment.getExternalStorageDirectory();
                        //新建一个文件对象来存储网络请求返回的数据
                        downloadFile=new File(parent,fileName);//参数介绍：文件生成的路径和文件名
                        //将输出流指向生成的文件
                        outputStream=new FileOutputStream(downloadFile);
                    }

                    byte[] b=new byte[2*1024];//创建一个长度为2*1024的"竹筒"
                    int len;//保存实际读取的字节数目
                    if(outputStream!=null)
                    {
                        //循环重复"取水"将输入流引入到输出的存储文件中
                        //read()方法是从输入流中读取单个字节,相当于从"竹筒"中取出一滴水
                        while ((len=inputStream.read(b))!=-1) {
                            //从"竹筒"中取出一滴水
                            outputStream.write(b,0,len);
                        }
                    }
                    //通过文件的路径获取图片资源（编码解析的方式）
                    Bitmap bitmap= BitmapFactory.decodeFile(downloadFile.getAbsolutePath());

                    Message message=new Message();//创建数据对象
                    message.what=SHOW_RESPONSE;//给数据对象设置标示用来分辨
                    //将服务器返回的结果存放到Message中
                    message.obj=bitmap;
                    mHandler.sendMessage(message);//将数据发送到主线程去处理
                }catch (Exception e)
                {
                    e.printStackTrace();
                }finally {
                    if(connection!=null)
                    {
                        connection.disconnect();//关闭HTTP连接
                    }
                }
            }
        }).start();
    }

    //POST方式进行网络请求的方法
    private void doPost(final String address)

    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection=null;
                try{
                    URL url=new URL(address);//新建一个网络地址
                    connection= (HttpURLConnection) url.openConnection();//打开网址链接
                    //设置HTTP请求
                    connection.setRequestMethod("POST");//配置请求方式为：获取数据
                    connection.setConnectTimeout(8000);//设置连接超时时间
                    connection.setReadTimeout(8000);//设置读取超时时间

                    /***下面的代码突出了post方式和get方式不同的地方***/
                    //声明一个输出流引用变量指向该网络的输出流通道
                    OutputStream outputStream=connection.getOutputStream();
                    String content="name="+"林宇川";
                    //将需要添加的消息通过输出流传递给服务器
                    outputStream.write(content.getBytes());//该方式会自动进行编码格式转换


                    InputStream inputStream=connection.getInputStream();//获取服务器返回的输入流
                    //对获取的输入流进行读取
                    BufferedReader reader=new BufferedReader(
                            new InputStreamReader(inputStream));
                    StringBuilder response=new StringBuilder();//创建一个字符数组来存储数据
                    String line;//表示每次读取到的数据
                    while ((line=reader.readLine())!=null) {
                        response.append(line);//将数据逐个读取后添加到数组中
                    }




                }catch (Exception e)
                {
                    e.printStackTrace();
                }finally {
                    if(connection!=null)
                    {
                        connection.disconnect();//关闭HTTP连接
                    }
                }
            }
        }).start();
    }

}
