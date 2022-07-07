package com.noob.easyexcel.demo;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.google.common.base.Strings;
import com.noob.easyexcel.demo.entity.BaseEntity;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class CustomizeEventListener<T extends BaseEntity> extends AnalysisEventListener<T> {
    @Getter
    private List dataList = new ArrayList();
    private String month;
    private String topic;
    private boolean hasNext = true;

    public CustomizeEventListener(String topic, String month) {
        this.month = month;
        this.topic = topic;
    }



    public boolean hasNext(AnalysisContext context) {
        return hasNext;
    }

    @Override
    public void invoke(T data, AnalysisContext context) {
        int rowIndex = context.readRowHolder().getRowIndex();
        if (Strings.isNullOrEmpty(data.getSerialNumber())) {
            log.warn("{} 解析到第{}行数据无效！ 将不在向下扫描！", topic, rowIndex);
            hasNext = false;
        } else {
            data.setMonth(month);
            dataList.add(data);
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        log.info("{} 解析完成，rowIndex:{}, 总计:{}", topic, context.readRowHolder().getRowIndex(), dataList.size());
    }

    @Override
    public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {

    }

}
