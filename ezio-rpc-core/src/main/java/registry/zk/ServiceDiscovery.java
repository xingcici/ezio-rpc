package registry.zk;

import com.google.common.collect.ImmutableList;
import lombok.Data;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @creed: Here be dragons !
 * @author: Ezio
 * @Time: 2019/12/6 5:33 下午
 * @desc:
 */
@Data
public class ServiceDiscovery {

    private String zkAddress;
    private CuratorFramework client;
    private ConcurrentHashMap<String, PathChildrenCache> servicesMap = new ConcurrentHashMap<String, PathChildrenCache>();


    @SneakyThrows
    public static void main(String[] args) {
        ServiceRegistry serviceRegistry = new ServiceRegistry();
        serviceRegistry.setServiceName("test");
        serviceRegistry.setZkAddress("localhost:2181");
        serviceRegistry.connect();
        serviceRegistry.register(9000);
        Thread.sleep(2000);

        ServiceDiscovery serviceDiscovery = new ServiceDiscovery();
        serviceDiscovery.setZkAddress("localhost:2181");
        serviceDiscovery.connect();
        serviceDiscovery.getService("test");
        ImmutableList<Pair<String, Integer>> test = serviceDiscovery.getService("test");
        System.out.println(test);


    }


    public synchronized void connect() {
        CuratorFramework client = CuratorFrameworkFactory.newClient(this.zkAddress, 15 * 1000,
                5000, new ExponentialBackoffRetry(1000, 3));
        client.start();
        this.client = client;
    }

    public ImmutableList<Pair<String, Integer>> getService(String serviceName) {


        try {
            byte[] data = this.client.getData().forPath(ServiceRegistryManager.getServicePath(serviceName));
            String strData = new String(data, StandardCharsets.UTF_8);
            if (StringUtils.isNotBlank(strData)) {
                //            String[] split = strData.split(":");
//            return Pair.of(split[0], Integer.parseInt(split[1]));
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

//        List<Pair<String, Integer>> service = Optional.ofNullable(getPathChildrenCache(serviceName))
//                .map(pathChildrenCache -> {
//                    return pathChildrenCache.getCurrentData().stream().map(childData -> {
//                        byte[] data = childData.getData();
//                        String strData = new String(data, StandardCharsets.UTF_8);
//                        String[] split = strData.split(":");
//                        return Pair.of(split[0], Integer.parseInt(split[1]));
//                    }).collect(Collectors.toList());
//                })
//                .orElse(Lists.newArrayList());
//
//        return ImmutableList.copyOf(service);
    }

    private PathChildrenCache getPathChildrenCache(String serviceName) {
        return Optional.ofNullable(servicesMap.get(serviceName)).orElseGet(() -> {
            PathChildrenCache pathChildrenCache = new PathChildrenCache(this.client,
                    ServiceRegistryManager.getServicePath(serviceName), true);
            try {
                pathChildrenCache.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);
                servicesMap.put(serviceName, pathChildrenCache);
                return pathChildrenCache;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        });
    }


}