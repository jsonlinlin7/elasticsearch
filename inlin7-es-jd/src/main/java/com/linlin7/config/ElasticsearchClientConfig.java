package com.linlin7.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*
1.找对象
2.放到spring中待用
3.分析源码
 */
@Configuration
//编写Elasticsearch配置类
public class ElasticsearchClientConfig {

    @Bean
    //es客户端组件RestHighLevelClient的使用，其封装了操作es的crud方法，底层原理就是模拟各种es需要的请求，如put，delete，get等方式
    public RestHighLevelClient restHighLevelClient(){
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("127.0.0.1", 9200, "http")));
        return client;
    }
}
