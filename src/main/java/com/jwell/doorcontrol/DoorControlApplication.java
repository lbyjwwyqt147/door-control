package com.jwell.doorcontrol;

import com.jwell.doorcontrol.service.command.WgControllerInfo;
import com.jwell.doorcontrol.service.command.WgUdpClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(value = {"com.jwell.doorcontrol", "com.jwell.boot.utilscommon"})
public class DoorControlApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(DoorControlApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        WgControllerInfo wgControllerInfo = new WgControllerInfo();
        wgControllerInfo.setWatchServerIp("10.0.1.14");
        wgControllerInfo.setWatchServerPort(61005);
        WgUdpClient.chainingWatchingServer(wgControllerInfo);
    }
}
