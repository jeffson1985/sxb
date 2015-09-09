package org.sxb.ext.handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sxb.handler.Handler;
import org.sxb.web.security.XSSRequestWrapper;
/**
 * 跨站脚本攻击 （Cross site Scripting)处理器
 * 请在配置文件中添加此处理器   me.add(new XssHandler("/admin"));
 * Not finished yet
 * @author Sun
 *
 */
public class XssHandler extends Handler {

	// 排除的url，使用的target.startsWith匹配的
    private String exclude;
   
    
    public XssHandler(String exclude) {
        this.exclude = exclude;
    }
    
	@Override
	public void handle(String target, HttpServletRequest request,
			HttpServletResponse response, boolean[] isHandled) {
		// 对于非静态文件，和非指定排除的url实现过滤
		//System.out.println("URL:" + target);
        if (target.indexOf(".") == -1 && !target.startsWith(exclude)){
            request = new XSSRequestWrapper(request);
       }
        nextHandler.handle(target, request, response, isHandled);

	}

}
