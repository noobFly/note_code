package com.noob.workflow;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 流程执行信息Mapper接口
 *
 * @author lfh
 * @date 2023-05-24
 */
public interface WorkflowProcessMapper {

    @Select("<script> select p.*, f.biz_id, f.biz_type, f.workflow_name, f.apply_time as workflow_apply_time ,  f.apply_user as workflow_apply_user "
            + "  a.asset_no , a.asset_name,  from  workflow_process p inner join workflow_instance f on p.flow_id = f.id  <if test = \" bizType != null \"> AND f.biz_type = #{bizType} </if>  "
            + "  inner join gdfae_asset a on a.asset_no = f.asset_no   <if test = \" assetName != null and assetName !='' \"> AND a.asset_name LIKE CONCAT('%', #{assetName} , '%') </if> "
            + "  <where>   "
            + "  <if test = \" opUserId != null  \"> AND p.audit_user = #{opUserId} </if>  "
            + " <if test = \" statusList != null and statusList.size() > 0 \" > and  p.audit_status in ( <foreach collection=\"statusList\" item=\"status\" separator=\",\"> #{status} </foreach> )   </if> "
            + " </where> "
            + " </script>")
    List<WorkflowProcess> selectByParam(@Param("bizType") Integer bizType, @Param("assetName") String assetName, @Param("opUserId") Long opUserId, @Param("statusList") List<Integer> statusList);
}
