-- 任务模块数据库表结构整合脚本
-- 创建时间：2023-04-07

-- 创建任务会话表
CREATE TABLE IF NOT EXISTS task_session (
    session_id VARCHAR(50) PRIMARY KEY COMMENT '会话ID',
    user_input TEXT NOT NULL COMMENT '用户输入',
    file_path VARCHAR(255) NOT NULL COMMENT '文件路径',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    status VARCHAR(20) NOT NULL COMMENT '状态（PENDING, RUNNING, COMPLETED, FAILED）',
    summary TEXT COMMENT '结果摘要'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务会话表';

-- 创建任务信息表
CREATE TABLE IF NOT EXISTS task_info (
    task_id VARCHAR(50) PRIMARY KEY COMMENT '任务ID',
    session_id VARCHAR(50) NOT NULL COMMENT '会话ID',
    task_order INT NOT NULL COMMENT '任务顺序',
    description TEXT NOT NULL COMMENT '任务描述',
    input_file VARCHAR(255) COMMENT '输入文件',
    output_files TEXT COMMENT '输出文件列表(JSON格式)',
    use_plugin BOOLEAN NOT NULL COMMENT '是否使用插件',
    plugin_name VARCHAR(100) COMMENT '插件名称',
    plugin_parameters TEXT COMMENT '插件参数（JSON格式）',
    python_file_path VARCHAR(255) COMMENT 'Python文件路径',
    status VARCHAR(20) NOT NULL COMMENT '状态（PENDING, RUNNING, COMPLETED, FAILED）',
    error_message TEXT COMMENT '错误信息',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    INDEX idx_session_id (session_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务信息表';

-- 初始化配置数据（如果需要的话）
-- INSERT INTO task_config (config_key, config_value, description) VALUES 
-- ('default_timeout', '60', '默认任务超时时间（秒）'),
-- ('max_concurrent_tasks', '5', '最大并发任务数');

-- 添加存储过程清理过期任务（示例）
DELIMITER //
CREATE PROCEDURE IF NOT EXISTS sp_clean_expired_tasks()
BEGIN
    -- 清理超过30天的已完成任务
    DELETE FROM task_info 
    WHERE status = 'COMPLETED' 
    AND end_time < DATE_SUB(NOW(), INTERVAL 30 DAY);
    
    -- 清理超过7天的失败任务
    DELETE FROM task_info 
    WHERE status = 'FAILED' 
    AND end_time < DATE_SUB(NOW(), INTERVAL 7 DAY);
    
    -- 清理过期的会话
    DELETE FROM task_session 
    WHERE status IN ('COMPLETED', 'FAILED') 
    AND create_time < DATE_SUB(NOW(), INTERVAL 30 DAY);
END //
DELIMITER ;

-- 创建定时事件，每天执行一次清理
CREATE EVENT IF NOT EXISTS evt_clean_expired_tasks
ON SCHEDULE EVERY 1 DAY
DO CALL sp_clean_expired_tasks(); 