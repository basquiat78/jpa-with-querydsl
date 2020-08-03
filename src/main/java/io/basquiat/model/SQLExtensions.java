package io.basquiat.model;

import java.time.LocalDateTime;

import com.querydsl.core.annotations.QueryDelegate;
import com.querydsl.core.annotations.QueryEntity;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.core.types.dsl.StringTemplate;

@QueryEntity
public class SQLExtensions {

	/** =================================================== Brand Entity 위임 메소드 작성 =================================================== */
	
	@QueryDelegate(Brand.class)
    public static <T> Expression<T> constant(QBrand brand, T constant) {
        return Expressions.constant(constant);
    }
	
	@QueryDelegate(Brand.class)
    public static <T> Expression<T> constant(QBrand brand, T constant, String alias) {
        return ExpressionUtils.as(Expressions.constant(constant), alias);
    }
	
	@QueryDelegate(Brand.class)
    public static BooleanExpression codeEq(QBrand brand, String code) {
        return brand.code.eq(code);
    }
	
	@QueryDelegate(Brand.class)
    public static BooleanExpression nameEq(QBrand brand, String name) {
        return brand.name.eq(name);
    }
    
    @QueryDelegate(Brand.class)
    public static BooleanExpression enNameEq(QBrand brand, String enName) {
        return brand.enName.eq(enName);
    }
    
    @QueryDelegate(Brand.class)
    public static BooleanExpression enNameAndNameEq(QBrand brand, String enName, String name) {
        return nameEq(brand, name).and(enNameEq(brand, enName));
    }

    @QueryDelegate(Brand.class)
    public static BooleanExpression enNameOrNameEq(QBrand brand, String enName, String name) {
        return nameEq(brand, name).or(enNameEq(brand, enName));
    }
    
    @QueryDelegate(Brand.class)
    public static StringTemplate dateFormat(QBrand brand, DateTimePath<LocalDateTime> date, String datePattern) {
        return Expressions.stringTemplate("function('date_format', {0}, {1})", date, datePattern);
    }
    
    @QueryDelegate(Brand.class)
    public static StringTemplate replace(QBrand brand, StringPath column, String target, String replace) {
        return Expressions.stringTemplate("function('replace', {0}, {1}, {2})", column, target, replace);
    }
	
    /** =================================================== Partner Entity 위임 메소드 작성 =================================================== */
    
    @QueryDelegate(Partner.class)
    public static BooleanExpression idEq(QPartner partner, String id) {
        return partner.id.eq(id);
    }
    
    @QueryDelegate(Partner.class)
    public static BooleanExpression nameEq(QPartner partner, String name) {
        return partner.name.eq(name);
    }
    
    @QueryDelegate(Partner.class)
    public static BooleanExpression idAndNameEq(QPartner partner, String id, String name) {
        return idEq(partner, id).and(nameEq(partner, name));
    }
    
    @QueryDelegate(Partner.class)
    public static BooleanExpression idOrNameEq(QPartner partner, String id, String name) {
        return idEq(partner, id).or(nameEq(partner, name));
    }
    
}
