package com.linlin7;


import com.alibaba.fastjson.JSON;
import com.linlin7.pojo.User;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

//在elasticsearch中提供了一种语句建造者QueryBuilder的接口，用于创建搜索查询的
//实用程序类。可以建造es数据库里的语句。
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Data 21/7/13
 * Author 振森
 */

@SpringBootTest
class JsonlinlinEsApiApplicationTests {
    @Autowired
    @Qualifier("restHighLevelClient")
    private RestHighLevelClient client;

    //测试请求
    @Test
    void testcreateIndex() throws IOException {
        //1，创建索引请求
        CreateIndexRequest request = new CreateIndexRequest("linlin7_test");
        //2,客户端执行请求 IndicesClient,请求后获得响应
        CreateIndexResponse createIndexResponse =
                client.indices().create(request, RequestOptions.DEFAULT);
        System.out.println(createIndexResponse);
    }

    //测试获取索引,
    @Test
    void testExistInder() throws IOException {
        GetIndexRequest request = new GetIndexRequest("linlin7_test");
        boolean exists = client.indices().exists(request, RequestOptions.DEFAULT);
        System.out.println(exists);
    }

    //测试添加文档
    @Test
    void testAddDocument() throws IOException {
        //创建对象
        User user = new User("linlin7", 23);
        //创建请求
        IndexRequest request = new IndexRequest("linlin7_test");
        //规则 put/lnlin7_index/_doc/1
        request.id("1");
        request.timeout(TimeValue.timeValueSeconds(1));
        request.timeout("1s");

        //将我们数据放入请求 json
        request.source(JSON.toJSONString(user), XContentType.JSON);

        //客户端发送请求,并获得响应结果
        IndexResponse indexResponse = client.index(request, RequestOptions.DEFAULT);

        System.out.println(indexResponse.toString());
        System.out.println(indexResponse.status()); //对应我们命令返回的状态 CREATED
    }

    //获取文档，判断是否存在 get/index/doc/1
    @Test
    void  testIsExists() throws IOException{
        GetRequest getRequest = new GetRequest("linlin7_test", "1");
        //不获取返回的_source的上下文
        getRequest.fetchSourceContext(new FetchSourceContext(false));
        getRequest.storedFields("_none_");

        boolean exists = client.exists(getRequest, RequestOptions.DEFAULT);
        System.out.println(exists);
    }

    //获取文档的信息
    @Test
    void testGetDocument() throws IOException{
        GetRequest getRequest = new GetRequest("linlin7_index1","1");
        GetResponse getResponse = client.get(getRequest,RequestOptions.DEFAULT);
        System.out.println(getResponse.getSourceAsString());//打印文档的内容
        System.out.println(getResponse);
    }

    //更新文档的信息
    @Test
    void testUpdateRequest() throws IOException{
        UpdateRequest updateRequest = new UpdateRequest("linlin7_test","1");
        updateRequest.timeout("1s");

        User user = new User("linlin10", 20);
        updateRequest.doc(JSON.toJSONString(user),XContentType.JSON);

        UpdateResponse updateResponse = client.update(updateRequest, RequestOptions.DEFAULT);
        System.out.println(updateResponse.status());
    }


    //批量插入数据
    @Test
    void testBulkRequest() throws IOException{
        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.timeout("10s");
        ArrayList<User> userList = new ArrayList<>();
        userList.add(new User("lin1",18));
        userList.add(new User("lin1",20));
        userList.add(new User("lin2",25));
        userList.add(new User("lin3",26));
        userList.add(new User("黄5",25));
        userList.add(new User("李六",26));

//批处理请求
        for (int i = 0; i <userList.size() ; i++) {
            bulkRequest.add(
                    new IndexRequest("linlin7_test")
                          .id(""+(i+1))   //除去id循环顺序增加，去除即按默认随机长id
                    .source(JSON.toJSONString(userList.get(i)),XContentType.JSON));
        }
        BulkResponse bulkItemResponses = client.bulk(bulkRequest,RequestOptions.DEFAULT);
        //返回false表示成功插入
    }

    //查询
    @Test
    void testSearch() throws IOException {
        SearchRequest searchRequest = new SearchRequest("linlin7_test");
        //构建搜索的条件
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        //查询条件，可以使用QueryBuilder 工具来实现
        //QueryBuilders.termQuery 精确查询
        //QueryBuilder.matchALLQuery() 匹配所有
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("name", "lin1");
        //MatchAllQueryBuilder matchAllQueryBuilder = QueryBuilders.matchAllQuery();
        sourceBuilder.query(termQueryBuilder);
        sourceBuilder.from();
        sourceBuilder.size();
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        //通过使用QueryBuilder可以自己拼接
        //查询语句，并且以elasticsearch特有的json语句方式传入。
        //在其中会先创建搜索源生成器searchSourceBuilder，
        //并且通过QueryBuilder工具包里的match query对查询语句进行分词，
        //分词后查询语句中的任何一个词项被匹配，文档就会被搜索到

        searchRequest.source(sourceBuilder);

        SearchResponse searchResponse = client.search(searchRequest,RequestOptions.DEFAULT);
        System.out.println(JSON.toJSONString(searchResponse.getHits()));
        System.out.println("=================================");//遍历出来
        for (SearchHit documentFields : searchResponse.getHits().getHits()){ //获取每个值
            System.out.println(documentFields.getSourceAsMap());  //打印所有值
        }

    }

    //删除文档记录
    @Test
    void testDeleteRequest() throws IOException{
        DeleteRequest request = new DeleteRequest("linlin7_index1", "1");
        request.timeout("1s");

        DeleteResponse deleteResponse = client.delete(request, RequestOptions.DEFAULT);
        System.out.println(deleteResponse.status());
    }

    //测试删除索引
    @Test
    void testDeleteInder() throws IOException {
        DeleteIndexRequest request = new DeleteIndexRequest("linlin7_index1");
        //删除
        AcknowledgedResponse delete = client.indices().delete(request, RequestOptions.DEFAULT);
        System.out.println(delete.isAcknowledged());
    }
}
