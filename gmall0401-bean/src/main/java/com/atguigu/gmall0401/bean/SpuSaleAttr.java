package com.atguigu.gmall0401.bean;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.io.Serializable;
import java.util.List;

@NoArgsConstructor
@Data
public class SpuSaleAttr  implements Serializable {

    @Id
    @Column
    String id ;	//spu保存的时候才生成

    @Column
    String spuId; //spu保存的时候才生成

    @Column
    String saleAttrId;

    @Column
    String saleAttrName;


    @Transient
	//Spu销售属性值
    List<SpuSaleAttrValue> spuSaleAttrValueList;
}

