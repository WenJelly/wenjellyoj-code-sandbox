package com.wenjelly.wenjellyojcodesandbox.docker;

/*
 * @time 2023/11/28 19:48
 * @package com.wenjelly.wenjellyojcodesandbox.docker
 * @project wenjellyoj-code-sandbox
 * @author WenJelly
 */

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.ListContainersCmd;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.LogContainerResultCallback;

import java.util.List;


public class DockerDemo {
    public static void main(String[] args) throws InterruptedException {
        // 获取默认的DockerClient
        DockerClient dockerClient = DockerClientBuilder.getInstance().build();
//        PingCmd pingCmd = dockerClient.pingCmd();
//        pingCmd.exec();
        // 创建镜像
        String image = "nginx:latest";
//        PullImageCmd pullImageCmd = dockerClient.pullImageCmd(image);
//        // 创建回调，当进程执行完之后就会执行回调
//        PullImageResultCallback pullImageResultCallback = new PullImageResultCallback() {
//            @Override
//            public void onNext(PullResponseItem item) {
//                System.out.println("下载镜像" + item.getStatus());
//                super.onNext(item);
//            }
//        };
//        pullImageCmd
//                .exec(pullImageResultCallback)
//                // 意思是如果没下载完就会一直阻塞在这
//                .awaitCompletion();
//        System.out.println("下载完成");
        // 创建容器
        CreateContainerCmd containerCmd = dockerClient.createContainerCmd(image);
        CreateContainerResponse createContainerResponse = containerCmd
                .withCmd("echo", "Hello Docker")
                .exec();
        System.out.println(createContainerResponse);

        String containerId = createContainerResponse.getId();

        // 查看容器状态
        ListContainersCmd listContainersCmd = dockerClient.listContainersCmd();
        List<Container> containerList = listContainersCmd.withShowAll(true).exec();

        for (Container container : containerList) {
            System.out.println(container);
        }

        // 启动容器
        dockerClient.startContainerCmd(containerId).exec();

        // 查看日志
        LogContainerResultCallback logContainerResultCallback = new LogContainerResultCallback() {
            @Override
            public void onNext(Frame item) {
                System.out.println("日志:" + new String(item.getPayload()));
                super.onNext(item);
            }
        };
        dockerClient.logContainerCmd(containerId)
                // 错误输出
                .withStdErr(true)
                // 标准输出
                .withStdOut(true)
                .exec(logContainerResultCallback)
                .awaitCompletion();
        // 删除容器
        dockerClient.removeContainerCmd(containerId)
                // 强制删除
                .withForce(true)
                .exec();
        // 删除镜像
        dockerClient.removeImageCmd(image)
                // 强制删除
                .withForce(true)
                .exec();
    }
}
