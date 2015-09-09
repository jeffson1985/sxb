package org.sxb.plugin.activerecord.generate;

import java.util.UUID;

/**
 * @author Dreampie
 * @date 2015-04-30
 * @what 生成主键
 */
public class DefaultGenerator implements Generator {

  /**
	 * 
	 */
	private static final long serialVersionUID = 366432190057630030L;

/**
   * 非自动生成主键的产生策略 UUID
   * 8-4-4-4-12 格式
   *
   * @return object
   */
  public Object generateKey() {
    return UUID.randomUUID().toString();
  }

}
