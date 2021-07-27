package com.linlin7.service;

import com.alibaba.fastjson.JSON;
import com.linlin7.pojo.Content;
import com.linlin7.utils.HtmlParseUtil;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.MacAddressProvider;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.naming.directory.SearchResult;
import javax.swing.text.Highlighter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

//业务编写
@Service
public class ContentService {
    @Autowired
    //注入Elasticsearch配置类
    private RestHighLevelClient restHighLevelClient;

    //public static void main(String[] args) throws Exception {
    //    new ContentService().parseContent("java");
    //}   导入失败，查看原因是Autowired需要spring容器

    //1.解析数据放到es索引中
    public Boolean parseContent(String keyword) throws Exception {  //通过 Content类对象，再通过keyword关键字解析
        List<Content> contents = new HtmlParseUtil().parseJD(keyword);
        //把查询到的数据插入到es中
        BulkRequest bulkRequest = new BulkRequest();
        //拿到封装请求
        bulkRequest.timeout("2m");
        //请求时间限制

        for (int i = 0; i <contents.size() ; i++) {
            //循环插入进去，直到插入完所有爬到的数据
            bulkRequest.add(
                    new IndexRequest("jd_goods") //插入到es的jd_goods所有里
                    .source(JSON.toJSONString(contents.get(i)), XContentType.JSON)
                    //拿到对象数据封装成JSON数据传输（es主要靠json语句操作）
            );
        }
        BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        //执行批量请求
        return !bulk.hasFailures();
        //查看是否插入成功，如果成功就返回ture
    }

    //2,获取es里这些数据，实现搜索功能
    public List<Map<String,Object>> searchPage(String keyword, int pageNo, int pageSize) throws IOException {
        //返回的是List，封装成Map，用Object取出来，再进行分页
        if (pageNo<=1){
            pageNo =1;
            //Map里的页数少于1，就默认页数为1
        }

        //条件搜索
        SearchRequest searchRequest = new SearchRequest("jd_goods");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        //分页
        sourceBuilder.from(pageNo);
        sourceBuilder.size(pageSize);

        //精确匹配
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("title",keyword);
        sourceBuilder.query(termQueryBuilder);
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        //执行搜索
        searchRequest.source(sourceBuilder);
    SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

    //解析结果
    ArrayList<Map<String,Object>> list = new ArrayList<>();
        for (SearchHit documentFields : searchResponse.getHits().getHits()){
        list.add(documentFields.getSourceAsMap());
    }
        return list;
}



    //3,实现搜索高亮功能
    public List<Map<String,Object>> searchPageHighlightBuilder(String keyword, int pageNo, int pageSize) throws IOException {
        if (pageNo<=1){
            pageNo =1;
        }

        //条件搜索
        SearchRequest searchRequest = new SearchRequest("jd_goods");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        //分页
        sourceBuilder.from(pageNo);
        sourceBuilder.size(pageSize);

        //精确匹配
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("title",keyword);
        sourceBuilder.query(termQueryBuilder);
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));




        //----------------------------------------------------------------------//

        //实现搜索高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title"); //设置高亮的字段
        highlightBuilder.requireFieldMatch(false); //关闭多个字段高亮显示
        highlightBuilder.preTags("<span style='color:red'>"); //设置前缀
        highlightBuilder.postTags("</span>"); //设置后缀
        sourceBuilder.highlighter(highlightBuilder);

        //执行搜索
        searchRequest.source(sourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        //解析结果
        ArrayList<Map<String,Object>> list = new ArrayList<>();
        for (SearchHit hit : searchResponse.getHits().getHits()){

            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
           HighlightField title =  highlightFields.get("title");
           Map<String,Object> sourceAsMap = hit.getSourceAsMap();  //原来的结果
            //解析高亮的字段,将原来的字段换为我们的字段即可！
            if(title!=null){
                Text[] fragments = title.fragments();
                String n_title = "";
                for(Text text:fragments){
                    n_title += text;
                }
                sourceAsMap.put("title",n_title);  //高亮字段替换为原来的字段
            }
            list.add(sourceAsMap);
        }
        return list;
    }
}
