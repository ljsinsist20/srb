package com.atguigu.srb.core;

import com.atguigu.srb.core.mapper.DictMapper;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@SpringBootTest
@RunWith(SpringRunner.class)
public class RedisTemplateTests {

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private DictMapper dictMapper;

//    @Test
//    public void saveDict() {
//        Dict dict = dictMapper.selectById(1);
//        redisTemplate.opsForValue().set("dict", dict, 5, TimeUnit.MINUTES);
//        Dict dict1 = (Dict) redisTemplate.opsForValue().get("dict");
//        System.out.println(dict1);
//    }
}
