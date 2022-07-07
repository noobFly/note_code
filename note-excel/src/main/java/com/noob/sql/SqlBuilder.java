package com.noob.sql;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlSchemaStatVisitor;
import com.alibaba.druid.sql.visitor.SchemaStatVisitor;
import com.alibaba.druid.stat.TableStat;
import com.alibaba.druid.util.JdbcConstants;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.List;
import java.util.Map;

/**
 * 分表sql的创建
 * <dependency> <groupId>com.alibaba</groupId> <artifactId>druid</artifactId>
 * <version>1.1.6</version> </dependency>
 *
 */
public class SqlBuilder {
	private static String getContent(String filePath) throws IOException {
		List<String> list = Lists.newArrayList();

		BufferedReader fin = null;
		try {
			fin = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
			String line = null;
			while ((line = fin.readLine()) != null) {
				list.add(line);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (fin != null)
				fin.close();
		}

		return String.join("\n", list);
	}

	private static void out(String sql, String filePath) throws IOException {
		FileOutputStream outFile = null;
		try {

			File file = new File(filePath);
			/* 不要这个判定也可以在write阶段生成新文件
			 * if (!file.exists()) { file.createNewFile(); }
			 */
			outFile = new FileOutputStream(file);
			outFile.write(sql.getBytes("UTF-8"));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (outFile != null)
				outFile.close();
		}

	}
  // 常规建表语句生成32语句
	public static void main(String[] args) throws Exception {

		// 格式化输出
		String result = SQLUtils.format(getContent("C:\\Users\\admin\\Desktop\\origin.sql"), JdbcConstants.MYSQL);
		List<SQLStatement> stmtList = SQLUtils.parseStatements(result, JdbcConstants.MYSQL);
		Map<String, String> sqlTable = Maps.newLinkedHashMap();

		// MySqlSchemaStatVisitor
		for (SQLStatement stmt : stmtList) {
			// MySqlSchemaStatVisitor
			SchemaStatVisitor visitor = new MySqlSchemaStatVisitor();
			stmt.accept(visitor);
			// 获取sql语句操作的表
			Map<TableStat.Name, TableStat> tables = visitor.getTables();
			String tableName = null;
			for (Map.Entry<TableStat.Name, TableStat> nameTableStatEntry : tables.entrySet()) {
				tableName = nameTableStatEntry.getKey().getName().replace("`", "");
			}
			sqlTable.put(SQLUtils.toMySqlString(stmt), tableName);
		}

		List<String> convertLsit = Lists.newArrayList();
		sqlTable.forEach((sql, tableName) -> {
			for (int i = 0; i < 32; i++) {
				String no = i < 10 ? "0" + i : i + "";
				convertLsit.add(StringUtils.replaceOnce(sql, tableName, tableName + "_" + no)); // 字段名有可能包含表名，所以只是覆盖第一个
			}
		});

		out(String.join("\n", convertLsit), "C:\\Users\\admin\\Desktop\\sqlResult.sql");

	}

}
