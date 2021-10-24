package com.atguigu.srb.core.service.impl;

import com.alibaba.excel.EasyExcel;
import com.atguigu.srb.core.listener.ExcelDictDTOListener;
import com.atguigu.srb.core.mapper.DictMapper;
import com.atguigu.srb.core.pojo.dto.ExcelDictDTO;
import com.atguigu.srb.core.pojo.entity.Dict;
import com.atguigu.srb.core.service.DictService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 数据字典 服务实现类
 * </p>
 *
 * @author ljs
 * @since 2021-10-17
 */
@Slf4j
@Service
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict> implements DictService {

    @Resource
    private RedisTemplate redisTemplate;

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public void importData(InputStream inputStream) {
        EasyExcel.read(inputStream, ExcelDictDTO.class, new ExcelDictDTOListener(baseMapper)).sheet().doRead();
        log.info("importData finished");
    }

    @Override
    public List<ExcelDictDTO> listDictData() {
        List<Dict> dictList  = baseMapper.selectList(null);
        ArrayList<ExcelDictDTO> excelDictDTOList  = new ArrayList<>(dictList.size());
        dictList.forEach(dict -> {
            ExcelDictDTO excelDictDTO = new ExcelDictDTO();
            BeanUtils.copyProperties(dict, excelDictDTO);
            excelDictDTOList.add(excelDictDTO);
        });
        return excelDictDTOList;
    }

    @Override
    public List<Dict> listByParentId(Long parentId) {
        List<Dict> dictList = null;
        //redis查询
        dictList = (List<Dict>) redisTemplate.opsForValue().get("srb:core:dictList:" + parentId);
        try {
            if (dictList != null) {
            log.info("从redis中取值");
            return dictList;
        }
        } catch (Exception e) {
            //此处不抛出异常，继续执行后面的代码
            log.error("redis服务器异常：" + ExceptionUtils.getStackTrace(e));
        }
        //数据库查询
//        Wrapper<Dict> queryWrapper = new QueryWrapper<Dict>().eq("parent_id", parentId);
        dictList = baseMapper.selectList(new QueryWrapper<Dict>().eq("parent_id", parentId));
        dictList.forEach(dict -> {
            boolean flag = this.hasChildren(dict.getId());
            dict.setHasChildren(flag);
        });

        //数据存入redis
        try {
            redisTemplate.opsForValue().set("srb:core:dictList:" + parentId, dictList, 5, TimeUnit.MINUTES);
            log.info("数据存入redis");
        }catch (Exception e) {
            log.error("redis服务器异常：" + ExceptionUtils.getStackTrace(e));
        }

        return dictList;
    }

    private boolean hasChildren(Long id) {
        Integer count = baseMapper.selectCount(new QueryWrapper<Dict>().eq("parent_id", id));
        if (count.intValue() > 0) {
            return true;
        }
        return false;
    }
}