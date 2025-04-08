package org.ruoyi.task.mapper;

import org.apache.ibatis.annotations.*;
import org.ruoyi.task.domain.TaskInfo;

import java.util.List;

/**
 * 任务信息Mapper接口
 */
@Mapper
public interface TaskInfoMapper {
    
    /**
     * 插入任务信息
     */
    @Insert("INSERT INTO task_info(task_id, session_id, task_order, description, input_file, output_file, " +
            "use_plugin, plugin_name, plugin_parameters, python_file_path, status, error_message, " +
            "create_time, start_time, end_time) " +
            "VALUES(#{taskId}, #{sessionId}, #{taskOrder}, #{description}, #{inputFile}, #{outputFile}, " +
            "#{usePlugin}, #{pluginName}, #{pluginParameters}, #{pythonFilePath}, #{status}, #{errorMessage}, " +
            "#{createTime}, #{startTime}, #{endTime})")
    int insert(TaskInfo taskInfo);
    
    /**
     * 根据ID查询任务
     */
    @Select("SELECT * FROM task_info WHERE task_id = #{taskId}")
    TaskInfo selectById(@Param("taskId") String taskId);
    
    /**
     * 根据会话ID查询所有任务
     */
    @Select("SELECT * FROM task_info WHERE session_id = #{sessionId} ORDER BY task_order ASC")
    List<TaskInfo> selectBySessionId(@Param("sessionId") String sessionId);
    
    /**
     * 更新任务信息
     */
    @Update("UPDATE task_info SET status = #{status}, error_message = #{errorMessage}, " +
            "python_file_path = #{pythonFilePath}, start_time = #{startTime}, end_time = #{endTime} " +
            "WHERE task_id = #{taskId}")
    int updateById(TaskInfo taskInfo);
} 