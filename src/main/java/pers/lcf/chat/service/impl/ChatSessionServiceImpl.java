package pers.lcf.chat.service.impl;

import pers.lcf.chat.constant.CommonConstant;
import pers.lcf.chat.entity.Message;
import pers.lcf.chat.entity.User;
import pers.lcf.chat.service.ChatSessionService;
import pers.lcf.chat.utils.CoreUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @Param: 
 * @Return: 
 * @Author: lcf
 * @Date: 2019/10/20 12:35
 */
@Slf4j
@Service
public class ChatSessionServiceImpl implements ChatSessionService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public User findById(String id) {

        if (id != null) {
            String value = null;
            if (id.startsWith(CommonConstant.USER_PREFIX)) {
                value = redisTemplate.boundValueOps(id).get();
            } else {
                value = redisTemplate.boundValueOps(CommonConstant.USER_PREFIX + id).get();
            }
            JSONObject object = JSONObject.parseObject(value);
            if (object != null) {
                return object.toJavaObject(User.class);
            }
        }
        return null;
    }

    @Override
    public void pushMessage(String fromId, String toId, String message) {
        Message entity = new Message();
        entity.setMessage(message);
        entity.setFrom(this.findById(fromId));
        entity.setTime(CoreUtil.format(new Date()));
        if (toId != null) {
            //查询接收方信息
            entity.setTo(this.findById(toId));
            //单个用户推送
            push(entity, CommonConstant.CHAT_FROM_PREFIX + fromId + CommonConstant.CHAT_TO_PREFIX + toId);
        } else {
            //公共消息 -- 群组
            entity.setTo(null);
            push(entity, CommonConstant.CHAT_COMMON_PREFIX + fromId);
        }
    }

    /**
     * 推送消息
     *
     * @param entity Session value
     * @param key    Session key
     */
    private void push(Message entity, String key) {
        //这里按照 PREFIX_ID 格式，作为KEY储存消息记录
        //但一个用户可能推送很多消息，VALUE应该是数组
        List<Message> list = new ArrayList<>();
        String value = redisTemplate.boundValueOps(key).get();
        if (value == null) {
            //第一次推送消息
            list.add(entity);
        } else {
            //第n次推送消息
            list = Objects.requireNonNull(JSONObject.parseArray(value)).toJavaList(Message.class);
            list.add(entity);
        }
        redisTemplate.boundValueOps(key).set(JSONObject.toJSONString(list));
    }
/**
 * @Param: [id]
 * @Return: java.util.List<User>
 * @Author: lcf
 * @Date: 2019/10/19 16:22
 * 获取用户聊天列表
 */
    @Override
    public List<Message> onlineList(String id) {
        List<Message> list = new ArrayList<>();
        Set<String> fromkeys = redisTemplate.keys(CommonConstant.CHAT_FROM_PREFIX +id+ CommonConstant.REDIS_MATCH_PREFIX);
        Set<String> tokeys=(redisTemplate.keys(CommonConstant.REDIS_MATCH_PREFIX+CommonConstant.CHAT_TO_PREFIX+id ));
        Set<String> keys=new HashSet<>();
        if (fromkeys != null && fromkeys.size() > 0) {
            fromkeys.forEach(fromkey -> {
                keys.add(fromkey.split(CommonConstant.CHAT_TO_PREFIX)[1]);
            });
        }
        if (tokeys != null && tokeys.size() > 0) {
            tokeys.forEach(tokey -> {
                keys.add((tokey.split(CommonConstant.CHAT_TO_PREFIX)[0]).split("_")[2]);
            });
        }
        if (keys != null && keys.size() > 0) {
            keys.forEach(key -> {
                List<Message> messageList =selfList(id,key);
                if(messageList.size()>0){
                    list.add(messageList.get(messageList.size()-1));
                }
            });
        }
        System.out.println(keys);
        return list;
    }

    @Override
    public List<Message> commonList() {
        List<Message> list = new ArrayList<>();
        Set<String> keys = redisTemplate.keys(CommonConstant.CHAT_COMMON_PREFIX + CommonConstant.REDIS_MATCH_PREFIX);
        if (keys != null && keys.size() > 0) {
            keys.forEach(key -> {
                String value = redisTemplate.boundValueOps(key).get();
                List<Message> messageList = Objects.requireNonNull(JSONObject.parseArray(value)).toJavaList(Message.class);
                list.addAll(messageList);
            });
        }
        CoreUtil.sort(list);
        return list;
    }
/**
 * @Param: [fromId, toId]
 * @Return: java.util.List<Message>
 * @Author: lcf
 * @Date: 2019/10/19 16:16
 * 获取和指定用户的聊天记录
 */
    @Override
    public List<Message> selfList(String fromId, String toId) {
        List<Message> list = new ArrayList<>();
        //A -> B
        String fromTo = redisTemplate.boundValueOps(CommonConstant.CHAT_FROM_PREFIX + fromId + CommonConstant.CHAT_TO_PREFIX + toId).get();
        //B -> A
        String toFrom = redisTemplate.boundValueOps(CommonConstant.CHAT_FROM_PREFIX + toId + CommonConstant.CHAT_TO_PREFIX + fromId).get();

        JSONArray fromToObject = JSONObject.parseArray(fromTo);
        JSONArray toFromObject = JSONObject.parseArray(toFrom);
        if (fromToObject != null) {
            list.addAll(fromToObject.toJavaList(Message.class));
        }
        if (toFromObject != null) {
            list.addAll(toFromObject.toJavaList(Message.class));
        }

        if (list.size() > 0) {
            CoreUtil.sort(list);
            return list;
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public void delete(String id) {
        if (id != null) {
            log.info("从Redis中删除此Key: " + id);
            redisTemplate.delete(CommonConstant.USER_PREFIX + id);
        }
    }
}
