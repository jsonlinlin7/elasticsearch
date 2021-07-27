package com.linlin7;

import com.linlin7.pojo.Content;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

//编写解析网页工具包
//封装工具类
@Component
public class HtmlParseUtil {
    public HtmlParseUtil() {
    }

    public List<Content> parseJD(String keyword) throws Exception {
        //获取请求：https://search.jd.com/Search?keyword=java
        //需要联网
        String url = "https://search.jd.com/Search?keyword=" + keyword;
        //解析网页（Jsoup返回的Document对象就是JS的Document对象）
        Document document = Jsoup.parse(new URL(url), 30000);
        //所有JS的方法都可以使用
        Element element = document.getElementById("J_goodsList");
        //获取所有的li标签
        Elements elements = element.getElementsByTag("li");
        //获取元素中的内容，这里的el就是每个li标签
        ArrayList<Content> goodsList = new ArrayList();

        Iterator var7 = elements.iterator();

        while(var7.hasNext()) {
            Element el = (Element)var7.next();
            //关于图片特别多的网站，所有图片都是懒加载
            if (el.attr("class").equalsIgnoreCase("gl-item")) {
                String img = el.getElementsByTag("img").eq(0).attr("data-lazy-img");  //图片
                String price = el.getElementsByClass("p-price").eq(0).text(); //价格
                String title = el.getElementsByClass("p-name").eq(0).text();  //名字
                Content conten = new Content();
                conten.setTitle(title);
                conten.setPrice(price);
                conten.setImg(img);
                goodsList.add(conten);
            }
        }

        return goodsList;
    }
}