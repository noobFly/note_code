package com.noob;

import com.noob.json.JSON;
import lombok.Data;
import org.apache.commons.collections.MapUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
import java.util.Map;

// 用来验证yml配置的Map 和 0x开头的字符不加''会被当做16进制！
@Data
@ConfigurationProperties(prefix = "webase")
@Configuration
public class WeBaseConfig {

	
	// 服务交易地址
	private String transactionUrl;
	// 群组ID
	private int groupId = 1;
	private String userAdderss;
	private String signUserId;
	// 合约名称
	private Map<String, ContractInfo> contractAddressMap;

	// 合约信息
	@Data
	public static class ContractInfo {
		// 合约地址
		private String contractAddress;
		// 合约编译后生成的abi文件内容
		private String contractAbi;
		// 是否使用cns调用
		private boolean useCns = false;
		// cns名称 useCns为true时不能为空
		private String cnsName;
		// cns版本 useCns为true时不能为空
		private String version;

		private List<Object> contractAbiObj;

		public void init() throws IOException {
			contractAbiObj =JSON.parseArray(contractAbi,  Object.class);
		}
	}


	public static void  main(String[] args)   {
		String msg = "[{\"constant\":false,\"inputs\":[{\"name\":\"assetId\",\"type\":\"string\"},{\"name\":\"assetJson\",\"type\":\"string\"}],\"name\":\"storeAsset\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"assetId\",\"type\":\"string\"}],\"name\":\"queryAsset\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"newAdmin\",\"type\":\"address\"}],\"name\":\"addAdmin\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"assetId\",\"type\":\"string\"}],\"name\":\"queryAssetHisotry\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"constructor\"}]";
		List<Object>  contractAbiObj =  JSON.parseArray(msg,  Object.class);
		System.out.print("");
	}

	@PostConstruct
	public void init() {
		if (MapUtils.isNotEmpty(contractAddressMap)) {
			contractAddressMap.forEach((contractName, info) -> {
				try {
					info.init();
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
		}
	}
}
