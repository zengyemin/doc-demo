package com.doc.demo.schedules;

import com.doc.demo.utils.SpringUtil;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 文件的定时任务
 *
 * @author : zengYeMin
 * @date : 2022/5/9 9:28
 **/
public class FileSchedule {

    private final Logger LOGGER = LoggerFactory.getLogger(FileSchedule.class);

    private RedisTemplate redisTemplate;

    private final String KEY = "previewFilePaths";

    private static FileSchedule fileSchedule;

    {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        //定时任务线程
        executor.scheduleAtFixedRate(() -> {
            Map<String, Long> previewFileExpiredMap = getPreviewFileExpiredMap();
            if (previewFileExpiredMap.isEmpty()) {
                return;
            }
            //filterList中为需要做过期删除的预览文件
            List<String> filterFilePathList = previewFileExpiredMap.entrySet().stream()
                //当前过期时间小于等于系统时间，则表示要做文件过期处理了
                .filter(x -> x.getValue() <= System.currentTimeMillis()).map(Entry::getKey)
                .collect(Collectors.toList());
            filterFilePathList.forEach(x -> {
                boolean del = previewFileDel(x);
                if (!del) {
                    LOGGER.info("删除预览文件失败，文件路径:{}", x);
                    return;
                }
                LOGGER.info("删除预览文件成功，文件路径:{}", x);
                //删除Redis中的预览文件过期信息
                deletePreviewFileExpired(x);
            });
        }, 3, 5, TimeUnit.MINUTES);
    }

    /**
     * 保存文件过期信息
     *
     * @param filePath 文件路径
     * @param expiredTime 过期时间，单位为分钟
     */
    public void putFileExpiredInfo(String filePath, Long expiredTime) {
        //过期的毫秒数
        long expiredTimeMillis = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(expiredTime);
        redisTemplateInit();
        HashOperations<String, String, Long> hashOperations = redisTemplate.opsForHash();
        //将过期时间设置到Redis中
        hashOperations.put(KEY, filePath, expiredTimeMillis);
    }

    /**
     * 获取预览文件的过期信息Map
     *
     * @return 返回缓存中的map，如果没有则为{@link Collections#emptyMap()}
     */
    private Map<String, Long> getPreviewFileExpiredMap() {
        redisTemplateInit();
        HashOperations<String, String, Long> hashOperations = redisTemplate.opsForHash();
        return hashOperations.entries(KEY);
    }

    /**
     * 删除Redis中的预览文件过期信息
     *
     * @param filePath 文件路径
     */
    private void deletePreviewFileExpired(String filePath) {
        redisTemplateInit();
        HashOperations<String, String, Long> hashOperations = redisTemplate.opsForHash();
        hashOperations.delete(KEY, filePath);
    }

    /**
     * 删除过期的预览文件
     *
     * @param filePath 文件绝对路径
     * @return 是否删除成功
     */
    private boolean previewFileDel(String filePath) {
        File file = new File(filePath);
        //不存在则表示已经删除，直接返回true
        if (!file.exists()) {
            return true;
        }

        return file.delete();
    }

    /**
     * 单例创建对象
     *
     * @return {@link FileSchedule}
     */
    public static FileSchedule instance() {
        if (fileSchedule != null) {
            return fileSchedule;
        }
        synchronized (FileSchedule.class) {
            if (fileSchedule != null) {
                return fileSchedule;
            }
            fileSchedule = new FileSchedule();
        }
        return fileSchedule;
    }

    /**
     * 初始化Redis模板信息
     */
    private void redisTemplateInit() {
        if (redisTemplate != null) {
            return;
        }
        redisTemplate = SpringUtil.getBean("redisTemplate", RedisTemplate.class);

    }
}
