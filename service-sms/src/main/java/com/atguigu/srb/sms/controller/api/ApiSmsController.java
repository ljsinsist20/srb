package com.atguigu.srb.sms.controller.api;

import cn.hutool.core.util.PhoneUtil;
import cn.hutool.core.util.RandomUtil;
import com.atguigu.common.exception.Assert;
import com.atguigu.common.result.R;
import com.atguigu.common.result.ResponseEnum;
import com.atguigu.srb.sms.service.SmsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Api(tags = "短信管理")
@RestController
@RequestMapping("/api/sms")
@CrossOrigin
@Slf4j
public class ApiSmsController {

    @Resource
    private SmsService smsService;

    @Resource
    private RedisTemplate redisTemplate;

    @ApiOperation("获取验证码")
    @GetMapping("send/{mobile}")
    public R send(
            @ApiParam(value = "手机号", required = true)
            @PathVariable String mobile){
        Assert.notEmpty(mobile, ResponseEnum.MOBILE_NULL_ERROR);
        Assert.isTrue(PhoneUtil.isPhone(mobile), ResponseEnum.MOBILE_ERROR);
        Object oldCode =  redisTemplate.opsForValue().get("srb:sms:code:" + mobile);
        if (oldCode != null) {
            return R.ok().message("请在60秒后再次提交");
        }
        String code = RandomUtil.randomNumbers(4);
        redisTemplate.opsForValue().set("srb:sms:code:" + mobile, code, 60, TimeUnit.SECONDS);
        return R.ok().message("短信发送成功" + code);
    }
}
