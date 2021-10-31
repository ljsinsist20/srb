package com.atguigu.srb.core.controller.api;


import cn.hutool.core.util.PhoneUtil;
import com.atguigu.common.exception.Assert;
import com.atguigu.common.result.R;
import com.atguigu.common.result.ResponseEnum;
import com.atguigu.srb.base.util.JwtUtils;
import com.atguigu.srb.core.pojo.vo.LoginVO;
import com.atguigu.srb.core.pojo.vo.RegisterVO;
import com.atguigu.srb.core.pojo.vo.UserInfoVO;
import com.atguigu.srb.core.service.UserInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * 用户基本信息 前端控制器
 * </p>
 *
 * @author ljs
 * @since 2021-10-17
 */
@Api(tags = "会员接口")
@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/api/core/userInfo")
public class UserInfoController {

    @Resource
    private UserInfoService userInfoService;


    @Resource
    private RedisTemplate redisTemplate;

    @ApiOperation("会员注册")
    @PostMapping("/register")
    public R register(@RequestBody RegisterVO registerVO) {
        String mobile = registerVO.getMobile();
        String code = registerVO.getCode();
        String password = registerVO.getPassword();
        Assert.notEmpty(mobile, ResponseEnum.MOBILE_NULL_ERROR);
        Assert.isTrue(PhoneUtil.isPhone(mobile), ResponseEnum.MOBILE_ERROR);
        Assert.notEmpty(password, ResponseEnum.PASSWORD_NULL_ERROR);
        Assert.notEmpty(code, ResponseEnum.CODE_NULL_ERROR);

        String codeGen = (String) redisTemplate.opsForValue().get("srb:sms:code:" + mobile);
        Assert.equals(code, codeGen, ResponseEnum.CODE_ERROR);

        userInfoService.register(registerVO);
        return R.ok().message("注册成功");

    }

    @ApiOperation("会员登录")
    @PostMapping("/login")
    public R login(@RequestBody LoginVO loginVO, HttpServletRequest request) {
        String mobile = loginVO.getMobile();
        String password = loginVO.getPassword();
        Assert.notEmpty(mobile, ResponseEnum.MOBILE_NULL_ERROR);
        Assert.notEmpty(password, ResponseEnum.PASSWORD_NULL_ERROR);

        String ip = request.getRemoteAddr();
        UserInfoVO userInfoVO = userInfoService.login(loginVO, ip);
        return R.ok().data("userInfo", userInfoVO);
    }

    @ApiOperation("验证token")
    @GetMapping("/checkToken")
    public R checkToken(HttpServletRequest request){
        String token = request.getHeader("token");
        boolean flag = JwtUtils.checkToken(token);
        if (flag) {
            return R.ok();
        } else {
            return R.setResult(ResponseEnum.LOGIN_AUTH_ERROR);
        }
    }
}

