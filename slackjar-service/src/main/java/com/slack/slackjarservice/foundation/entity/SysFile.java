package com.slack.slackjarservice.foundation.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.slack.slackjarservice.common.base.BaseModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 系统文件表(SysFile)表实体类
 *
 * @author zhn
 * @since 2025-08-19 11:11:27
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("sys_file")
public class SysFile extends BaseModel {
    /**
     * 文件ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 文件名
     */
    private String fileName;
    /**
     * 用户ID
     */
    private Long userId;
    /**
     * 文件全路径(不含域名)
     */
    private String filePath;
    /**
     * 缩略文件路径
     */
    private String thumbnailPath;
    /**
     * 文件大小（字节）
     */
    private Long fileSize;
    /**
     * 文件类型（如image/jpeg）
     */
    private String fileType;
    /**
     * 文件md5值
     */
    private String fileMd5;
    /**
     * 文件存储类型（如：aliyun）
     */
    private String storageType;
    /**
     * 业务文件类型（如：avatar、video）
     */
    private String bizType;
    /**
     * 访问级别（0-私有、1-公开、2-指定用户可见）
     */
    private Integer accessLevel;
    /**
     * 审核状态（0-待审核、1-审核通过、2-审核拒绝）
     */
    private Integer auditStatus;
    /**
     * 下载次数
     */
    private Integer downloadCount;
    /**
     * 上传状态（tinyint，0-上传中、1-上传完成、2-上传失败）
     */
    private Integer uploadStatus;
    /**
     * 过期时间(毫秒时间戳),-1代表不过期
     */
    private Long expired;
}

