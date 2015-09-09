package com.demo.config;

import org.sxb.config.Constants;
import org.sxb.config.Handlers;
import org.sxb.config.Interceptors;
import org.sxb.config.Plugins;
import org.sxb.config.Routes;
import org.sxb.config.SxbConfig;
import org.sxb.core.Const;
import org.sxb.core.Sxb;
import org.sxb.ext.handler.ContextPathHandler;
import org.sxb.ext.handler.XssHandler;
import org.sxb.ext.interceptor.SessionInViewInterceptor;
import org.sxb.ext.route.AutoBindRoutes;
import org.sxb.i18n.I18nInterceptor;
import org.sxb.kit.PropKit;
import org.sxb.plugin.activerecord.ActiveRecordPlugin;
import org.sxb.plugin.c3p0.C3p0Plugin;
import org.sxb.plugin.druid.DruidPlugin;
import org.sxb.plugin.druid.DruidStatViewHandler;
import org.sxb.plugin.ehcache.EhCachePlugin;
import org.sxb.plugin.mail.MailPlugin;
import org.sxb.plugin.quartz.QuartzPlugin;

import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.wall.WallFilter;
import com.demo.handler.AcitonExtentionHandler;
import com.google.common.collect.Sets;
/**
 * 必ずSxbConfigを継承し、初期設定を行ってください
 * @author Jeffson
 *
 */
public class DemoConfig extends SxbConfig {

	@Override
	public void configConstant(Constants me) {
		//loadPropertyFile("sxb_config.txt");
		PropKit.use(Sxb.me().getConstants().getDefaultSxbConfigName());
		me.setBaseViewPath("/WEB-INF/templates");
		me.setDevMode(PropKit.getBoolean("system.devMode", false));
		me.setFileRenamePolicy(new MyFileRenamePolicy(true));
		//me.addAllowUploadFileType("pptx");
		me.setAllowUploadFileType(Sets.newHashSet("pptx", "xlsx", "docx"));
	}

	@Override
	public void configRoute(Routes me) {
		// 自动添加路由，如果想手动添加，可以把下面的注释去掉
		//PropKit.use("sxb_config.txt");
		boolean autoScan = PropKit.getBoolean("system.autoBindRoutes", true);
		if(autoScan)
			me.add(new AutoBindRoutes());
		//me.add("/", IndexController.class);
		//me.add("/manage", MainController.class);
		//me.add("/common", CommonController.class);
		//me.add("/manage/admin", AdminController.class);
	}

	@Override
	public void configPlugin(Plugins me) {

		// 配置Druid数据库连接池插件 据称世界最快，具有SQL注入防止功能
		// MySql用
        DruidPlugin dp = new DruidPlugin(PropKit.get("db.jdbcUrl"), PropKit.get("db.user"), PropKit.get("db.password"));
        // 添加SQL监控
        //dp.addFilter(new StatFilter());
        // MariaDB用
        //DruidPlugin dp = new DruidPlugin(PropKit.get("mariadb.jdbcUrl"), PropKit.get("db.user"), PropKit.get("db.password"),PropKit.get("mariadb.class"));
        dp.setTestWhileIdle(true).setTestOnBorrow(true).setTestOnReturn(true);
        dp.addFilter(new StatFilter());
        WallFilter wall = new WallFilter();
        wall.setDbType("mysql");
        dp.addFilter(wall);
        me.add(dp);
		// 配置C3p0数据库连接池插件，大家常用的老牌数据库连接池
        /*
		C3p0Plugin c3p0Plugin = new C3p0Plugin(PropKit.get("db.jdbcUrl"),
				PropKit.get("db.user"), PropKit.get("db.password").trim());
		me.add(c3p0Plugin);
		ActiveRecordPlugin arp = new ActiveRecordPlugin(c3p0Plugin).autoBindModel();
		*/

		// 配置ActiveRecord插件
		// 自动扫描并加载所有包以及jar文件内的模型类
		ActiveRecordPlugin arp = new ActiveRecordPlugin(dp).autoBindModel();
		me.add(arp);
		// 自动扫描指定包内的模型类
		//arp.addIncludeClassPackages("com.demo.model");
		// arp.addMapping("admin", Admin.class);
		// arp.addMapping("role", Role.class);

		// 定时任务处理
		QuartzPlugin quartzPlugin = new QuartzPlugin();
		me.add(quartzPlugin);
		
		MailPlugin mp = new MailPlugin();
		me.add(mp);
		
		// 启用缓存
		me.add(new EhCachePlugin());

	}

	@Override
	public void configInterceptor(Interceptors me) {
		me.add(new I18nInterceptor());
		me.add(new SessionInViewInterceptor());

	}

	@Override
	public void configHandler(Handlers me) {
		me.add(new ContextPathHandler("base"));// 添加项目contextPath,以便在页面直接获取该值
												// ${base?if_exists}
		me.add(new AcitonExtentionHandler()); // 添加html后缀处理
		// 配置SQL状态控制器
		//DruidStatViewHandler dvh = new DruidStatViewHandler("/druid");
		//me.add(dvh);
		// 
		// XSS攻击防御，只针对客户端的请求进行过滤操作
		me.add(new XssHandler("/manage"));

	}

}
