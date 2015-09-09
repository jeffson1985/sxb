package org.sxb.render;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import net.sf.jasperreports.engine.JRException;

import org.sxb.kit.PathKit;
import org.sxb.log.Logger;
import org.sxb.plugin.activerecord.DbKit;
import org.sxb.render.report.IReportFileExporter;
import org.sxb.render.report.ReportCreater;
import org.sxb.render.report.ReportException;
import org.sxb.render.report.ReportHtmlExporter;
import org.sxb.render.report.ReportPdfExporter;
import org.sxb.render.report.ReportPptxExporter;
import org.sxb.render.report.ReportPrint;
import org.sxb.render.report.ReportExcelExporter;
import org.sxb.render.report.ReportType;
import org.sxb.render.report.ReportWordExporter;
/**
 * SXB报表生成类
 * 此类利用了开源软件JasperReports
 * 因为JasperReports依赖太多的jar包，并且更新太慢
 * 考虑以后重新设计，重写报表生成功能
 * 2015-07-24
 * @author Jeffson
 *
 */
public class ReportRender extends Render {
	private static final Logger logger = Logger.getLogger(ReportRender.class);
	private ReportType format;
	private ReportPrint reportPrint;
	private String webRootPath = PathKit.getWebRootPath();
	private static Map<ReportType, IReportFileExporter> EXPORTER_MAP =
			new HashMap<ReportType, IReportFileExporter>(5);
	private ResultSet rs = null;
	private Collection<Map<String, ?>> dataSource = null;
	
	static {
		EXPORTER_MAP.put(ReportType.EXCEL, new ReportExcelExporter());
		EXPORTER_MAP.put(ReportType.WORD, new ReportWordExporter());
		EXPORTER_MAP.put(ReportType.POWERPOINT, new ReportPptxExporter());
		EXPORTER_MAP.put(ReportType.PDF, new ReportPdfExporter());
		EXPORTER_MAP.put(ReportType.HTML, new ReportHtmlExporter());
	}
	
	public ReportRender(String view, ReportType format){
		this.view = view;
		this.format = format;
	}
	
	public ReportRender(String view, ReportType format, ResultSet rs){
		this(view, format);
		this.rs = rs;
	}
	
	public ReportRender(String view, ReportType format, Collection<Map<String, ?>> dataSource){
		this(view, format);
		this.dataSource = dataSource;
	}
	
	public ReportRender(String view, ReportType format, ReportPrint reportPrint){
		this.view = view;
		this.format = format;
	}
	
	private void exportFile(ReportPrint reportPrint, ReportType format, HttpServletResponse response) {
		try {
			_exportFile(reportPrint, format, response);
		} catch (JRException e) {
			logger.error("导出报表异常", e);
		} catch (IOException e) {
			logger.error(null, e);
		}
	}
	
	private void _exportFile(ReportPrint reportPrint, ReportType format, HttpServletResponse response) throws IOException, JRException {
		OutputStream buffOS = null;
		
		try {
			//buffOS = new BufferedOutputStream(response.getOutputStream());
			IReportFileExporter exporter = null;
			
			if (EXPORTER_MAP.containsKey(format)) {
				exporter = EXPORTER_MAP.get(format);//获取需要格式的导出类
				exporter.export(reportPrint, response);
			} else {
				logger.error("错误的报表格式:" + format);
			}
		} finally {
			if (buffOS != null) {
				buffOS.close();
			}
		}
	}

	@Override
	public void render() {
		// 取得设定值，并传递给JasperReport，这样在在JasperResport studio中就可以通过Map的键值用Parameter属性来引用这些值
		Map<String, Object> param = new HashMap<String, Object>();
		for (Enumeration<String> attrs=request.getAttributeNames(); attrs.hasMoreElements();) {
			String attrName = attrs.nextElement();
			param.put(attrName, request.getAttribute(attrName));
		}
		try {
			if(rs != null){
				
				reportPrint = new ReportCreater().createReport(webRootPath + view, rs,param);
				
			}else if(dataSource != null){
				
				reportPrint = new ReportCreater().createReport(webRootPath + view, dataSource,param);
			}else{
			
				reportPrint = new ReportCreater().createReport(webRootPath + view, DbKit.getConfig().getConnection(),param);
			}
		} catch (ReportException e1) {
			
			e1.printStackTrace();
		} catch (SQLException e1) {
			
			e1.printStackTrace();
		}
		// 此处仅仅设定不缓存项，其他头部信息必须在相应的报表类中设定
		response.setHeader("Pragma", "no-cache");	// HTTP/1.0 caches might not implement Cache-Control and might only implement Pragma: no-cache
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
		exportFile(reportPrint, format, response);
	}
	
}
