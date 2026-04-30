package com.slack.slackjarservice.foundation.controller;

import com.slack.slackjarservice.common.base.BaseController;
import com.slack.slackjarservice.common.enumtype.foundation.OperationEnum;
import com.slack.slackjarservice.common.response.ApiResponse;
import com.slack.slackjarservice.common.response.PageResult;
import com.slack.slackjarservice.foundation.model.request.SysDictPageQuery;
import com.slack.slackjarservice.foundation.model.request.SysDictRequest;
import com.slack.slackjarservice.foundation.model.response.SysDictResponse;
import com.slack.slackjarservice.foundation.service.SysDictService;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 数据字典表(SysDict)表控制层
 *
 * @author zhn
 */
@RestController
@RequestMapping("/sys-dict")
public class SysDictController extends BaseController {

    @Resource
    private SysDictService sysDictService;

    /**
     * 保存字典（新增、修改）
     *
     * @param request 字典请求参数
     * @return 操作结果
     */
    @PostMapping("/save")
    public ApiResponse<Boolean> saveDict(@Validated @RequestBody SysDictRequest request) {
        sysDictService.saveDict(request);
        recordOperateLog(OperationEnum.USER_UPSERT, "保存字典成功：" + request.getDictName());
        return success(true);
    }

    /**
     * 删除字典
     *
     * @param id 字典ID
     * @return 操作结果
     */
    @PostMapping("/delete/{id}")
    public ApiResponse<Boolean> deleteDict(@PathVariable Long id) {
        sysDictService.deleteDict(id);
        recordOperateLog(OperationEnum.USER_DELETE, "删除字典成功，ID：" + id);
        return success(true);
    }

    /**
     * 根据ID查询字典
     *
     * @param id 字典ID
     * @return 字典信息
     */
    @GetMapping("/query/{id}")
    public ApiResponse<SysDictResponse> getDictById(@PathVariable Long id) {
        SysDictResponse response = sysDictService.getDictById(id);
        recordOperateLog(OperationEnum.USER_QUERY, "查询字典成功，ID：" + id);
        return success(response);
    }

    /**
     * 根据字典编码查询字典
     *
     * @param dictCode 字典编码
     * @return 字典信息
     */
    @GetMapping("/query/code/{dictCode}")
    public ApiResponse<SysDictResponse> getDictByCode(@PathVariable String dictCode) {
        SysDictResponse response = sysDictService.getDictByCode(dictCode);
        recordOperateLog(OperationEnum.USER_QUERY, "查询字典成功，编码：" + dictCode);
        return success(response);
    }

    /**
     * 分页条件查询字典列表
     *
     * @param query 分页查询条件
     * @return 分页结果
     */
    @PostMapping("/pageQuery")
    public ApiResponse<PageResult<SysDictResponse.DictItem>> pageQueryDicts(@RequestBody SysDictPageQuery query) {
        PageResult<SysDictResponse.DictItem> result = sysDictService.pageQueryDicts(query);
        recordOperateLog(OperationEnum.USER_QUERY, "分页查询字典列表成功");
        return success(result);
    }
}
