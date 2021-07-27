package com.linlin7.controller;

import com.linlin7.service.ContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

//编写请求，编写controller
@RestController
public class ContentController {
    @Autowired
    //注入ContentService类
    private ContentService contentService;

    //Map搜插入请求
    @GetMapping("/parse/{keyword}") //浏览器地址里直接解析关键字
    public Boolean parse(@PathVariable ("keyword") String keyword) throws Exception {
       return contentService.parseContent(keyword);
        //解析关键字，返回请求
    }

    //Map搜索请求
    @GetMapping("/search/{keyword}/{pageNo}/{pageSize}")
    public List<Map<String,Object>> search(@PathVariable("keyword")String keyword,
                                           @PathVariable("pageNo") int pageNo,
                                           @PathVariable("pageSize") int pageSize) throws IOException {
        return contentService.searchPageHighlightBuilder(keyword,pageNo,pageSize);

    }
}
