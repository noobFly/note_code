CREATE TABLE `data_check_column_mapping` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '编号',
	`topic` TINYINT(4) NOT NULL COMMENT '主题',
	`title` VARCHAR(100) NOT NULL COMMENT 'Excel列名',
	`column_name` VARCHAR(100) NOT NULL COMMENT '关联的字段名',
	`data_type` VARCHAR(50) NOT NULL DEFAULT '0' COMMENT '数据类型 ',
	`primary_key` CHAR(1) NOT NULL COMMENT '是否主键',
	`extra` VARCHAR(200) NULL DEFAULT NULL COMMENT '扩展属性',
	PRIMARY KEY (`id`) USING BTREE,
	UNIQUE INDEX `uk_column_mapping` (`topic`, `column_name`) USING BTREE
)
COMMENT='数据核对字段配置表'
AUTO_INCREMENT=1000
;



CREATE TABLE `data_check_table_mapping` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '编号',
	`topic` TINYINT(4) NOT NULL COMMENT '主题： 1 产基项目',
	`sheet_name` VARCHAR(100) NOT NULL DEFAULT '' COMMENT 'excel表',
	`table_name` VARCHAR(200) NOT NULL COMMENT '关联的数据表',
	`head_index` VARCHAR(16) NOT NULL DEFAULT '0' COMMENT '标题行, 支持多个标题行合并',
	`data_start_index` TINYINT(4) NOT NULL DEFAULT '1' COMMENT '数据开始行',
	`create_by` VARCHAR(64) NULL DEFAULT NULL COMMENT '创建者',
	`create_time` DATETIME NOT  NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
	`update_by` VARCHAR(64) NULL DEFAULT NULL COMMENT '更新者',
	`update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
	`remark` VARCHAR(100) NULL DEFAULT NULL COMMENT '备注',
	PRIMARY KEY (`id`) USING BTREE,
	UNIQUE INDEX `uk_table_mapping` (`topic`) USING BTREE
)
COMMENT='数据核对表配置表'
ENGINE=InnoDB
AUTO_INCREMENT=1000
;


INSERT INTO `data_check_table_mapping` ( `topic`, `sheet_name`, `table_name`, `head_index`, `data_start_index`) VALUES
( 1, '项目情况表', 'view_fund_activity_details', '1,2,3', 6);

INSERT INTO `data_check_column_mapping` (`topic`, `title`, `column_name`, `data_type`, `primary_key`, `extra`) VALUES
( 1, '基金名称', 'fund_name', 'DEFAULT', 'Y', ''),
( 1, '项目公司名称', 'project_name', 'DEFAULT', 'Y', ''),
( 1, '投资时间', 'invest_date', 'DATE', 'Y', '{"dateModel":"array","pattern":"yyyy-MM-dd","split":",|;|；"}'),
( 1, '最新持股比例', 'shareholding_ratio', 'NUMBER', '', '{"scale":"2","roundingMode":"4","defaultValue":"0","clear":"%|-|/"}'),
( 1, '签约投资金额', 'sign_amount', 'NUMBER', '', '{"scale":"2","roundingMode":"4","defaultValue":"0","clear":",|-|/"}'),
( 1, '员工跟投额', 'follow_invest_amount', 'NUMBER', '', '{"scale":"2","roundingMode":"4","defaultValue":"0","clear":",|-|/"}'),
( 1, '实退出日期', 'exit_date', 'DATE', '', '');

