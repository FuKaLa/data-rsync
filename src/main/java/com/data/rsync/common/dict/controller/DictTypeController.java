package com.data.rsync.common.dict.controller;

import com.data.rsync.common.dict.entity.DictType;
import com.data.rsync.common.dict.service.DictTypeService;
import com.data.rsync.common.model.Response;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/dict/type")
public class DictTypeController {

    @Resource
    private DictTypeService dictTypeService;

    /**
     * 分页查询字典类型
     * @param page 页码
     * @param size 每页大小
     * @param typeCode 类型编码
     * @param typeName 类型名称
     * @param status 状态
     * @return 分页结果
     */
    @GetMapping("/page")
    public Response<List<DictType>> page(@RequestParam(defaultValue = "1") Integer page,
                                          @RequestParam(defaultValue = "10") Integer size,
                                          @RequestParam(required = false) String typeCode,
                                          @RequestParam(required = false) String typeName,
                                          @RequestParam(required = false) Integer status) {
        DictType dictType = new DictType();
        dictType.setTypeCode(typeCode);
        dictType.setTypeName(typeName);
        dictType.setStatus(status);
        List<DictType> list = dictTypeService.getByCondition(dictType);
        // 简化实现，实际应在Service层实现分页
        return Response.success(list);
    }

    /**
     * 根据ID查询字典类型
     * @param id 主键
     * @return 字典类型
     */
    @GetMapping("/detail")
    public Response<DictType> detail(@RequestParam Long id) {
        DictType dictType = dictTypeService.getById(id);
        return Response.success(dictType);
    }

    /**
     * 新增字典类型
     * @param dictType 字典类型
     * @return 结果
     */
    @PostMapping("/add")
    public Response<String> add(@RequestBody DictType dictType) {
        boolean success = dictTypeService.add(dictType);
        if (success) {
            return Response.success("新增成功");
        } else {
            return Response.failure("新增失败");
        }
    }

    /**
     * 修改字典类型
     * @param dictType 字典类型
     * @return 结果
     */
    @PutMapping("/update")
    public Response<String> update(@RequestBody DictType dictType) {
        boolean success = dictTypeService.update(dictType);
        if (success) {
            return Response.success("修改成功");
        } else {
            return Response.failure("修改失败");
        }
    }

    /**
     * 删除字典类型
     * @param id 主键
     * @return 结果
     */
    @DeleteMapping("/delete")
    public Response<String> delete(@RequestParam Long id) {
        boolean success = dictTypeService.delete(id);
        if (success) {
            return Response.success("删除成功");
        } else {
            return Response.failure("删除失败");
        }
    }

    /**
     * 启用字典类型
     * @param id 主键
     * @return 结果
     */
    @PutMapping("/enable")
    public Response<String> enable(@RequestParam Long id) {
        boolean success = dictTypeService.enable(id);
        if (success) {
            return Response.success("启用成功");
        } else {
            return Response.failure("启用失败");
        }
    }

    /**
     * 禁用字典类型
     * @param id 主键
     * @return 结果
     */
    @PutMapping("/disable")
    public Response<String> disable(@RequestParam Long id) {
        boolean success = dictTypeService.disable(id);
        if (success) {
            return Response.success("禁用成功");
        } else {
            return Response.failure("禁用失败");
        }
    }

    /**
     * 检查类型编码是否存在
     * @param typeCode 类型编码
     * @param excludeId 排除的ID
     * @return 结果
     */
    @GetMapping("/checkCode")
    public Response<Boolean> checkCode(@RequestParam String typeCode,
                                         @RequestParam(required = false) Long excludeId) {
        boolean exist = dictTypeService.checkTypeCodeExist(typeCode, excludeId);
        return Response.success(exist);
    }

    /**
     * 获取所有字典类型列表
     * @return 字典类型列表
     */
    @GetMapping("/list")
    public Response<List<DictType>> list() {
        return Response.success(dictTypeService.getAll());
    }
}
