#!/bin/bash

# Slackjar 服务管理脚本
# 使用方法: ./backend.sh [start|stop|restart|status|clean-log]

# 设置日志目录
LOG_DIR="/home/logs"
# 创建日志目录（如果不存在）
mkdir -p $LOG_DIR

# 获取当前时间戳用于日志文件名
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
LOG_FILE="$LOG_DIR/service_$TIMESTAMP.log"

# 应用目录
APP_DIR="/home/slack/slackjar-service"
# PID文件路径
PID_FILE="$LOG_DIR/slackjar-service.pid"
# 应用日志文件路径
APP_LOG_FILE="$LOG_DIR/slackjar-service_$TIMESTAMP.log"

# 记录日志函数
log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" | tee -a $LOG_FILE
}

# 检查应用是否运行
is_app_running() {
    if [ -f "$PID_FILE" ]; then
        PID=$(cat "$PID_FILE")
        if ps -p $PID > /dev/null; then
            return 0  # 运行中
        else
            return 1  # PID文件存在但进程不存在
        fi
    else
        # 检查是否有Java应用进程在运行
        # 使用更精确的匹配，查找包含java和slackjar的进程
        JAVA_PIDS=$(pgrep -f "java.*slackjar")
        if [ -n "$JAVA_PIDS" ]; then
            return 0  # 运行中
        else
            return 1  # 未运行
        fi
    fi
}

# 获取应用PID
get_app_pid() {
    if [ -f "$PID_FILE" ]; then
        PID=$(cat "$PID_FILE")
        if ps -p $PID > /dev/null; then
            echo $PID
            return
        fi
    fi

    # 如果PID文件无效，查找Java应用进程
    JAVA_PIDS=$(pgrep -f "java.*slackjar")
    if [ -n "$JAVA_PIDS" ]; then
        echo $JAVA_PIDS | head -1
        return
    fi

    # 如果都没有找到，返回空
    echo ""
}

# 启动应用
start_app() {
    log "======================== 开始启动 Slackjar 服务 ========================"

    # 检查应用是否已经在运行
    if is_app_running; then
        PID=$(get_app_pid)
        log "应用已经在运行中 (PID: $PID)"
        echo "应用已经在运行中 (PID: $PID)"
        return 0
    fi

    # 进入应用目录
    log "进入应用目录: $APP_DIR"
    cd $APP_DIR || { log "无法进入应用目录: $APP_DIR"; echo "无法进入应用目录: $APP_DIR"; return 1; }

    # 检查是否需要拉取最新代码
    log "检查代码更新..."
    cd /home/slack || { log "无法进入 /home/slack 目录"; echo "无法进入 /home/slack 目录"; return 1; }

    # 恢复 backend.sh 脚本文件到 Git 仓库中的最新版
    log "恢复backend.sh文件..."
    git checkout -- backend.sh >> $LOG_FILE 2>&1
    if [ $? -ne 0 ]; then
        log "Git checkout backend.sh 失败"
        echo "Git checkout backend.sh 失败，请检查日志: $LOG_FILE"
        return 1
    fi

    # 拉取最新代码
    log "拉取最新代码..."
    echo "===== 正在拉取最新代码 ====="
    git pull >> $LOG_FILE 2>&1
    if [ $? -ne 0 ]; then
        log "Git pull 失败"
        echo "Git pull 失败，请检查日志: $LOG_FILE"
        return 1
    fi

    # 显示拉取的文件变更
    echo ""
    echo "===== 代码更新详情 ====="
    echo "最近更新的文件:"
    # 显示最近更新的文件列表
    git log --name-only --pretty=format: -1

    echo ""
    echo "最近提交的变更统计:"
    # 显示最近提交的变更统计
    git log --stat --pretty=format: -1
    echo "======================="

    log "代码更新完成"

    # 进入服务目录
    log "进入服务目录: $APP_DIR"
    cd $APP_DIR || { log "无法进入服务目录: $APP_DIR"; echo "无法进入服务目录: $APP_DIR"; return 1; }

    # 构建应用
    log "开始构建应用(clean package)..."
    mvn clean package -DskipTests >> $LOG_FILE 2>&1
    if [ $? -ne 0 ]; then
        log "Maven 构建失败"
        echo "Maven 构建失败，请检查日志: $LOG_FILE"
        return 1
    fi
    log "应用构建完成"

    # 查找构建的jar文件
    JAR_FILE=$(find "$APP_DIR/target" -name "*.jar" -type f | head -1)
    if [ -z "$JAR_FILE" ]; then
        log "未找到构建的jar文件"
        echo "未找到构建的jar文件"
        return 1
    fi

    log "找到jar文件: $JAR_FILE"

    # 启动应用
    log "启动 Spring Boot 应用..."
    log "应用日志将写入: $APP_LOG_FILE"

    # 使用 nohup 在后台启动应用，并将输出重定向到日志文件
    nohup java -jar "$JAR_FILE" > $APP_LOG_FILE 2>&1 &
    APP_PID=$!

    # 保存PID到文件
    echo $APP_PID > $PID_FILE

    log "应用已启动，PID: $APP_PID"
    echo "应用已启动，PID: $APP_PID"
    echo "应用日志: $APP_LOG_FILE"

    # 应用是否成功启动
    retry_count=0
    max_retries=10

    while [ $retry_count -lt $max_retries ]; do
        if ps -p $APP_PID > /dev/null; then
            log "应用启动成功！"
            echo "应用启动成功！"
            break
        else
            retry_count=$((retry_count + 1))
            if [ $retry_count -ge $max_retries ]; then
                log "应用启动失败，请检查日志: $APP_LOG_FILE"
                echo "应用启动失败，请检查日志: $APP_LOG_FILE"
                return 1
            else
                log "等待应用启动... ($retry_count/$max_retries)"
                sleep 5
            fi
        fi
    done

    log "======================== 启动完成 ========================"
}

# 停止应用
stop_app() {
    log "======================== 开始停止 Slackjar 服务 ========================"

    # 检查应用是否在运行
    if ! is_app_running; then
        log "应用未运行"
        echo "应用未运行"
        return 0
    fi

    # 获取应用PID
    PID=$(get_app_pid)

    # 检查PID是否为空
    if [ -z "$PID" ]; then
        log "无法获取应用PID"
        echo "无法获取应用PID"
        return 1
    fi

    log "找到应用进程 (PID: $PID)"

    # 尝试优雅停止
    log "尝试优雅停止应用 (PID: $PID)..."
    kill -15 $PID >> $LOG_FILE 2>&1

    # 等待进程结束
    sleep 5

    # 检查进程是否仍在运行
    if ps -p $PID > /dev/null; then
        log "应用未正常关闭，强制终止 (PID: $PID)..."
        # 发送SIGKILL信号，强制终止进程
        kill -9 $PID >> $LOG_FILE 2>&1
        sleep 2
    fi

    # 再次检查进程是否已终止
    if ps -p $PID > /dev/null; then
        log "警告: 无法终止应用进程 (PID: $PID)"
        echo "警告: 无法终止应用进程 (PID: $PID)"
        return 1
    else
        log "应用已成功停止"
        echo "应用已成功停止"
        # 删除PID文件
        rm -f "$PID_FILE"
    fi

    log "======================== 停止完成 ========================"
}

# 重启应用
restart_app() {
    log "======================== 开始重启 Slackjar 服务 ========================"

    # 先停止应用
    stop_app

    # 等待几秒钟
    sleep 3

    # 再启动应用
    start_app

    log "======================== 重启完成 ========================"
}

# 查看应用状态
status_app() {
    echo "======================== Slackjar 服务状态 ========================"

    if is_app_running; then
        PID=$(get_app_pid)

        # 检查PID是否为空
        if [ -z "$PID" ]; then
            echo "状态: 运行中但无法获取PID"
        else
            echo "状态: 运行中"
            echo "PID: $PID"

            # 显示内存使用情况
            if command -v ps &> /dev/null; then
                # 获取进程的内存使用百分比和物理内存使用量
                MEM_PERCENT=$(ps -p $PID -o %mem --no-headers 2>/dev/null)
                MEM_RSS=$(ps -p $PID -o rss --no-headers 2>/dev/null)
                if [ -n "$MEM_PERCENT" ] && [ -n "$MEM_RSS" ]; then
                    # 将RSS值从KB转换为MB以便更易读
                    MEM_RSS_MB=$((MEM_RSS / 1024))
                    echo "内存使用: ${MEM_PERCENT}% (${MEM_RSS_MB}MB)"
                fi
            fi

            # 显示运行时间
            if command -v ps &> /dev/null; then
                START_TIME=$(ps -p $PID -o lstart= --no-headers 2>/dev/null)
                if [ -n "$START_TIME" ]; then
                    # 将时间格式化为 年月日 时分秒 格式
                    FORMATTED_TIME=$(date -d "$START_TIME" "+%Y-%m-%d %H:%M:%S" 2>/dev/null || date -j -f "%a %b %d %H:%M:%S %Y" "$START_TIME" "+%Y-%m-%d %H:%M:%S" 2>/dev/null)
                    echo "启动时间: $FORMATTED_TIME"
                fi
            fi
        fi

        # 查找最新的应用日志文件
        if [[ "$OSTYPE" == "darwin"* ]]; then
            # macOS系统
            LATEST_LOG=$(find "$LOG_DIR" -name "slackjar-service_*.log" -type f -exec stat -f "%m %N" {} \; | sort -n | tail -1 | cut -d' ' -f2-)
        else
            # Linux系统
            LATEST_LOG=$(find "$LOG_DIR" -name "slackjar-service_*.log" -type f -printf "%T@ %p\n" | sort -n | tail -1 | cut -d' ' -f2-)
        fi

        # 显示最近的日志
        if [ -n "$LATEST_LOG" ] && [ -f "$LATEST_LOG" ]; then
            echo ""
            echo "最近的日志 (最后5行):"
            tail -n 5 "$LATEST_LOG"
        fi
    else
        echo "状态: 未运行"
    fi

    echo "=============================================="
}

# 清理日志
clean_log() {
    log "======================== 开始清理日志 ========================"

    # 询问用户要保留多少天的日志
    echo "请输入要保留的日志天数 (默认: 30天):"
    read -r DAYS_TO_KEEP
    if [ -z "$DAYS_TO_KEEP" ]; then
        DAYS_TO_KEEP=30
    fi

    # 清理旧日志文件
    log "清理 $DAYS_TO_KEEP 天以前的日志文件..."
    if [[ "$OSTYPE" == "darwin"* ]]; then
        # macOS系统
        find "$LOG_DIR" -name "*.log" -type f -mtime +$DAYS_TO_KEEP -delete >> $LOG_FILE 2>&1
    else
        # Linux系统
        find "$LOG_DIR" -name "*.log" -type f -mtime +$DAYS_TO_KEEP -delete >> $LOG_FILE 2>&1
    fi

    # 计算清理了多少文件
    if [[ "$OSTYPE" == "darwin"* ]]; then
        # macOS系统
        COUNT=$(find "$LOG_DIR" -name "*.log" -type f -mtime +$DAYS_TO_KEEP | wc -l | tr -d ' ')
    else
        # Linux系统
        COUNT=$(find "$LOG_DIR" -name "*.log" -type f -mtime +$DAYS_TO_KEEP | wc -l)
    fi
    log "已清理 $COUNT 个旧日志文件"
    echo "已清理 $COUNT 个旧日志文件"

    # 显示当前日志目录大小
    if command -v du &> /dev/null; then
        DIR_SIZE=$(du -sh "$LOG_DIR" 2>/dev/null | cut -f1)
        echo "当前日志目录大小: $DIR_SIZE"
    fi

    log "======================== 日志清理完成 ========================"
}

# 主程序
case "$1" in
    start)
        start_app
        ;;
    stop)
        stop_app
        ;;
    restart)
        restart_app
        ;;
    status)
        status_app
        ;;
    clean-log)
        clean_log
        ;;
    debug)
        echo "======================== 调试信息 ========================"
        echo "当前目录: $(pwd)"
        echo "应用目录: $APP_DIR"
        echo "日志目录: $LOG_DIR"
        echo "PID文件: $PID_FILE"

        if [ -f "$PID_FILE" ]; then
            echo "PID文件内容: $(cat $PID_FILE)"
        else
            echo "PID文件不存在"
        fi

        echo ""
        echo "所有Java进程:"
        pgrep -f "java" | head -5

        echo ""
        echo "所有包含'slackjar'的进程:"
        pgrep -f "slackjar" | head -5

        echo ""
        echo "所有包含'java.*slackjar'的进程:"
        pgrep -f "java.*slackjar" | head -5

        echo ""
        echo "应用运行状态检查结果:"
        if is_app_running; then
            PID=$(get_app_pid)
            echo "应用正在运行 (PID: $PID)"
        else
            echo "应用未运行"
        fi
        echo "======================================"
        ;;
    *)
        echo "使用方法: $0 {start|stop|restart|status|clean-log|debug}"
        echo ""
        echo "命令说明:"
        echo "  start      - 启动服务"
        echo "  stop       - 停止服务"
        echo "  restart    - 重启服务"
        echo "  status     - 查看服务状态"
        echo "  clean-log  - 清理日志文件"
        echo "  debug      - 显示调试信息"
        exit 1
        ;;
esac

exit 0
