package com.atguigu.gmall0401.item.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall0401.bean.SkuInfo;
import com.atguigu.gmall0401.bean.SpuSaleAttr;
import com.atguigu.gmall0401.config.LoginRequire;
import com.atguigu.gmall0401.service.ListService;
import com.atguigu.gmall0401.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Controller	//根据return “item" :需要进一步渲染
public class ItemController {

    @Reference
    ManageService manageService; //查询需要一个接口

    //@Reference
    //ListService listService;

	//路径是伪静态的结构
    @GetMapping("{skuId}.html")

    public String item(@PathVariable("skuId") String skuId, HttpServletRequest request){
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);
        List<SpuSaleAttr> spuSaleAttrList = manageService.getSpuSaleAttrListCheckSku(skuId, skuInfo.getSpuId());
	
	//skuinfo在demo.html中会识别为skuinfo
        request.setAttribute("skuInfo",skuInfo);
        request.setAttribute("spuSaleAttrList",spuSaleAttrList);

        //得到属性组合与skuid的映射关系 ，用于页面根据属性组合进行跳转
        Map skuValueIdsMap = manageService.getSkuValueIdsMap(skuInfo.getSpuId());

	//map转成Json, 可以在页面上找一个地方接
        String valuesSkuJson = JSON.toJSONString(skuValueIdsMap);

        request.setAttribute("valuesSkuJson",valuesSkuJson);
        //listService.incrHotScore(skuId);
        request.getAttribute("userId");
        return   "item";
    }
}
