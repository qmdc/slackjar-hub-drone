package com.slack.slackjarservice.foundation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.slack.slackjarservice.common.constant.BaseConstants;
import com.slack.slackjarservice.common.enumtype.foundation.EnableStatusEnum;
import com.slack.slackjarservice.common.enumtype.foundation.ResponseEnum;
import com.slack.slackjarservice.common.exception.BusinessException;
import com.slack.slackjarservice.common.response.PageResult;
import com.slack.slackjarservice.common.util.AssertUtil;
import com.slack.slackjarservice.foundation.dao.SysDictDao;
import com.slack.slackjarservice.foundation.dao.SysDictItemDao;
import com.slack.slackjarservice.foundation.entity.SysDict;
import com.slack.slackjarservice.foundation.entity.SysDictItem;
import com.slack.slackjarservice.foundation.model.request.SysDictPageQuery;
import com.slack.slackjarservice.foundation.model.request.SysDictRequest;
import com.slack.slackjarservice.foundation.model.response.SysDictResponse;
import com.slack.slackjarservice.foundation.service.SysDictItemService;
import com.slack.slackjarservice.foundation.service.SysDictService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 数据字典表，存储字典类型定义(SysDict)表服务实现类
 *
 * @author zhn
 */
@Service("sysDictService")
public class SysDictServiceImpl extends ServiceImpl<SysDictDao, SysDict> implements SysDictService {

    @Resource
    private SysDictItemDao sysDictItemDao;

    @Resource
    private SysDictItemService sysDictItemService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveDict(SysDictRequest request) {
        SysDict dict;
        if (request.getId() != null) {
            // 更新
            dict = this.getById(request.getId());
            AssertUtil.notNull(dict, ResponseEnum.NOT_FOUND);

            // 字典编码不能修改
            AssertUtil.equals(dict.getDictCode(), request.getDictCode(), ResponseEnum.DICT_CODE_NOT_ALLOW_MODIFY);
        } else {
            // 新增时检查字典编码是否重复
            long count = this.count(new LambdaQueryWrapper<SysDict>().eq(SysDict::getDictCode, request.getDictCode()));
            AssertUtil.isTrue(count < 1, ResponseEnum.DICT_CODE_REPEAT);
            // 新增
            dict = new SysDict();
        }

        dict.setDictName(request.getDictName());
        dict.setDictCode(request.getDictCode());
        dict.setDescription(request.getDescription());
        dict.setStatus(request.getStatus() != null ? request.getStatus() : EnableStatusEnum.ENABLE.getCode());
        dict.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : BaseConstants.Digital.ZERO);

        this.saveOrUpdate(dict);

        // 处理字典项
        if (!CollectionUtils.isEmpty(request.getDictItems())) {
            // 校验字典项编码是否重复
            List<String> itemValues = request.getDictItems().stream().map(SysDictRequest.DictItem::getItemValue).toList();
            Integer distinctCount = Math.toIntExact(itemValues.stream().distinct().count());
            AssertUtil.equals(distinctCount, itemValues.size(), ResponseEnum.DICT_CODE_ITEM_REPEAT);

            // 先删除旧的字典项
            sysDictItemDao.delete(new LambdaQueryWrapper<SysDictItem>().eq(SysDictItem::getDictId, dict.getId()));

            // 保存新的字典项
            List<SysDictItem> dictItems = request.getDictItems().stream().map(item -> {
                SysDictItem dictItem = new SysDictItem();
                dictItem.setDictId(dict.getId());
                dictItem.setItemLabel(item.getItemLabel());
                dictItem.setItemValue(item.getItemValue());
                dictItem.setDescription(item.getDescription());
                dictItem.setStatus(item.getStatus() != null ? item.getStatus() : EnableStatusEnum.ENABLE.getCode());
                dictItem.setSortOrder(item.getSortOrder() != null ? item.getSortOrder() : BaseConstants.Digital.ZERO);
                return dictItem;
            }).toList();
            sysDictItemService.saveBatch(dictItems);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDict(Long id) {
        // 删除字典
        this.removeById(id);
        // 删除字典项
        sysDictItemDao.delete(new LambdaQueryWrapper<SysDictItem>().eq(SysDictItem::getDictId, id));
    }

    @Override
    public SysDictResponse getDictById(Long id) {
        SysDict dict = this.getById(id);
        AssertUtil.notNull(dict, ResponseEnum.DICT_CODE_NOT_EXIST);

        SysDictResponse response = new SysDictResponse();
        BeanUtils.copyProperties(dict, response);

        // 查询字典项
        List<SysDictItem> items = sysDictItemDao.selectList(new LambdaQueryWrapper<SysDictItem>()
                .eq(SysDictItem::getDictId, id).orderByAsc(SysDictItem::getSortOrder));
        List<SysDictResponse.DictItemResponse> itemResponses = items.stream().map(item -> {
            SysDictResponse.DictItemResponse itemResponse = new SysDictResponse.DictItemResponse();
            BeanUtils.copyProperties(item, itemResponse);
            return itemResponse;
        }).toList();
        response.setDictItems(itemResponses);

        return response;
    }

    @Override
    public SysDictResponse getDictByCode(String dictCode) {
        SysDict dict = this.getOne(new LambdaQueryWrapper<SysDict>().eq(SysDict::getDictCode, dictCode));
        AssertUtil.notNull(dict, ResponseEnum.DICT_CODE_NOT_EXIST);
        return getDictById(dict.getId());
    }

    @Override
    public List<SysDictResponse.DictItem> getAllDicts() {
        List<SysDict> dicts = this.list(new LambdaQueryWrapper<SysDict>()
                .eq(SysDict::getStatus, 0)
                .orderByAsc(SysDict::getSortOrder));
        return dicts.stream().map(dict -> {
            SysDictResponse.DictItem item = new SysDictResponse.DictItem();
            BeanUtils.copyProperties(dict, item);
            return item;
        }).collect(Collectors.toList());
    }

    @Override
    public PageResult<SysDictResponse.DictItem> pageQueryDicts(SysDictPageQuery query) {
        // 构建分页对象
        Page<SysDict> page = new Page<>(query.getPageNo(), query.getPageSize());

        // 构建查询条件
        LambdaQueryWrapper<SysDict> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getDictName())) {
            wrapper.like(SysDict::getDictName, query.getDictName());
        }
        if (StringUtils.hasText(query.getDictCode())) {
            wrapper.like(SysDict::getDictCode, query.getDictCode().toUpperCase());
        }
        if (Objects.nonNull(query.getStatus())) {
            wrapper.eq(SysDict::getStatus, query.getStatus());
        }
        wrapper.orderByDesc(SysDict::getSortOrder);

        // 执行分页查询
        Page<SysDict> resultPage = this.page(page, wrapper);

        // 转换为响应对象
        List<SysDictResponse.DictItem> records = resultPage.getRecords().stream().map(dict -> {
            SysDictResponse.DictItem item = new SysDictResponse.DictItem();
            BeanUtils.copyProperties(dict, item);
            return item;
        }).collect(Collectors.toList());

        // 构建分页结果
        return PageResult.of(records, resultPage.getTotal(), query.getPageNo(), query.getPageSize());
    }
}
