package com.atguigu.gmall0401.manage;

import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;


@RunWith(SpringRunner.class)
@SpringBootTest
public class Gmall0401ManageWebApplicationTests {

    @Test
    public void contextLoads() {
    }

    @Test
    public void uploadFile() throws IOException, MyException {
        // 1
        String file = this.getClass().getResource("/tracker.conf").getFile();

	//把文件内容加载到内存中/环境中
	//内存读取文件参数：地址
        ClientGlobal.init(file);

	//client传给storage——需要知道storage地址
	//			——client跟tracker建立连接得到storage地址
	//				——client连接storage

	//trackerClient:客户端连服务器
        TrackerClient trackerClient = new TrackerClient();

	//client跟tracker建立连接得到storage地址
        TrackerServer trackerServer = trackerClient.getConnection();

	
        StorageClient storageClient = new StorageClient(trackerServer,null);

        String[] upload_file = storageClient.upload_file("d://金背.jpg", "jpg", null);
        for (int i = 0; i < upload_file.length; i++) {
            String s = upload_file[i];
            System.out.println("s="+s);
        }

    }

}
