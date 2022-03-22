package com.noob;

import com.noob.request.component.BService;
import com.noob.request.component.ExecuteSortComponent;
import com.noob.util.JacksonUtil;
import lombok.Data;
import org.apache.commons.collections.MapUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
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
	
    @Bean(initMethod = "initMethod" )
	public ExecuteSortComponent executeSortComponent(BService b) {
		return new ExecuteSortComponent(b);
	}
	
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
			contractAbiObj = (List<Object>)JacksonUtil.jsonToCollection(contractAbi, List.class, Object.class);
		}
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
