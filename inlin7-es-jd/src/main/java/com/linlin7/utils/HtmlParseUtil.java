package com.linlin7.utils;

import com.linlin7.pojo.Content;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;


import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Component
//放到Spring容器里待用
public class HtmlParseUtil {
    //HtmlParseUtil的工具类

  //  public static void main(String[] args) throws Exception {
  //      new HtmlParseUtil().parseJD("玩具").forEach(System.out::println);
  //  }

    public List<Content> parseJD(String keyword) throws Exception {

        //中文可以在链接后面加&enc=utf-8
        //new HtmlParseUtil().parseJd("心理学").stream().forEach(System.out::println);
        //获取请求
        //前提，需要联网， ajax不能获取到，模拟浏览器才能获取到
        String url = "https://search.jd.com/Search?keyword="+keyword;
        /**
         * 解析网页(Jsoup返回Document就是浏览器Document对象)
         * 这里最开始得到的是跳转登录的页面，后来我在网页上登陆了，还是不行，看了弹幕，把
         * jsoup升级到最新版本（1.31.1）就好了
         */
        Document document = Jsoup.parse(new URL(url), 30000);
        //所有你在js中使用的方法这里都能用
        Element element = document.getElementById("J_goodsList");
        //获取所有的li元素
        Elements elements = element.getElementsByTag("li");

        //把爬到的对象保存到content里
        ArrayList<Content> goodsList = new ArrayList<>();

        //获取元素中的内容，这里el就是每一个li标签了
        for (Element el : elements) {
            //关于这种图片，特别多的网站都是延迟加载的
            //加载获取数据的具体元素
            if (el.attr("class").equalsIgnoreCase("gl-item")) {

                String img = el.getElementsByTag("img").eq(0).attr("data-lazy-img");
                String price = el.getElementsByClass("p-price").eq(0).text();
                String title = el.getElementsByClass("p-name").eq(0).text();


                //爬到的数据封装对象
                Content conten = new Content();
                conten.setTitle(title);
                conten.setPrice(price);
                conten.setImg(img);
                goodsList.add(conten);
            }
        }
            return goodsList;
        //返回对象
        }

}