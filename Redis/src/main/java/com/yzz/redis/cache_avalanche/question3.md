####【考核题目】：
    利用 TTL 随机化 策略解决商品首页的缓存雪崩问题。

####【业务背景】：
    我们要开发电商 APP 的“首页每日精选”模块。

    现状：后台有一个定时任务，每天凌晨 00:00 计算出当天推荐的 100 个商品，写入 Redis。

    事故：为了省事，开发人员给这 100 个商品都设置了 expire = 1 hour。结果每天凌晨 01:00，这 100 个 Key 同时过期。此时首页并发量依然很大，MySQL CPU 直接飙升 100% 报警。

####【具体需求】：

    编写 HomeRecommendService 类。

    需求：写入缓存 (TTL 随机化)

        编写方法 void refreshRecommendProducts(List<Product> products)。

        核心考点：将商品存入 Redis 时，基础过期时间是 1 小时（3600秒）。请在此基础上增加一个随机时间（比如 0~300秒），让过期时间分散开，不再同时失效。

    【数据模型】：

    Redis Key 格式：home:recommend:+id

####请提供：
    HomeRecommendService 的完整代码。