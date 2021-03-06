/**
 * 上传包
 * 本框架本着用户体验第一的原则，采用了公认效率第一的RelliyCos上传组件
 * 上传原理与apache upload组件不太相同，
 * 上传文件类型由开发者在前端检查  js，html5的accept等，或者事前利用ajax来判断
 * 因Relliycos的做法是传到自己这里的东西首先上传，然后进行重命名或者更改目录或者添加水印以及改变大小等
 * 
 * 本框架也轻微实现了重命名规则以及文件类型检查，单效率并不太大，因为是先上传然后判断，
 * 不符合则删除返回警告，稍微延迟一些
 * 
 * 如果用户觉着麻烦，考虑版本升级后追加apache upload组件上传插件
 */
package org.sxb.upload;
