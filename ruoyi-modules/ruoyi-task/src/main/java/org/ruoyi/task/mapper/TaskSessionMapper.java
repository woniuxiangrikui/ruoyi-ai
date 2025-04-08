package org.ruoyi.task.mapper;

import org.apache.ibatis.annotations.*;
import org.ruoyi.task.domain.TaskSession;

/**
 * 任务会话Mapper接口
 */
@Mapper
public interface TaskSessionMapper {
    
    /**
     * 插入任务会话
     */
    @Insert("INSERT INTO task_session(session_id, user_input, file_path, create_time, status, summary) " +
            "VALUES(#{sessionId}, #{userInput}, #{filePath}, #{createTime}, #{status}, #{summary})")
    int insert(TaskSession session);
    
    /**
     * 根据ID查询会话
     */
    @Select("SELECT * FROM task_session WHERE session_id = #{sessionId}")
    TaskSession selectById(@Param("sessionId") String sessionId);
    
    /**
     * 更新会话信息
     */
    @Update("UPDATE task_session SET status = #{status}, summary = #{summary} WHERE session_id = #{sessionId}")
    int updateById(TaskSession session);
} 