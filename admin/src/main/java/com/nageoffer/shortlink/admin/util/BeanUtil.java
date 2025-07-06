package com.nageoffer.shortlink.admin.util;


import com.github.dozermapper.core.DozerBeanMapperBuilder;
import com.github.dozermapper.core.Mapper;
import com.github.dozermapper.core.loader.api.BeanMappingBuilder;
import com.github.dozermapper.core.loader.api.TypeMappingOptions;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.lang.reflect.Array;
import java.util.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BeanUtil {

    protected static Mapper BEAN_MAPPER_BUILDER;

    static{
        BEAN_MAPPER_BUILDER = DozerBeanMapperBuilder.buildDefault();
    }

    public static <T,S> T convert(S source,T target){
        Optional.ofNullable(source).ifPresent(each -> BEAN_MAPPER_BUILDER.map(each,target));
        return target;
    }

    public static <T,S> T convert(S source,Class<T> target){
        return Optional.ofNullable(source).map(each -> BEAN_MAPPER_BUILDER.map(each,target))
                .orElse(null);
    }

    public static <T,S> List<T> convert(List<S> source,Class<T> target){
        return Optional.ofNullable(source).map(each -> {
            ArrayList<T> targetList = new ArrayList<>(each.size());
            each.stream().forEach(item -> targetList.add(BEAN_MAPPER_BUILDER.map(item,target)));
            return targetList;
        }).orElse(null);
    }

    public static <T,S> Set<T> convert(Set<S> source,Class<T> target){
        return Optional.ofNullable(source).map(each -> {
            HashSet<T> targetSet = new HashSet<>(each.size());
            each.stream().forEach(item -> targetSet.add(BEAN_MAPPER_BUILDER.map(item, target)));
            return targetSet;
        }).orElse(null);
    }

    public static <T,S> T[] convert(S[] sources,Class<T> clazz){
        return Optional.ofNullable(sources).map(each -> {
            @SuppressWarnings("uncheked")
            T[] targetArray = (T[]) Array.newInstance(clazz,sources.length);
            for(int i = 0;i < targetArray.length;i ++){
                targetArray[i]  = BEAN_MAPPER_BUILDER.map(sources[i],clazz);
            }
            return targetArray;
        }).orElse(null);
    }

    public static void convertIgnoreNullAndBlank(Object source,Object target){
        DozerBeanMapperBuilder dozerBeanMapperBuilder = DozerBeanMapperBuilder.create();
        Mapper mapper = dozerBeanMapperBuilder.withMappingBuilder(new BeanMappingBuilder() {
            @Override
            protected void configure() {
                mapping(source.getClass(),target.getClass(), TypeMappingOptions.mapNull(false),
                        TypeMappingOptions.mapEmptyString(false)
                );
            }
        }).build();
        mapper.map(source,target);
    }

    public static void convertIgnoreNull(Object source,Object target){
        DozerBeanMapperBuilder dozerBeanMapperBuilder = DozerBeanMapperBuilder.create();
        Mapper mapper = dozerBeanMapperBuilder.withMappingBuilder(new BeanMappingBuilder() {
            @Override
            protected void configure() {
                mapping(source.getClass(),target.getClass(), TypeMappingOptions.mapNull(false));
            }
        }).build();
        mapper.map(source,target);
    }


}
