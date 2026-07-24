package com.sky.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class BaiduMapUtil {

  // 百度地图 API URL 常量
  public static final String LOCATION_URL = "https://api.map.baidu.com/geocoding/v3";
  public static final String DISTANCE_URL = "https://api.map.baidu.com/routematrix/v2/riding";

  /**
   * 解析地址获取经纬度（带 precise 精准度校验）
   */
  public static Map<String, Double> getLocation(String address, String ak) {
    if (address == null || address.trim().isEmpty() || ak == null) {
      return null;
    }

    // 1. 组装请求参数
    Map<String, String> param = new HashMap<>();
    param.put("address", address);
    param.put("output", "json");
    param.put("ak", ak);

    // 2. 发送请求
    String rawResult = HttpClientUtil.doGet(LOCATION_URL, param);
    if (rawResult == null || rawResult.isEmpty()) {
      return null;
    }

    // 去除 JSONP 包裹（如果有）
    if (rawResult.contains("(") && rawResult.endsWith(")")) {
      rawResult = rawResult.substring(rawResult.indexOf("(") + 1, rawResult.lastIndexOf(")"));
    }

    // 3. 解析 JSON
    JSONObject jsonObject = JSON.parseObject(rawResult);

    if (jsonObject.getInteger("status") == 0) {
      JSONObject result = jsonObject.getJSONObject("result");

      // 💡 提取 confidence（匹配可信度：0~100）
      Integer confidence = result.getInteger("confidence");

      // 如果可信度小于 30（说明百度完全是在瞎猜，比如降级到了区县中心），才判定解析失败
      if (confidence != null && confidence < 30) {
        throw new RuntimeException("地址解析失败，请输入更详细的地址: " + address);
      }

      JSONObject locationJson = result.getJSONObject("location");
      Map<String, Double> location = new HashMap<>();
      location.put("lng", locationJson.getDouble("lng"));
      location.put("lat", locationJson.getDouble("lat"));
      return location;
    }

    return null;
  }

  /**
   * 计算两点间的路线距离（单位：米）
   */
  public static Integer getDistance(Map<String, Double> origin, Map<String, Double> destination, String ak) {
    if (origin == null || destination == null || ak == null) {
      return null;
    }

    // 1. 组装请求参数
    Map<String, String> param = new HashMap<>();
    param.put("origins", origin.get("lat") + "," + origin.get("lng"));
    param.put("destinations", destination.get("lat") + "," + destination.get("lng"));
    param.put("output", "json");
    param.put("ak", ak);

    // 2. 发送请求
    String rawResult = HttpClientUtil.doGet(DISTANCE_URL, param);
    if (rawResult == null || rawResult.isEmpty()) {
      throw new RuntimeException("距离估算失败");
    }

    // 3. 解析 JSON 提取距离
    JSONObject jsonObject = JSON.parseObject(rawResult);

    if (jsonObject.getInteger("status") == 0) {
      JSONArray resultArr = jsonObject.getJSONArray("result");
      if (resultArr != null && !resultArr.isEmpty()) {
        // 提取 result[0].distance.value (单位：米)
        return resultArr.getJSONObject(0)
            .getJSONObject("distance")
            .getInteger("value");
      }
    }

    return null;
  }
}