package com.data.rsync.common.dict.controller;

import com.data.rsync.common.dict.entity.DictItem;
import com.data.rsync.common.dict.service.DictItemService;
import com.data.rsync.common.model.Response;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/dict/item")
public class DictItemController {

    @Resource
    private DictItemService dictItemService;

    /**
     * 分页查询字典项
     * @param page 页码
     * @param size 每页大小
     * @param typeId 字典类型ID
     * @param itemCode 字典项编码
     * @param itemName 字典项名称
     * @param status 状态
     * @return 分页结果
     */
    @GetMapping("/page")
    public Response<List<DictItem>> page(@RequestParam(defaultValue = "1") Integer page,
                                          @RequestParam(defaultValue = "10") Integer size,
                                          @RequestParam(required = false) Long typeId,
                                          @RequestParam(required = false) String itemCode,
                                          @RequestParam(required = false) String itemName,
                                          @RequestParam(required = false) Integer status) {
        IPage<DictItem> pageInfo = new Page<>(page, size);
        DictItem dictItem = new DictItem();
        dictItem.setTypeId(typeId);
        dictItem.setItemCode(itemCode);
        dictItem.setItemName(itemName);
        dictItem.setStatus(status);
        // 简化实现，实际应在Service层实现条件查询
        IPage<DictItem> result = dictItemService.page(pageInfo);
        return Response.success(result.getRecords());
    }

    /**
     * 根据ID查询字典项
     * @param id 主键
     * @return 字典项
     */
    @GetMapping("/detail")
    public Response<DictItem> detail(@RequestParam Long id) {
        DictItem dictItem = dictItemService.getById(id);
        return Response.success(dictItem);
    }

    /**
     * 新增字典项
     * @param dictItem 字典项
     * @return 结果
     */
    @PostMapping("/add")
    public Response<String> add(@RequestBody DictItem dictItem) {
        // 检查字典项编码是否存在
        boolean exist = dictItemService.checkItemCodeExist(dictItem.getTypeId(), dictItem.getItemCode(), null);
        if (exist) {
            return Response.failure(400, "字典项编码已存在");
        }
        boolean success = dictItemService.save(dictItem);
        if (success) {
            return Response.success("新增成功");
        } else {
            return Response.failure("新增失败");
        }
    }

    /**
     * 修改字典项
     * @param dictItem 字典项
     * @return 结果
     */
    @PutMapping("/update")
    public Response<String> update(@RequestBody DictItem dictItem) {
        // 检查字典项编码是否存在
        boolean exist = dictItemService.checkItemCodeExist(dictItem.getTypeId(), dictItem.getItemCode(), dictItem.getId());
        if (exist) {
            return Response.failure(400, "字典项编码已存在");
        }
        boolean success = dictItemService.updateById(dictItem);
        if (success) {
            return Response.success("修改成功");
        } else {
            return Response.failure("修改失败");
        }
    }

    /**
     * 删除字典项
     * @param id 主键
     * @return 结果
     */
    @DeleteMapping("/delete")
    public Response<String> delete(@RequestParam Long id) {
        boolean success = dictItemService.removeById(id);
        if (success) {
            return Response.success("删除成功");
        } else {
            return Response.failure("删除失败");
        }
    }

    /**
     * 启用字典项
     * @param id 主键
     * @return 结果
     */
    @PutMapping("/enable")
    public Response<String> enable(@RequestParam Long id) {
        boolean success = dictItemService.enableDictItem(id);
        if (success) {
            return Response.success("启用成功");
        } else {
            return Response.failure("启用失败");
        }
    }

    /**
     * 禁用字典项
     * @param id 主键
     * @return 结果
     */
    @PutMapping("/disable")
    public Response<String> disable(@RequestParam Long id) {
        boolean success = dictItemService.disableDictItem(id);
        if (success) {
            return Response.success("禁用成功");
        } else {
            return Response.failure("禁用失败");
        }
    }

    /**
     * 检查字典项编码是否存在
     * @param typeId 字典类型ID
     * @param itemCode 字典项编码
     * @param excludeId 排除的ID
     * @return 结果
     */
    @GetMapping("/checkCode")
    public Response<Boolean> checkCode(@RequestParam Long typeId,
                                        @RequestParam String itemCode,
                                        @RequestParam(required = false) Long excludeId) {
        boolean exist = dictItemService.checkItemCodeExist(typeId, itemCode, excludeId);
        return Response.success(exist);
    }

    /**
     * 根据字典类型编码获取字典项列表
     * @param typeCode 字典类型编码
     * @return 字典项列表
     */
    @GetMapping("/listByTypeCode")
    public Response<List<DictItem>> listByTypeCode(@RequestParam String typeCode) {
        return Response.success(dictItemService.getByTypeCode(typeCode));
    }

    /**
     * 根据字典类型编码和字典项编码获取字典项
     * @param typeCode 字典类型编码
     * @param itemCode 字典项编码
     * @return 字典项
     */
    @GetMapping("/getByCode")
    public Response<DictItem> getByCode(@RequestParam String typeCode,
                                          @RequestParam String itemCode) {
        return Response.success(dictItemService.getByTypeCodeAndItemCode(typeCode, itemCode));
    }
}
