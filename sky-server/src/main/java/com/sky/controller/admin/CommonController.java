package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.properties.AliOssProperties;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.io.IOException;
import java.security.PrivateKey;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 通用接口
 */
@Api(tags = "通用接口")
@RestController
@RequestMapping("/admin/common")
@Slf4j
public class CommonController {
  @Autowired
  private AliOssUtil aliOssUtil;

  /**
   * 文件上传
   * @param file
   * @return
   */
  @PostMapping("/upload")
  @ApiOperation(value = "文件上传")
  public Result<String> upload(MultipartFile file) {
    log.info("上传文件: {}", file);
    try {
      // 获取原始文件名
      String originalFilename = file.getOriginalFilename();
      // 获取文件名后缀
      String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
      // 生成新的文件名
      String fileName = UUID.randomUUID().toString() + suffix;
      // 文件请求路径
      String url = aliOssUtil.upload(file.getBytes(), fileName);
      return Result.success(url);
    } catch (Exception e) {
      log.error("文件上传失败: {}", e.getMessage());
    }
    return Result.error(MessageConstant.UPLOAD_FAILED);
  }
}
