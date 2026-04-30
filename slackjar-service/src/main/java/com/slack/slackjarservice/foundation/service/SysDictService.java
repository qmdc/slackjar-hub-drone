package com.slack.slackjarservice.foundation.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.slack.slackjarservice.common.response.PageResult;
import com.slack.slackjarservice.foundation.entity.SysDict;
import com.slack.slackjarservice.foundation.model.request.SysDictPageQuery;
import com.slack.slackjarservice.foundation.model.request.SysDictRequest;
import com.slack.slackjarservice.foundation.model.response.SysDictResponse;

import java.util.List;

/**
 * 数据字典表，存储字典类型定义(SysDict)表服务接口
 *
 * @author zhn
 */
public interface SysDictService extends IService<SysDict> {

    /**
     * 保存字典（新增或更新，包含字典项）
     *
     * @param request 字典请求参数
     */
    void saveDict(SysDictRequest request);

    /**
     * 删除字典
     *
     * @param id 字典ID
     */
    void deleteDict(Long id);

    /**
     * 根据ID查询字典
     *
     * @param id 字典ID
     * @return 字典响应
     */
    SysDictResponse getDictById(Long id);

    /**
     * 根据字典编码查询字典
     *
     * @param dictCode 字典编码
     * @return 字典响应
     */
    SysDictResponse getDictByCode(String dictCode);

    /**
     * 获取所有启用的字典列表
     *
     * @return 字典列表
     */
    List<SysDictResponse.DictItem> getAllDicts();

    /**
     * 分页条件查询字典列表
     *
     * @param query 分页查询条件
     * @return 分页结果
     */
    PageResult<SysDictResponse.DictItem> pageQueryDicts(SysDictPageQuery query);
}
