package com.noob.shardingJdbc.algorithm.config;

import java.text.DecimalFormat;

import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

/**
 * 特征码提取工具  。 保证按外部交易流水号、授信编号、借据号能定位到同一个表库中. 多种场景可能不同的字段去分表，要注意数据表的平均分布。
 * <p>
 * 生成借据号，limit_use表loan_no ，特征码原型是 授信合同号creditContractNo<br>
 * 编号规则为：三位类型码+七位特征码+八位日期+十位流水号（其中前7位是自升序数字，后三位为随机数字），共28位
 * <p>
 * 生成授信合同号，apply_limit表contract_no , 特征码原型是外部传入的tradeFlowNo<br>
 * 编号规则为：三位类型码+七位特征码+八位日期+十位流水号（其中前7位是自升序数字，后三位为随机数字），共28位
 * <p>
 * 提醒消息编号，4固定 + 8日期 + 7流水 + 3随机
 * <p>
 * 支付交易流水号，trade_log表trade_no<br>
 * 编号规则为：三位类型码+八位日期+十位流水号（其中前7位是自升序数字，后三位为随机数字），共21位 *
 */
public abstract class FeatureCodeUtil {

	/**
	 * 提取特征码<br>
	 * 从第四位起7位特征码<br>
	 * 作用于授信合同号、借据号
	 * 
	 * @param columnValue 授信合同号、借据号
	 * @return
	 */
	public static String getFeatureCode(String columnValue) {
		return columnValue.substring(3, 10);
	}

	private final static DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0000000");
	private final static int MAX_FETURE_CODE = 10000000;

	private final static HashFunction murmur = Hashing.murmur3_32();

	/**
	 * 计算三位数字特征码<br>
	 * 作用于进件流水号
	 * 
	 * @param columnValue 进件流水号
	 * @return
	 */
	public static String computeFetureCode(String columnValue) {
		return DECIMAL_FORMAT
				.format(Math.abs(murmur.hashString(columnValue, Charsets.UTF_8).asInt()) % MAX_FETURE_CODE);
	}
	
	public static void main(String[] args) {
		System.out.println(Math.abs(murmur.hashString("20198987626", Charsets.UTF_8).asInt()));
		System.out.println(computeFetureCode("20198987626"));

	}
}
