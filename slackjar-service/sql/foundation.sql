create table slackjar_drone.sys_user
(
    id              bigint auto_increment comment '用户ID'
        primary key,
    username        varchar(50)       not null comment '用户名（唯一）',
    password        varchar(100)      not null comment '密码（加密存储）',
    nickname        varchar(50)       null comment '用户昵称',
    email           varchar(100)      null comment '邮箱',
    phone           varchar(20)       null comment '手机号',
    status          tinyint default 0 not null comment '状态（0-正常，1-禁用）',
    avatar_id       bigint            null comment '头像图片ID（关联sys_file表）',
    background_id   bigint            null comment '背景图片ID（关联sys_file表）',
    last_login_time bigint            null comment '最后登录时间（毫秒时间戳）',
    ip              varchar(50)       null comment '最后登录IP',
    create_time     bigint            null comment '创建时间（毫秒时间戳）',
    update_time     bigint            null comment '更新时间（毫秒时间戳）',
    deleted         tinyint default 0 not null comment '逻辑删除（0-未删，1-已删）',
    version         bigint  default 1 not null comment '版本号（用于乐观锁）'
)
    comment '系统用户表';

    create table slackjar_drone.role
(
    id          bigint auto_increment comment '角色ID'
        primary key,
    role_name   varchar(50)       not null comment '角色名称（如：管理员、普通用户）',
    role_code   varchar(50)       not null comment '角色编码（如：ROLE_ADMIN，唯一标识）',
    description varchar(200)      null comment '角色描述',
    role_type   tinyint default 1 not null comment '角色类型（1-系统角色，2-自定义角色）',
    status      tinyint default 0 not null comment '状态（0-启用，1-禁用）',
    sort_order  int     default 0 not null comment '排序号',
    create_time bigint            null comment '创建时间（毫秒时间戳）',
    update_time bigint            null comment '更新时间（毫秒时间戳）',
    deleted     tinyint default 0 not null comment '逻辑删除（0-未删，1-已删）',
    version     bigint  default 1 not null comment '版本号（用于乐观锁）'
)
    comment '角色信息表，存储系统中所有角色定义';

create table slackjar_drone.user_role
(
    id          bigint auto_increment comment '关联记录ID'
        primary key,
    user_id     bigint            not null comment '用户ID（关联用户表主键）',
    role_id     bigint            not null comment '角色ID（关联角色表主键）',
    create_time bigint            null comment '创建时间（毫秒时间戳）',
    update_time bigint            null comment '更新时间（毫秒时间戳）',
    deleted     tinyint default 0 not null comment '逻辑删除（0-未删，1-已删）',
    version     bigint  default 1 not null comment '版本号（用于乐观锁）'
)
    comment '用户与角色的关联表，维护多对多关系';

create table slackjar_drone.permission
(
    id               bigint auto_increment comment '权限ID'
        primary key,
    permission_name  varchar(50)       not null comment '权限名称（如：用户管理、查看订单）',
    permission_code  varchar(50)       not null comment '权限编码（如：user:manage、order:view，唯一标识）',
    description      varchar(200)      null comment '权限描述',
    permission_type  tinyint default 1 not null comment '权限类型（1-菜单权限，2-按钮权限，3-接口权限）',
    parent_id        bigint            null comment '父权限ID（用于构建权限树）',
    sort_order       int     default 0 not null comment '排序号',
    create_time      bigint            null comment '创建时间（毫秒时间戳）',
    update_time      bigint            null comment '更新时间（毫秒时间戳）',
    deleted          tinyint default 0 not null comment '逻辑删除（0-未删，1-已删）',
    version          bigint  default 1 not null comment '版本号（用于乐观锁）'
)
    comment '权限信息表，存储系统中所有权限定义';

create table slackjar_drone.role_permission
(
    id            bigint auto_increment comment '关联记录ID'
        primary key,
    role_id       bigint            not null comment '角色ID（关联角色表主键）',
    permission_id bigint            not null comment '权限ID（关联权限表主键）',
    create_time   bigint            null comment '创建时间（毫秒时间戳）',
    update_time   bigint            null comment '更新时间（毫秒时间戳）',
    deleted       tinyint default 0 not null comment '逻辑删除（0-未删，1-已删）',
    version       bigint  default 1 not null comment '版本号（用于乐观锁）'
)
    comment '角色与权限的关联表，维护多对多关系';

create table slackjar_drone.sys_file
(
    id             bigint auto_increment comment '文件ID'
        primary key,
    file_name      varchar(255)       null comment '文件名',
    user_id        bigint             not null comment '用户ID',
    file_path      varchar(512)       not null comment '文件全路径(不含域名)',
    thumbnail_path varchar(512)       null comment '缩略文件路径',
    file_size      bigint             not null comment '文件大小（字节）',
    file_type      varchar(50)        not null comment '文件类型（如image/jpeg）',
    file_md5       varchar(100)       null comment '文件md5值',
    storage_type   varchar(50)        null comment '文件存储类型（如：aliyun）',
    biz_type       varchar(50)        null comment '业务类型（如：user-avatar、video）',
    access_level   tinyint            null comment '访问级别（0-私有、1-公开、2-指定用户可见）',
    audit_status   tinyint            null comment '审核状态（0-待审核、1-审核通过、2-审核拒绝）',
    download_count int     default 0  not null comment '下载次数',
    upload_status  int     default 0  not null comment '上传状态（tinyint，0-上传中、1-上传完成、2-上传失败）',
    expired        bigint  default -1 not null comment '过期时间(毫秒时间戳),-1代表不过期',
    create_time    bigint             null comment '创建时间（毫秒时间戳）',
    update_time    bigint             null comment '更新时间（毫秒时间戳）',
    deleted        tinyint default 0  not null comment '逻辑删除（0-未删，1-已删）',
    version        bigint  default 1  not null comment '版本号（用于乐观锁）'
)
    comment '系统文件表';

create table slackjar_drone.ssl_certificate
(
    id            bigint auto_increment comment '主键ID'
        primary key,
    domain        varchar(255)      not null comment '域名',
    ssl_cert_key  text              null comment 'ssl证书key',
    ssl_cert_pem  text              null comment 'ssl证书pem',
    ssl_cert_path varchar(512)      null comment 'SSL证书存放目录',
    apply_time    bigint            null comment '申请时间',
    validity_days int               null comment 'ssl证书有效期（天）',
    cert_source   tinyint           null comment '证书来源 0-腾讯云 1-阿里云 2-华为云 3-其他',
    console_wiki  varchar(512)      null comment '控制台文档',
    create_time   bigint            null comment '创建时间（毫秒时间戳）',
    update_time   bigint            null comment '更新时间（毫秒时间戳）',
    deleted       tinyint default 0 not null comment '逻辑删除（0-未删，1-已删）',
    version       bigint  default 1 not null comment '版本号（用于乐观锁）'
)
    comment 'SSL证书信息表';

create table slackjar_drone.sys_config
(
    id           bigint auto_increment comment '主键ID'
        primary key,
    config_key   varchar(100)      not null comment '配置键名',
    config_value text              null comment '配置值',
    category     varchar(50)       not null comment '配置分类',
    description  varchar(500)      null comment '配置描述',
    status       tinyint default 0 not null comment '启用状态（1：禁用、0启用）',
    create_time  bigint            null comment '创建时间（毫秒时间戳）',
    update_time  bigint            null comment '更新时间（毫秒时间戳）',
    deleted      tinyint default 0 not null comment '逻辑删除（0-未删，1-已删）',
    version      bigint  default 1 not null comment '版本号（用于乐观锁）'
)
    comment '系统参数配置表';

create table slackjar_drone.sys_dict
(
    id          bigint auto_increment comment '字典ID'
        primary key,
    dict_name   varchar(50)       not null comment '字典名称（如：性别、状态）',
    dict_code   varchar(50)       not null comment '字典编码（如：gender、status，唯一标识）',
    description varchar(200)      null comment '字典描述',
    status      tinyint default 0 not null comment '状态（0-启用，1-禁用）',
    sort_order  int     default 0 not null comment '排序号',
    create_time bigint            null comment '创建时间（毫秒时间戳）',
    update_time bigint            null comment '更新时间（毫秒时间戳）',
    deleted     tinyint default 0 not null comment '逻辑删除（0-未删，1-已删）',
    version     bigint  default 1 not null comment '版本号（用于乐观锁）'
)
    comment '数据字典表，存储字典类型定义';

create table slackjar_drone.sys_dict_item
(
    id          bigint auto_increment comment '字典项ID'
        primary key,
    dict_id     bigint            not null comment '字典ID（关联sys_dict表主键）',
    item_label  varchar(200)       not null comment '字典项标签（如：男、女）',
    item_value  varchar(2048)       not null comment '字典项值（如：1、2）',
    description varchar(200)      null comment '字典项描述',
    status      tinyint default 0 not null comment '状态（0-启用，1-禁用）',
    sort_order  int     default 0 not null comment '排序号',
    create_time bigint            null comment '创建时间（毫秒时间戳）',
    update_time bigint            null comment '更新时间（毫秒时间戳）',
    deleted     tinyint default 0 not null comment '逻辑删除（0-未删，1-已删）',
    version     bigint  default 1 not null comment '版本号（用于乐观锁）'
)
    comment '数据字典项表，存储字典项定义';

-- 用户设备登录记录表
create table slackjar_drone.`user_device` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT comment '主键ID',
    `user_id` BIGINT NOT NULL comment '用户ID',
    `token_value` VARCHAR(255) NOT NULL comment 'Sa-Token 的 token 值',
    `device` VARCHAR(50) NOT NULL comment '设备标识，如 PC、MAC、MOBILE',
    `ip_addr` VARCHAR(50) comment '登录IP',
    `location` VARCHAR(100) comment '登录地点',
    `browser` VARCHAR(50) comment '浏览器',
    `os` VARCHAR(50) comment '操作系统',
    `login_time` DATETIME comment '登录时间',
    `expire_time` DATETIME comment '过期时间',
    `status`      tinyint default 0 null comment '生效状态 0-生效、1-失效',
    `create_time` BIGINT NOT NULL DEFAULT 0 comment '创建时间（毫秒时间戳）',
    `update_time` BIGINT NOT NULL DEFAULT 0 comment '更新时间（毫秒时间戳）',
    `deleted` INT NOT NULL DEFAULT 0 comment '逻辑删除（0-未删，1-已删）',
    `version` BIGINT NOT NULL DEFAULT 1 comment '版本号（用于乐观锁）',
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_token_value` (`token_value`)
)
    comment='用户设备登录记录表';
