package org.sxb.render.report;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRResultSetDataSource;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

import org.sxb.core.Sxb;
import org.sxb.log.Logger;
/**
 *  报表中间件统一生成类
 * @author Sun
 *
 */
public class ReportCreater {
	private static final Logger logger = Logger.getLogger(ReportCreater.class);
	private boolean devMode = Sxb.me().getConstants().getDevMode();
	/**
	 * jasperDesignMap作为一个缓存来存储编译后的JasperReport模板
	 * 此处直接将模版放入内存缓存，如果报表数量巨大时，需要重新设计，一次编译全部，然后硬盘保存
	 */
	private Map<String, JasperReport> jasperDesignMap = new ConcurrentHashMap<String, JasperReport>();

	public ReportCreater() {
		// 如果时开发模式，清除缓存
		if (devMode) {

			logger.info("清除报表模版缓存");
			resetJasperDesignCache();
		}
	}

	// 如果不想缓存模版对象，这调用此方法，比如开发模式时
	public void resetJasperDesignCache() {

		jasperDesignMap.clear();

	}

	/**
	 * ReportRender调用该方法来产生ReportPrint对象 自己定义业务处理SQL，将执行后的结果集传递给JasperReport
	 * 利用JasperReport stdio设计报表时，需要的动态字段必须跟结果集的字段名称一致
	 * 
	 * @param view
	 * @param rs
	 * @param reportParams
	 * @return
	 * @throws ReportException
	 */
	public ReportPrint createReport(final String view, final ResultSet rs,
			Map<String, Object> reportParams) throws ReportException {
		
		try {
			return _createReport(view, rs, reportParams);
		} catch (JRException e) {
			logger.error(null, e);
			throw new ReportException("产生报表出错" + view);
		}
	}

	/**
	 * ReportRender调用该方法来产生ReportPrint对象 此处仅仅把JDBC 连接传递给JasperReport
	 * 所有的业务处理SQL等，必须在JasperReport Stdio中完成
	 * 
	 * @param view
	 * @param conn
	 * @param reportParams
	 * @return
	 * @throws ReportException
	 */
	public ReportPrint createReport(final String view,
			final java.sql.Connection conn, Map<String, Object> reportParams)
			throws ReportException {
		
		try {
			return _createReport(view, conn, reportParams);
		} catch (JRException e) {
			logger.error(null, e);
			throw new ReportException("产生报表出错" + view);
		}
	}

	/**
	 * ReportRender调用该方法来产生ReportPrint对象 业务处理SQl以及结果集的处理均在程序中处理
	 * 处理后的集合交给JasperReport处理
	 * 
	 * @param view
	 * @param data
	 * @param reportParams
	 * @return
	 * @throws ReportException
	 */
	public ReportPrint createReport(final String view,
			final Collection<Map<String, ?>> data,
			Map<String, Object> reportParams) throws ReportException {
		
		try {
			return _createReport(view, data, reportParams);
		} catch (JRException e) {
			logger.error(null, e);
			throw new ReportException("产生报表出错" + view);
		}
	}

	// 以下是实现方法，不一一做详细说明

	private ReportPrint _createReport(final String view, final ResultSet rs,
			Map<String, Object> reportParams) throws ReportException,
			JRException {
		JasperReport jasperReport = getJasperReport(view);
		ReportPrint reportPrint = new ReportPrint();
		JRResultSetDataSource resultSetDataSource = new JRResultSetDataSource(
				rs);
		JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport,
				reportParams, resultSetDataSource);
		reportPrint.setJasperPrint(jasperPrint);

		return reportPrint;
	}

	private ReportPrint _createReport(final String view,
			final Collection<Map<String, ?>> data,
			Map<String, Object> reportParams) throws ReportException,
			JRException {
		JasperReport jasperReport = getJasperReport(view);
		ReportPrint reportPrint = new ReportPrint();
		JRMapCollectionDataSource resultSetDataSource = new JRMapCollectionDataSource(
				data);
		JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport,
				reportParams, resultSetDataSource);
		reportPrint.setJasperPrint(jasperPrint);

		return reportPrint;
	}

	private ReportPrint _createReport(final String view,
			final java.sql.Connection conn, Map<String, Object> reportParams)
			throws ReportException, JRException {
		JasperReport jasperReport = getJasperReport(view);
		ReportPrint reportPrint = new ReportPrint();
		JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport,
				reportParams, conn);
		reportPrint.setJasperPrint(jasperPrint);

		return reportPrint;
	}

	private JasperReport getJasperReport(final String view) {
		try {
			return _getJasperReport(view);
		} catch (IOException e) {
			logger.error(null, e);
			throw new ReportException("关闭文件流异常:" + view);
		} catch (JRException e) {
			logger.error(null, e);
			throw new ReportException("产生报表异常:" + view);
		}
	}

	private JasperReport _getJasperReport(final String view)
			throws IOException, JRException {
		JasperReport jasperReport = null;
		if (jasperDesignMap.containsKey(view)) {
			jasperReport = jasperDesignMap.get(view);
		} else {
			jasperReport = getJasperReportFromFile(view);
			jasperDesignMap.put(view, jasperReport);
		}

		return jasperReport;
	}

	/**
	 * 模版预处理 1 如果不存在jsper中间件则从模板文件编译获得模板对象 2 如果存在，则直接返回模版对象
	 */
	private JasperReport getJasperReportFromFile(final String view)
			throws IOException, JRException {
		String filePath = view + ".jasper"; // 报表中间件
		File reportFile = new File(filePath);
		InputStream jasperFileIS = null;
		JasperReport jasperReport = null;
		// 如果存在报表中间件，优先使用
		if (reportFile.exists()) {
			jasperReport = (JasperReport) JRLoader.loadObject(reportFile);
			return jasperReport;
		}

		// 如果不存在报表中间件，则读取报表格式化文件，编译成中间件返回
		filePath = view + ".jrxml";
		try {
			reportFile = new File(filePath);
			if (!reportFile.exists()) {
				throw new ReportException("报表文件不存在:" + filePath);
			}

			jasperFileIS = new FileInputStream(reportFile);
			JasperDesign jasperDesign = JRXmlLoader.load(jasperFileIS);

			jasperReport = JasperCompileManager.compileReport(jasperDesign);
			// 编译生成中间件文件**.jasper
			// 如果是开发模式则忽略不生成
			if(!devMode)
				JasperCompileManager.compileReportToFile(filePath);
		} finally {
			if (jasperFileIS != null) {
				jasperFileIS.close();
			}
		}

		return jasperReport;
	}
}
