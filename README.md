## Ystar直播

基于Netty从零搭建的一个直播平台，自研了消息传递IM服务器和全局ID生成器，实现了消息的定向投递，开发高性能用户中台，支持亿级用户数据存储，充分拆解服务边界，大量使用缓存增速和MQ解耦削峰，主要功能有消息互发、直播打赏、主播PK等

### 技术栈：SpringCloud、Netty、Dubbo、RabbitMQ、Redis、MySQL
- 自研IM服务器，基于推拉模型实现在线/离线均可获取直播间全程消息，引入重试机制确保消息送达
- 基于数据库号段模式自研分布式唯一ID生成器，供给订单、送礼、注册服务高效获取唯一ID
- 扩展Dubbo SPI机制，实现消息向特定用户所在IM转发，避免消息风暴，提高IM的可靠性
- 使用MQ、线程池等技术，多处采用多线程或消息投递方式执行，提高了约50%的执行速度
- 搭建MySQL主从集群，延迟双删解决主从一致问题，保障DB的高度可用
- 针对多处先查缓存再改数据如余额扣减等，通过分布式锁和Lua脚本保证俩个操作的原子性
- 单机环境下，4核8G服务器Jmeter25并发度压测下平均响应时间在14ms，错误率在0.81%，吞吐量在1728/s左右
