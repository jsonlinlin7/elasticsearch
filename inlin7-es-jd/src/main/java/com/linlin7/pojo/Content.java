package com.linlin7.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
//把爬取到的数据封装成一个Content对象
public class Content {
    private String title;
    private String img;
    private String price;
    //以后可自行添加属性
}
