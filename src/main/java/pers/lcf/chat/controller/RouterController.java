package pers.lcf.chat.controller;

import pers.lcf.chat.constant.CommonConstant;
import pers.lcf.chat.entity.User;
import pers.lcf.chat.exception.GlobalException;
import pers.lcf.chat.service.ChatSessionService;
import pers.lcf.chat.utils.R;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

/**
 * @Param:
 * @Return:
 * @Author: lcf
 * @Date: 2019/10/20 12:33
 * 路由接口控制器
 */
@Slf4j
@Controller
public class RouterController {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ChatSessionService chatSessionService;


    /**
     * 登陆页面
     *
     * @return
     */
    @GetMapping("/")
    public String index() {
        return "login";
    }

    /**
     * 登录接口
     *
     * @param user
     * @return
     */
    @ResponseBody
    @PostMapping("/login")
    public R login(@RequestBody User user) {
        return addRedisUser(user);
    }

    /**
     * 首页入口
     *
     * @return
     */
    @GetMapping("/{id}/chat")
    public String index(@PathVariable("id") String id) {
        User user = chatSessionService.findById(id);
        if (user == null) {
         User user1=new User();
            user1.setId(id);
            user1.setName("用户" + id);
            user1.setAvatar("/avatar/20180414165840.jpg");
            addRedisUser(user1);
//            return "redirect:/";
        }
        return "index";
    }

    //    把用户添加进redis聊天列表
    public R addRedisUser(User user) {

        Set<String> keys = redisTemplate.keys(CommonConstant.USER_PREFIX + CommonConstant.REDIS_MATCH_PREFIX);
        if (keys != null && keys.size() > 0) {
            keys.forEach(key -> {
                User entity = chatSessionService.findById(key);
                if (entity != null) {
                    if ((entity.getName()).equals(user.getName())) {
                        throw new GlobalException("用户名已存在");
                    }
                }
            });
        }
        redisTemplate.boundValueOps(CommonConstant.USER_PREFIX + user.getId()).set(JSONObject.toJSONString(user));
        return new R();
    }
}
