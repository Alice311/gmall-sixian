package com.atguigu.gmall0401.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall0401.bean.CartInfo;
import com.atguigu.gmall0401.bean.SkuInfo;
import com.atguigu.gmall0401.cart.mapper.CartInfoMapper;
import com.atguigu.gmall0401.service.CartService;
import com.atguigu.gmall0401.service.ManageService;
import com.atguigu.gmall0401.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.*;


@Service
public class CartServiceImpl implements CartService {

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    CartInfoMapper cartInfoMapper;

    @Reference
    ManageService manageService;//远程调用，查信息-查价格

    @Override
    public CartInfo addCart(String userId, String skuId, Integer num) {
        // 为了防止 更新购物车前 缓存过期
        loadCartCacheIfNotExists(  userId) ;
        // 加数据库
        // 尝试取出已有的数据    如果有  把数量更新 update   如果没有insert
        CartInfo cartInfoQuery=new CartInfo();
        cartInfoQuery.setSkuId(skuId);
        cartInfoQuery.setUserId(userId);
        CartInfo cartInfoExists=null;
          cartInfoExists = cartInfoMapper.selectOne(cartInfoQuery);

        SkuInfo skuInfo = manageService.getSkuInfo(skuId);//利用manageService查sku信息

        if(cartInfoExists!=null){
            cartInfoExists.setSkuName(skuInfo.getSkuName());//设置最新名称
            cartInfoExists.setCartPrice(skuInfo.getPrice());//设置最新的价格
            cartInfoExists.setSkuNum(cartInfoExists.getSkuNum()+num);//设置最新数量=传过来的数量+新增数量
            cartInfoExists.setImgUrl(skuInfo.getSkuDefaultImg());//设置图片
            cartInfoMapper.updateByPrimaryKeySelective(cartInfoExists);//selective可以不加
        }

	//没查到加入的物品
	else{
            CartInfo cartInfo = new CartInfo();
            cartInfo.setSkuId(skuId);
            cartInfo.setUserId(userId);
            cartInfo.setSkuNum(num);
            cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfo.setSkuName(skuInfo.getSkuName());
            cartInfo.setCartPrice(skuInfo.getPrice());
            cartInfo.setSkuPrice(skuInfo.getPrice());

            cartInfoMapper.insertSelective(cartInfo);
            cartInfoExists=cartInfo;
        }

        loadCartCache(userId);



        return cartInfoExists;
    }

    @Override
    public List<CartInfo> cartList(String userId) {
        /*先查缓存
	type:	stringiest,zset,list,hash	选择：
		购物车：1一对多关系 2可以改其中一个对象，某一条购物车信息修改
		用string:怎么改某一条购物信息：json串反序列回来，改值，再改成json串
			结果：性能不好
		set,zset,list：都可以散着存。问题：不能直接找到需要改的值
		用hash
			原因：外面有大key,里面有小key，修改效率好
		把key变成userid+skuid, value放购物车信息
			问题：key太多
	Key: 	cart:101:info
	Field	skid
	Value	cartInfoJson

	如果购物车中已有该sku,增加个数，如果没有新增一条	
	*/

        Jedis jedis = redisUtil.getJedis();
        String cartKey="cart:"+userId+":info"; //key: cart:101:info




	/*
	hgetALL: skuid -1:n-cartinfo, 但是不需要skuid
	
	hash： key -1:n-field+value,只需要value.
	使用jedis.hvals
	*/

	//得到Json串
        List<String> cartJsonList = jedis.hvals(cartKey);


        List<CartInfo> cartList=new ArrayList<>();

	//将Json串变成具体的object
        if(cartJsonList!=null&&cartJsonList.size()>0){  //缓存命中
            for (String cartJson : cartJsonList) {
                CartInfo cartInfo = JSON.parseObject(cartJson, CartInfo.class);
                cartList.add(cartInfo);
            }

	/*
	购物车有顺序，新买的放在上面
	怎么判断时间——id号越大，越新——id号大的放在上面
	
	*/
            cartList.sort(new Comparator<CartInfo>() {
                @Override
                public int compare(CartInfo o1, CartInfo o2) {
                    return o2.getId().compareTo(o1.getId());
                }
            });
            return    cartList;
        }else {
            //缓存未命中  //缓存没有查数据库 ，同时加载到缓存中
            return loadCartCache(userId);
        }

    }

    /**
     * 合并购物车
     * @param userIdDest
     * @param userIdOrig
     * @return
     */
    @Override
    public List<CartInfo> mergeCartList(String userIdDest, String userIdOrig) {
        //1 先做合并
        cartInfoMapper.mergeCartList(userIdDest,userIdOrig);
        // 2 合并后把临时购物车删除
        CartInfo cartInfo = new CartInfo();

        cartInfo.setUserId(userIdOrig);//userIdOrig临时的id
        cartInfoMapper.delete(cartInfo);///删除的是第二个购物车，临时的
        Jedis jedis = redisUtil.getJedis();
        jedis.del("cart:"+userIdOrig+":info");
        jedis.close();
        // 3 重新读取数据 加载缓存
        List<CartInfo> cartInfoList = loadCartCache(userIdDest);

        return cartInfoList;
    }



    /**
     *  缓存没有查数据库 ，同时加载到缓存中
     * @param userId
     * @return
     */
    public List<CartInfo>  loadCartCache(String userId){
        // 读取数据库
        List<CartInfo> cartInfoList = cartInfoMapper.selectCartListWithSkuPrice(userId);
        /*
	加载到缓存中
        	为了方便插入redis  把list --> map
	把skuinfo 和cartinfo关联，需要join操作
	建立大sql
	*/
        if(cartInfoList!=null&&cartInfoList.size()>0) {
            Map<String, String> cartMap = new HashMap<>();

            for (CartInfo cartInfo : cartInfoList) {
                cartMap.put(cartInfo.getSkuId(), JSON.toJSONString(cartInfo));
            }

            Jedis jedis = redisUtil.getJedis();
            String cartKey = "cart:" + userId + ":info";
            jedis.del(cartKey);
            jedis.hmset(cartKey, cartMap);                // hash
            jedis.expire(cartKey, 60 * 60 * 24);//过期时间
            jedis.close();
        }
        return  cartInfoList;

    }

    public void  loadCartCacheIfNotExists(String userId){
        String cartkey="cart:"+userId+":info";
        Jedis jedis = redisUtil.getJedis();
        Long ttl = jedis.ttl(cartkey);
        int ttlInt = ttl.intValue();
        jedis.expire(cartkey,ttlInt+10);
        Boolean exists = jedis.exists(cartkey);
        jedis.close();
        if( !exists){
             loadCartCache( userId);
        }

    }


    @Override
    public void checkCart(String userId, String skuId, String isChecked) {


        loadCartCacheIfNotExists(userId);// 检查一下缓存是否存在 避免因为缓存失效造成 缓存和数据库不一致

   
	/*
	isCheck
		数据位置：
			key-field(skuid)-value(属于json串）组成：字段isCheck
			改isCheck步骤
			找到Json串——反序成对象——改对象的值——序列化——写回Json串
		isCheck数据 值保存在缓存中
	先取缓存
		cartkey:来自loadCartCache
	得到jedis	
	*/

        //保存标志
        String cartKey = "cart:" + userId + ":info";
        Jedis jedis = redisUtil.getJedis();

	//得到一条数据的json串
        String cartInfoJson = jedis.hget(cartKey, skuId);
	//反序成对象
        CartInfo cartInfo = JSON.parseObject(cartInfoJson, CartInfo.class);
	//改ischecked值
        cartInfo.setIsChecked(isChecked);
	//序列化
        String cartInfoJsonNew = JSON.toJSONString(cartInfo);
	//写回Json
        jedis.hset(cartKey,skuId,cartInfoJsonNew);


        // 为了订单结账 把所有勾中的商品单独 在存放到一个checked购物车中
        String cartCheckedKey = "cart:" + userId + ":checked";
        if(isChecked.equals("1")){  //勾中加入到待结账购物车中， 取消勾中从待结账购物车中删除
            jedis.hset(cartCheckedKey,skuId,cartInfoJsonNew);
            jedis.expire(cartCheckedKey,60*60); 
        }else{
            jedis.hdel(cartCheckedKey,skuId);
        }
        jedis.close();

    }

    @Override
    public List<CartInfo> getCheckedCartList(String userId) {
	/*
	读Hash
		结构：key -1:n-field 1:1 value
	读取value列表：hvals
	
	*/
        String cartCheckedKey = "cart:" + userId + ":checked";
        Jedis jedis = redisUtil.getJedis();

        List<String> checkedCartList = jedis.hvals(cartCheckedKey);
        List<CartInfo> cartInfoList=new ArrayList<>();
        for (String cartInfoJson : checkedCartList) {
            CartInfo cartInfo = JSON.parseObject(cartInfoJson, CartInfo.class);
            cartInfoList.add(cartInfo);
        }


        jedis.close();

        return cartInfoList;
    }


}
