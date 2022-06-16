package com.atguigu.gmall0401.manage.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall0401.bean.*;
import com.atguigu.gmall0401.service.ListService;
import com.atguigu.gmall0401.service.ManageService;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

@RestController
@CrossOrigin
public class ManageController {

    @Reference
    ManageService manageService;

    @Reference
    ListService listService;

    @PostMapping("getCatalog1")
    public List<BaseCatalog1> getBaseCatalog1(){

        List<BaseCatalog1> baseCatalog1List = manageService.getCatalog1();
        return baseCatalog1List;
    }

    @PostMapping("getCatalog2")
    public List<BaseCatalog2> getBaseCatalog2(String catalog1Id){

        List<BaseCatalog2> baseCatalog2List = manageService.getCatalog2(catalog1Id);
        return baseCatalog2List;
    }


    @PostMapping("getCatalog3")
    public List<BaseCatalog3> getBaseCatalog3(String catalog2Id){

        List<BaseCatalog3> baseCatalog3List = manageService.getCatalog3(catalog2Id);
        return baseCatalog3List;
    }

    @GetMapping("attrInfoList")
    public List<BaseAttrInfo> getBaseAttrInfoList(String catalog3Id){
        List<BaseAttrInfo> attrList = manageService.getAttrList(catalog3Id);

        return attrList;

    }

    @PostMapping("saveAttrInfo")

    public String saveAttrInfo(@RequestBody BaseAttrInfo baseAttrInfo){

        manageService.saveAttrInfo(baseAttrInfo);
        return  "success";

    }

    @PostMapping("getAttrValueList")
	//对应：ManageServiceImpl-
    public List<BaseAttrValue>  getAttrValueList(String attrId){
	
	//查询整个平台属性。
	//为什么不直接做查询列表要做查询属性？有较强的适应性。用于：别人还需要一个List
        BaseAttrInfo baseAttrInfo = manageService.getBaseAttrInfo(attrId);
	
	//查询平台属性里的列表属性.存在问题：把原有属性值全删了，序号也会被删，再新加的时候会有新序号
	//解决：在删除的时候保留原序号
	//删除对应：ManageServiceImpl-saveAttrInfo
        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();

        return attrValueList;

    }


    @PostMapping("baseSaleAttrList")
	
    public List<BaseSaleAttr> getBaseSaleAttrList(){
        return manageService.getBaseSaleAttrList();
    }


    @PostMapping("saveSpuInfo")

	//保存图片
    public String saveSpuInfo(@RequestBody SpuInfo spuInfo){
        manageService.saveSpuInfo(spuInfo);
        return "success";
    }


    @GetMapping("spuList")

	//根据三级分类查询spu列表
    public List<SpuInfo> getSpuList(String catalog3Id){
        return manageService.getSpuList(catalog3Id);
    }


    @GetMapping("spuImageList")
    public List<SpuImage> getSpuImageList(String spuId){
        return manageService.getSpuImageList(spuId);

    }


    @GetMapping("spuSaleAttrList")
	//得到销售属性值
    public  List<SpuSaleAttr> getSpuSaleAttrList(String spuId){
       return  manageService.getSpuSaleAttrList(spuId);
    }

    public  String onSaleBySpu(String spuId){
        // 根据spu 把其下所有sku上架
        return null;
    }

    @PostMapping("onSale")
    public  String onSale(@RequestParam("skuId") String skuId){

        SkuInfo skuInfo = manageService.getSkuInfo(skuId);

        SkuLsInfo skuLsInfo = new SkuLsInfo();

        try {
            BeanUtils.copyProperties(skuLsInfo,skuInfo);//拷贝

        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        //
        listService.saveSkuLsInfo(skuLsInfo);

        return "success";

    }


}
