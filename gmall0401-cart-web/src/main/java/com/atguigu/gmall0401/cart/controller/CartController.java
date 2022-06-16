package com.atguigu.gmall0401.cart.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall0401.bean.CartInfo;
import com.atguigu.gmall0401.config.LoginRequire;
import com.atguigu.gmall0401.service.CartService;
import com.atguigu.gmall0401.util.CookieUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.UUID;

@Controller
public class CartController {

    @Reference //后台业务
    CartService cartService;


    @PostMapping("addToCart")

	//如果没登录，会跳转到登录界面.但是购物车允许不登录。可以放行
    @LoginRequire(autoRedirect = false)
	//request,任何请求都经过拦截器。Authinterceptor

    public  String  addCart(@RequestParam("skuId") String skuId, @RequestParam("num") int num, HttpServletRequest request, HttpServletResponse response){
        String userId =(String) request.getAttribute("userId");//userid经过认证才能使用


        if(userId==null){
            //如果用户未登录  检查cookie用户是否有token 如果有token  用token 作为id 加购物车 如果没有生成一个新的token放入cookie

            userId = CookieUtil.getCookieValue(request, "user_tmp_id", false);//得到用户临时id
		//如果临时id还是空的，就生成一个
            if(userId==null){
                userId = UUID.randomUUID().toString();
                CookieUtil.setCookie(request,response,"user_tmp_id",userId,60*60*24*7,false);
            }

        }
        CartInfo cartInfo = cartService.addCart(userId, skuId, num);
        request.setAttribute("cartInfo",cartInfo);
        request.setAttribute("num",num);

        return "success";
    }

    @GetMapping("cartList")
    @LoginRequire(autoRedirect = false)
				//HttpServletRequest:官方参数，如果没有数据就取不出来
    public  String  cartList(HttpServletRequest request){
	
	/*
	传入参数：userid
	参数取自：cookie
		1有token 2有临时购物车的id 
		如果都没有，说明没有购物车
	能取request. request.getAttribute的前提：拦截器认证
		前提：@LoginRequire

	*/

	/*思路
	判断临时购物车：cookie里是否有临时id

	是否登录 - 登录了 -设置cartList=null 是否存在临时购物车 - 存在：合并
						        - cartList=null 或者 没东西-取登录后的购物车
	       - 没登录 - 是否存在临时购物车 - 存在：设置临时购物车为购物车
	

	*/
        String userId =(String) request.getAttribute("userId");  //查看用户登录id

        if(userId!=null){   //有登录
            List<CartInfo> cartList=null;   //如果登录前（未登录）时，存在临时购物车 ，要考虑合并
            String userTmpId=CookieUtil.getCookieValue(request, "user_tmp_id", false); //从cookie里取临时id

		//既没登录，也没临时id
            if(userTmpId!=null){
                List<CartInfo> cartTempList =  cartService.cartList(  userTmpId);  //如果有临时id ，查是否有临时购物车
                if( cartTempList!=null&&cartTempList.size()>0){
                    cartList=  cartService.mergeCartList(userId,userTmpId); // 如果有临时购物车 ，那么进行合并 ，并且获得合并后的购物车列表
                }
            }
            if(cartList==null||cartList.size()==0){
                cartList =  cartService.cartList(  userId);  //如果不需要合并 ，再取登录后的购物车
            }
            request.setAttribute("cartList",cartList);
        }else {   //未登录 直接取临时购物车
            String userTmpId=CookieUtil.getCookieValue(request, "user_tmp_id", false);
            if(userTmpId!=null) {
                List<CartInfo> cartTempList = cartService.cartList(userTmpId);
                request.setAttribute("cartList",cartTempList);
            }

        }

        return "cartList";
    }


    @PostMapping("checkCart")
    @LoginRequire(autoRedirect = false)
    @ResponseBody
    public void checkCart(@RequestParam("isChecked") String isChecked ,@RequestParam("skuId") String skuId,HttpServletRequest request){
        String userId =(String)request.getAttribute("userId");

	//如果userId没有，一定有user_tmp_id,不然购物车没办法取
        if(userId==null){
            userId = CookieUtil.getCookieValue(request, "user_tmp_id", false);
        }

	//后台操作，把购物车勾选状态存起来
        cartService.checkCart(userId,skuId,isChecked);

    }



}
