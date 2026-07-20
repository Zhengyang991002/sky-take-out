package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import java.time.LocalDateTime;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

  @Autowired
  private UserMapper userMapper;
  @Autowired
  private static final String WECHAT_LOGIN_URL = "https://api.weixin.qq.com/sns/jscode2session";
  @Autowired
  private WeChatProperties weChatProperties;

  /**
   * 微信登录
   * @param userLoginDTO
   * @return
   */
  @Override
  public User wxLogin(UserLoginDTO userLoginDTO) {
    // 调用微信接口服务，获取用户openid
    String openid = getOpenid(userLoginDTO.getCode());
    // 判断openid是否为空
    if (openid == null) {
      throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
    }
    // 判断当前用户是否为新用户
    User user = userMapper.getByOpenid(openid);
    // 若是新用户，自动完成注册
    if (user == null) {
      user = User.builder()
          .openid(openid)
          .createTime(LocalDateTime.now())
          .build();
      userMapper.insert(user);
    }
    // 返回用户信息
    return user;
  }

  /**
   * 获取微信用户openid
   * @param code
   */
  private String getOpenid(String code) {
    // 调用微信接口服务，获取用户openid
    HashMap<String, String> param = new HashMap<>();
    param.put("appid", weChatProperties.getAppid());
    param.put("secret", weChatProperties.getSecret());
    param.put("js_code", code);
    param.put("grant_type", "authorization_code");
    String userInfo = HttpClientUtil.doGet(WECHAT_LOGIN_URL, param);
    JSONObject jsonObject = JSONObject.parseObject(userInfo);
    String openid = jsonObject.getString("openid");
    return openid;
  }
}
