package pers.lcf.chat.service;

import pers.lcf.chat.entity.Message;
import pers.lcf.chat.entity.User;

import java.util.List;

/**
 * @Param: 
 * @Return: 
 * @Author: lcf
 * @Date: 2019/10/20 12:35
 */
public interface ChatSessionService {

    /**
     * 根据ID从Redis中查询数据
     *
     * @param id
     * @return User对象
     */
    User findById(String id);

    /**
     * 推送消息，储存到Redis数据库中
     *
     * @param fromId  推送方ID
     * @param toId    接收方ID
     * @param message 消息
     */
    void pushMessage(String fromId, String toId, String message);

    /**
     * 获取在线用户列表
     *
     * @return
     */
    List<Message> onlineList(String id);

    /**
     * 获取公共消息内容 -- 群组
     *
     * @return
     */
    List<Message> commonList();

    /**
     * 获取该用户与指定窗口的推送消息
     *
     * @param fromId 推送方ID
     * @param toId   接收方ID
     * @return
     */
    List<Message> selfList(String fromId, String toId);

    /**
     * 删除指定ID在Redis中储存的数据
     *
     * @param id
     */
    void delete(String id);
}
