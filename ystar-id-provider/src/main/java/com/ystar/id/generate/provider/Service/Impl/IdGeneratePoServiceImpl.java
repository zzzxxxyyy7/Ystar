package com.ystar.id.generate.provider.Service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ystar.id.generate.provider.Domain.mapper.YstarIdGenerateConfigMapper;
import com.ystar.id.generate.provider.Domain.po.IdGeneratePo;
import com.ystar.id.generate.provider.Service.IdGeneratePoService;
import com.ystar.id.generate.provider.Service.bo.LocalSeqIdBO;
import com.ystar.id.generate.provider.Service.bo.LocalUnSeqIdBO;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
* @author Rhss
* @description 针对表【ystar_id_generate_config】的数据库操作Service实现
* @createDate 2024-07-29 19:22:43
*/
@Service
public class IdGeneratePoServiceImpl extends ServiceImpl<YstarIdGenerateConfigMapper, IdGeneratePo>
    implements IdGeneratePoService , InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(IdGeneratePoServiceImpl.class);

    /**
     * 本地内存 ID 管理器
     * 数据库 ID 分段实现器
     * 根据传入 id 值获取各个分段 ID 并且逐次递增
     */
    private static final Map<Integer , LocalSeqIdBO> localSeqIdBOMap = new ConcurrentHashMap<>();
    private static final Map<Integer , LocalUnSeqIdBO> localunSeqIdBOMap = new ConcurrentHashMap<>();

    /**
     * 初始化本地 Id 管理器全表查询使用
     */
    @Resource
    private YstarIdGenerateConfigMapper ystarIdGenerateConfigMapper;

    /**
     * 触发 ID 段异步更新阈值
     */
    private static final float UPDATE_RATE = 0.75f;

    /**
     * 异步更新用线程池
     */
    private static final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
            8,
            16,
            3,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(1000),
            r -> {
                Thread thread = new Thread(r);
                thread.setName("id-generate-thread-" + ThreadLocalRandom.current().nextInt(1000));
                return thread;
            }
    );

    /**
     * 避免扩容任务重复执行，
     */
    private static final Map<Integer , Semaphore> semaphoreMap = new ConcurrentHashMap<>();
    private static final Map<Integer , Semaphore> semaphoreMapButNotSeq = new ConcurrentHashMap<>();

    /**
     * 有序 ID 获取
     * @param id
     * @return
     */
    @Override
    public Long getSeqId(Integer id) {
        LOGGER.info("{} 发生了一次有序 ID 请求" , LocalDateTime.now());
        if (id == null) {
            LOGGER.info("获取有序分布式ID参数错误，id is 不存在" );
            return null;
        }

        /*
          获取本地内存中的 localSeqIdBO 对象
         */
        LocalSeqIdBO localSeqIdBO = localSeqIdBOMap.get(id);
        if (localSeqIdBO == null) {
            LOGGER.info("获取有序分布式ID参数错误，本地内存中不存在这样的 localSeqIdBO ");
            return null;
        }

        this.refreshLocalSeqId(localSeqIdBO);

        long returnResult = localSeqIdBO.getCurrentNum().getAndIncrement();

        // 如果本地 ID 段已经用完了，就要限制自增，避免抢占其他线程的 ID 段
        if (returnResult > localSeqIdBO.getNextThreshold()) {
            LOGGER.info("当前 ID 段已经用完");
            // 快速失败，更好的适用于高并发情况，同步刷新容易占用非常多的 Dubbo 线程池
            return null;

        }

        // 原子类自增，避免线程安全问题
        return returnResult;
    }

    /**
     * 无序 Id 获取
     * @param id
     * @return
     */
    @Override
    public Long getUnSeqId(Integer id) {
        LOGGER.info("{} 发生了一次无序 ID 请求" , LocalDateTime.now());
        if (id == null) {
            LOGGER.info("获取有序分布式ID参数错误，id is 不存在" );
            return null;
        }

        /*
          获取本地内存中的 localSeqIdBO 对象
         */
        LocalUnSeqIdBO localUnSeqIdBO = localunSeqIdBOMap.get(id);
        if (localUnSeqIdBO == null) {
            LOGGER.info("获取无序分布式ID参数错误，本地内存中不存在这样的 localUnSeqIdBO ");
            return null;
        }

        // 本身就是一个线程安全的队列
        Long poll = localUnSeqIdBO.getIdQueue().poll();

        if (poll == null) {
            LOGGER.info("获取无序分布式错误，无序队列为空 , id is {}" , localUnSeqIdBO.getId());
            return null;
        }

        this.refreshLocalUnSeqId(localUnSeqIdBO);
        return poll;
    }

    /**
     * 计算是否需要号段扩容 - 有序
     * @param localSeqIdBO
     */
    void refreshLocalSeqId(LocalSeqIdBO localSeqIdBO) {
        // 计算差值
        long step = localSeqIdBO.getNextThreshold() - localSeqIdBO.getCurrentStart();
        // 触发 ID 段更新
        if (localSeqIdBO.getCurrentNum().get() - localSeqIdBO.getCurrentStart() > step * UPDATE_RATE) {
            Semaphore semaphore = semaphoreMap.get(localSeqIdBO.getId());

            if (semaphore == null) {
                LOGGER.info("Semaphore is null ， id is {}" , localSeqIdBO.getId());
                return;
            }

            // 获取 Semaphore 获取成功才能进入判断操作
            boolean acquireStatus = semaphore.tryAcquire();

            if (acquireStatus) {
                LOGGER.info("已竞争到锁，开始异步更新 ID 段");
                // 异步启动
                threadPoolExecutor.execute(() -> {
                    try {
                        LOGGER.info("更新 ID 段开始");
                        // 查出最新的 ID 段，并且尝试加载
                        IdGeneratePo idGeneratePo = ystarIdGenerateConfigMapper.selectById(localSeqIdBO.getId());
                        tryUpdateMySQLRecord(idGeneratePo);
                        LOGGER.info("获取下一个 ID 段完成，更新结束，释放锁");
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    } finally {
                        // 释放 Semaphore
                        semaphore.release();
                    }
                });
            }
        }
    }

    /**
     * 计算是否需要号段扩容 - 无序
     * @param localUnSeqIdBO
     */
    void refreshLocalUnSeqId(LocalUnSeqIdBO localUnSeqIdBO) {
        // 计算差值
        long begin = localUnSeqIdBO.getCurrentStart();
        long end = localUnSeqIdBO.getNextThreshold();
        int remainSize = localUnSeqIdBO.getIdQueue().size();
        // 剩余空间不足百分之 25 触发刷新
        if ((end - begin) * 0.25 > remainSize) {
            Semaphore semaphore = semaphoreMapButNotSeq.get(localUnSeqIdBO.getId());

            if (semaphore == null) {
                LOGGER.info("Semaphore is null ， id is {}" , localUnSeqIdBO.getId());
                return;
            }

            boolean acquireResult = semaphore.tryAcquire();

            if (acquireResult) {
                LOGGER.info("成功抢占到锁，无序 Id 开始扩容, id is {}" , localUnSeqIdBO.getId());
                try {
                    threadPoolExecutor.execute(() -> {
                        IdGeneratePo idGeneratePo = ystarIdGenerateConfigMapper.selectById(localUnSeqIdBO.getId());
                        tryUpdateMySQLRecord(idGeneratePo);
                        semaphore.release();
                        LOGGER.info("无序 Id 扩容成功 ， id is {} ， 释放锁" , localUnSeqIdBO.getId());
                    });
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                } finally {
                    // 释放 Semaphore
                    semaphore.release();
                }
            }
        }
    }

    /**
     * 更新 Mysql ID 段
     * @param idGeneratePo
     */
    public void tryUpdateMySQLRecord(IdGeneratePo idGeneratePo) {
        // 告诉数据库，当前 ID 段位已被占用，但是更新不一定成功
        int updateResult = ystarIdGenerateConfigMapper.updateNewIdCountAndVersion(
                idGeneratePo.getId(),
                idGeneratePo.getVersion()
        );

        // 更新成功
        if (updateResult > 0) {
            if (idGeneratePo.getIsSeq() == 1) RetrySuccess(idGeneratePo);
            else RetrySuccessButNotSeq(idGeneratePo);
            return;
        }

        // 重试次数
        int retryTime = 0;

        // 更新失败，CAS 重试
        while (updateResult == 0 && retryTime < 3) {
            // 重试次数 + 1
            retryTime++;
            // 当前 PO 是过期 PO，版本号不一致，不断重试获取新 PO
            IdGeneratePo idGeneratePo1 = ystarIdGenerateConfigMapper.selectById(idGeneratePo.getId());
            // 重新更新 PO 不断重试
            updateResult = ystarIdGenerateConfigMapper.updateNewIdCountAndVersion(
                    idGeneratePo1.getId(),
                    idGeneratePo1.getVersion()
            );
            // 更新成功
            if (updateResult > 0) {
                // 传入新查出的 PO 对象
                if (idGeneratePo.getIsSeq() == 1) RetrySuccess(idGeneratePo1);
                else RetrySuccessButNotSeq(idGeneratePo);
                return;
            }
        }

        throw new RuntimeException("全局 ID 生成器 CAS 获取锁失败，竞争过于激烈 ， id is " + idGeneratePo.getId());
    }

    /**
     * 更新 MySql 字段成功且 Id 有序
     * @param idGeneratePo
     */
    public void RetrySuccess(IdGeneratePo idGeneratePo) {
        // 取出当前号段的 起始 和 边界值
        Long currentStart = idGeneratePo.getCurrentStart();
        Long nextThreshold = idGeneratePo.getNextThreshold();
        // 更新成功
        LocalSeqIdBO localSeqIdBO = new LocalSeqIdBO();
        AtomicLong atomicLong = new AtomicLong(currentStart);

        // 设置数据库号段 ID 标识
        localSeqIdBO.setId(idGeneratePo.getId());
        // 设置当前 ID 段起始值，第一次加载 初始值 就是 当前值
        localSeqIdBO.setCurrentNum(atomicLong);
        // 设置 初始值 和 结束值
        localSeqIdBO.setCurrentStart(currentStart);
        localSeqIdBO.setNextThreshold(nextThreshold);

        // 放入本地 ID 生成器
        localSeqIdBOMap.put(localSeqIdBO.getId(),  localSeqIdBO);
    }

    /**
     * 更新 MySql 字段成功且 Id 无序
     * @param idGeneratePo
     */
    public void RetrySuccessButNotSeq(IdGeneratePo idGeneratePo) {
        // 取出当前号段的 起始 和 边界值
        Long currentStart = idGeneratePo.getCurrentStart();
        Long nextThreshold = idGeneratePo.getNextThreshold();
        // 更新成功
        LocalUnSeqIdBO localUnSeqIdBO = new LocalUnSeqIdBO();
        // 设置数据库号段 ID 标识
        localUnSeqIdBO.setId(idGeneratePo.getId());
        // 设置 初始值 和 结束值
        localUnSeqIdBO.setCurrentStart(currentStart);
        localUnSeqIdBO.setNextThreshold(nextThreshold);
        // 取出所有 Id
        List<Long> idList = new ArrayList<>();
        for (Long begin = localUnSeqIdBO.getCurrentStart() ; begin <= localUnSeqIdBO.getNextThreshold() ;
        ++ begin) idList.add(begin);
        // 打乱 ID
        Collections.shuffle(idList);
        // 加入目标集合
        ConcurrentLinkedQueue<Long> concurrentLinkedQueue = new ConcurrentLinkedQueue<>(idList);
        localUnSeqIdBO.setIdQueue(concurrentLinkedQueue);
        // 放入本地 ID 生成器
        localunSeqIdBOMap.put(localUnSeqIdBO.getId(),  localUnSeqIdBO);
    }

    /**
     * SpringBoot Bean 在初始化的时候有一个生命周期，在初始化接口会进行回调这个函数，触发我们定义的操作
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        /**
         * 回调接口，用来初始化本地 ID 生成器
         */
        initLocalSeqIdBO();
    }

    /**
     * 初始化 Bean 的时候加载 ID 段到本地缓存
     */
    public void initLocalSeqIdBO() {
        List<IdGeneratePo> idGeneratePos = ystarIdGenerateConfigMapper.selectAll();
        for (IdGeneratePo idGeneratePo : idGeneratePos) {
            // 乐观锁实现 版本号机制 + CAS 算法
            tryUpdateMySQLRecord(idGeneratePo);
            // 针对每个号段维护一个 Semaphore 变量
            if (idGeneratePo.getIsSeq() == 1) semaphoreMap.put(idGeneratePo.getId() , new Semaphore(1));
            else semaphoreMapButNotSeq.put(idGeneratePo.getId() , new Semaphore(1));
        }
    }

}
