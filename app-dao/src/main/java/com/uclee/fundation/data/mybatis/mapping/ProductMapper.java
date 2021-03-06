package com.uclee.fundation.data.mybatis.mapping;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.uclee.fundation.data.mybatis.model.Product;
import com.uclee.fundation.data.web.dto.ProductDto;

public interface ProductMapper {
    int deleteByPrimaryKey(Integer productId);

    int insert(Product record);

    int insertSelective(Product record);

    Product selectByPrimaryKey(Integer productId);

    int updateByPrimaryKeySelective(Product record);

    int updateByPrimaryKey(Product record);

	ProductDto getProductById(Integer productId);

	List<ProductDto> selectOneImage(Integer productId);

	List<ProductDto> getAllProduct(@Param("categoryId")Integer categoryId, @Param("isSaleDesc")Boolean isSaleDesc, @Param("isPriceDesc")Boolean isPriceDesc);
}