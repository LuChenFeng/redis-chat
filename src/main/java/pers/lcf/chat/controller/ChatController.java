package pers.lcf.chat.controller;

import pers.lcf.chat.constant.CommonConstant;
import pers.lcf.chat.entity.Message;
import pers.lcf.chat.entity.User;
import pers.lcf.chat.exception.GlobalException;
import pers.lcf.chat.service.ChatSessionService;
import pers.lcf.chat.utils.R;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * @Param: 
 * @Return: 
 * @Author: lcf
 * @Date: 2019/10/20 12:33
 */
@Slf4j
@RestController
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private ChatSessionService chatSessionService;

    @Autowired
    private StringRedisTemplate redisTemplate;


    @PostMapping("/addToRedis")
    private R addToRedis(@RequestBody User user) {
        User userR = chatSessionService.findById((user.getId()).toString());
        if (userR == null) {
            //调用用户添加方法
           addRedisUser(user);
        }
        return new R();
    }

    /**
     * 获取当前窗口用户信息
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R info(@PathVariable("id") String id) {
        return new R(chatSessionService.findById(id));
    }

    /**
     * 向指定窗口推送消息
     *
     * @param toId    接收方ID
     * @param message 消息
     * @return
     */
    @PostMapping("/push/{toId}")
    public R push(@PathVariable("toId") String toId, @RequestBody Message message) {
        try {
            User userR = chatSessionService.findById(toId);
            if (userR == null) {
                //调用用户添加方法
                addRedisUser(message.getTo());
            }
            WebsocketServerEndpoint endpoint = new WebsocketServerEndpoint();
            endpoint.sendTo(toId, message);
            return new R();
        } catch (GlobalException e) {
            e.printStackTrace();
            return new R(500, e.getMsg());
        }
    }

    /**
     * 获取在线用户列表
     *
     * @return
     */
    @GetMapping("/online/list/{id}")
    public R onlineList(@PathVariable String id) {
        return new R(chatSessionService.onlineList(id));
    }

    /**
     * 获取公共聊天消息内容
     *
     * @return
     */
    @GetMapping("/common")
    public R commonList() {
        return new R(chatSessionService.commonList());
    }

    /**
     * 获取指定用户的聊天消息内容
     *
     * @param fromId 该用户ID
     * @param toId   哪个窗口
     * @return
     */
    @GetMapping("/self/{fromId}/{toId}")
    public R selfList(@PathVariable("fromId") String fromId, @PathVariable("toId") String toId) {
        List<Message> list = chatSessionService.selfList(fromId, toId);
        return new R(list);
    }

    /**
     * 退出登录
     *
     * @param id 用户ID
     * @return
     */
    @DeleteMapping("/{id}")
    public R logout(@PathVariable("id") String id) {
        chatSessionService.delete(id);
        return new R();
    }

    /**
     * 把该用户添加到redis中
     *
     * @param user
     * @return
     */
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
