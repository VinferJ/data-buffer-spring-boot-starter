package group.liquido.databuffer.core;

import group.liquido.databuffer.core.factory.DelegateThreadFactory;
import com.sun.management.GarbageCollectionNotificationInfo;
import com.sun.management.GcInfo;

import javax.management.Notification;
import javax.management.NotificationEmitter;
import javax.management.NotificationListener;
import javax.management.openmbean.CompositeData;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author vinfer
 * @date 2022-12-07 11:01
 */
public class GcCheck {

    public static void main(String[] args) {
        registerGcListener();
        doService();
        holdThread();
    }

    static void doService() {
        List<String> list = new CopyOnWriteArrayList<>();
        DelegateThreadFactory delegateThreadFactory = new DelegateThreadFactory();
        delegateThreadFactory.createThread("ReadThread", () -> {
            while (true) {
                for (String s : list) {
                    //System.out.println("=====read string len: " + s.length());
                }
                try {
                    Thread.sleep(500L);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
        delegateThreadFactory.createThread("WriteThread", () -> {
            Random random = new Random();
            int writeCount = 0;
            for (;;) {
                writeCount++;
                String str = genString(Math.abs(random.nextInt(1024)));
                list.add(str);
                System.out.println("=====write count: " + writeCount + " =======write size: " + str.length());
                try {
                    Thread.sleep(Math.max(50, random.nextInt(500)));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    static String genString(int size) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size; i++) {
            sb.append(i);
            if (sb.length() >= size) {
                break;
            }
        }
        return sb.toString();
    }

    static void holdThread() {
        while (true) {

        }
    }

    static void registerGcListener() {
        for (GarbageCollectorMXBean gcBean : ManagementFactory.getGarbageCollectorMXBeans()) {
            if (!(gcBean instanceof NotificationEmitter)) {
                continue;
            }
            NotificationEmitter emitter = (NotificationEmitter) gcBean;
            GcNotificationListener listener = new GcNotificationListener();
            emitter.addNotificationListener(listener, null, null);
        }
    }

    static class GcNotificationListener implements NotificationListener {

        @Override
        public void handleNotification(Notification notification, Object handback) {
            String notifType = notification.getType();
            if (notifType.equals(GarbageCollectionNotificationInfo.GARBAGE_COLLECTION_NOTIFICATION)) {
                CompositeData cd = (CompositeData) notification.getUserData();
                GarbageCollectionNotificationInfo info = GarbageCollectionNotificationInfo.from(cd);
                GcInfo gcInfo = info.getGcInfo();
                HashMap<String, Object> map = new HashMap<>();
                map.put("duration", gcInfo.getDuration());
                map.put("id", gcInfo.getId());
                map.put("UsageAfterGc", gcInfo.getMemoryUsageAfterGc());
                map.put("UsageBeforeGc", gcInfo.getMemoryUsageBeforeGc());
                map.put("GcAction", info.getGcAction());
                map.put("GcCause", info.getGcCause());
                map.put("GcName", info.getGcName());
                System.out.println(map);
            }
        }
    }

}
