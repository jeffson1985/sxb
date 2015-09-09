package org.sxb.plugin.activerecord.annotation;


import java.lang.annotation.*;

import org.sxb.plugin.activerecord.generate.DefaultGenerator;
import org.sxb.plugin.activerecord.generate.Generator;

/**
 * Created by son on 14-12-30.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
public @interface TableBind {
  String name();//表名

  String generatedKey() default "id";//自动生成的主键

  Class<? extends Generator> generator() default DefaultGenerator.class;//主键生成策略

  boolean generated() default false;//使用自定义的主键策略生成主键

  String[] primaryKey() default {};//非自动生成的主键放在这儿

  boolean cached() default false;//是否使用缓存
}
