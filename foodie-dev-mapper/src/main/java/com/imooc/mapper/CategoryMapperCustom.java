package com.imooc.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface CategoryMapperCustom {

    /**
     *
     * @param rootCatId
     * @return
     */
    public List getSubCatList(Integer rootCatId);

    public List getSixNewItemsLazy(@Param("paramsMap") Map<String,Object> map);

}