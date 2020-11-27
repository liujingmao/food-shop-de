package com.imooc.controller.center;

import com.imooc.controller.BaseController;
import com.imooc.pojo.Users;
import com.imooc.pojo.bo.center.CenterUserBO;
import com.imooc.resource.FileUpload;
import com.imooc.service.center.CenterUserService;
import com.imooc.utils.CookieUtils;
import com.imooc.utils.DateUtil;
import com.imooc.utils.IMOOCJSONResult;
import com.imooc.utils.JsonUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by liujingmao on 2019-12-28.
 */

@Api(value = "用户信息接口",tags = {"用户信息相关的接口"})
@RestController
@RequestMapping("userInfo")
public class CenterUserController extends BaseController {

    @Autowired
    private CenterUserService centerUserService;

    @Autowired
    private FileUpload fileUpload;

    @ApiOperation(value = "修改用户信息",notes="修改用户信息",httpMethod = "POST")
    @PostMapping("update")
    public IMOOCJSONResult update(@ApiParam(name="userId",value = "用户id",required = true)
                                      @RequestParam String userId,
                                  @RequestBody @Valid CenterUserBO centerUserBO,
                                  BindingResult result,
                                  HttpServletRequest request,
                                  HttpServletResponse response){
        // 判断BindingResult是否保存错误的验证，如果有就return
        if(result.hasErrors()){
            Map<String,String> errorMap = getErrors(result);
            return IMOOCJSONResult.errorMap(errorMap);
        }
        Users userReult =centerUserService.updateUserInfo(userId,centerUserBO);

        userReult=setNullProperty(userReult);

        CookieUtils.setCookie(request,response,"user", JsonUtils.objectToJson(userReult),true);

        //TODO 后续要改，增加令牌token,会整合进Redis以及分布式会话

        return IMOOCJSONResult.ok();
    }

    @ApiOperation(value = "修改用户头相",notes="修改用户头相",httpMethod = "POST")
    @PostMapping("uploadFace")
    public IMOOCJSONResult uploadFace(@ApiParam(name="userId",value = "用户id",required = true)
                                  @RequestParam String userId,
                                  @ApiParam(name = "file",value = "用户头相",required = true)
                                  MultipartFile file,
                                  HttpServletRequest request,HttpServletResponse response) {
        //定义保存头像的地址
        //String fileSpace = IMAGE_USER_FACE_LOCATION;
        String fileSpace = fileUpload.getImageUserFaceLocation();
        // 在路径上为每一个用户增加一个userId
        String uploadPathPrefix = File.separator + userId;
        // 开始文件上传
        if (file != null) {
            FileOutputStream fileOutputStream = null;
            try {
                //获得文件上传的文件名称
                String fileName = file.getOriginalFilename();
                if (StringUtils.isBlank(fileName)) {
                    // 文件重命名 imooc-face.png ->["imooc-face","png"]
                    String fileNameArr[] = fileName.split("\\.");
                    // 获取文件的后继名
                    String suffix = fileNameArr[fileNameArr.length - 1];
                    // 判断文件名后缀
                    if(!suffix.equalsIgnoreCase("png")&&
                    !suffix.equalsIgnoreCase("jpg")&&
                    !suffix.equalsIgnoreCase("jpeg")){
                        return IMOOCJSONResult.errorMsg("图片格式不正确");
                    }

                    //face-{userId}.png
                    //文件名称重组 覆盖式上传，增量式：额外拼接当前时间
                    String newFileName = "face-" + userId + "." + suffix;
                    //上传的头像最终保存的位置
                    String finalFacePath = fileSpace + uploadPathPrefix + File.separator + newFileName;
                    //用于提供给Web服务访问的地址
                    uploadPathPrefix += ("/" + newFileName);

                    File outFile = new File(finalFacePath);
                    if (outFile.getParentFile() != null) {
                        outFile.getParentFile().mkdirs();
                    }

                    //文件输出
                    fileOutputStream = new FileOutputStream(outFile);
                    InputStream inputStream = file.getInputStream();
                    IOUtils.copy(inputStream, fileOutputStream);
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                         try {
                                if (fileOutputStream != null) {
                                fileOutputStream.flush();
                                fileOutputStream.close();
                            }
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }

        } else {
            IMOOCJSONResult.errorMsg("文件不能为空");
        }

        //
        String imageServerUrl = fileUpload.getImageServerUrl();

        String finaUserFaceUrl = imageServerUrl+uploadPathPrefix+"?t" + DateUtil.getCurrentDateString(DateUtil.DATE_PATTERN);

        Users userResult = centerUserService.updateUserFace(userId,finaUserFaceUrl);

        //更新用户头像到数据库
        //Users userReult =centerUserService.updateUserFace(userId,centerUserBO);

        userResult=setNullProperty(userResult);

        CookieUtils.setCookie(request,response,"user", JsonUtils.objectToJson(userResult),true);

        return IMOOCJSONResult.ok();
    }

    private Map<String,String> getErrors(BindingResult result){
        Map<String,String> map = new HashMap<>();
        List<FieldError> errorList = result.getFieldErrors();
        for(FieldError error:errorList){
            // 发生验证错误的对应的某一个属性
            String errorField = error.getField();
            // 验证错误的信息
            String errorMsg = error.getDefaultMessage();
            map.put(errorField,errorMsg);
        }
        return map;
    }
    private Users setNullProperty(Users  userResult){
        userResult.setPassword(null);
        userResult.setMobile(null);
        userResult.setEmail(null);
        userResult.setUpdatedTime(null);
        userResult.setCreatedTime(null);
        userResult.setBirthday(null);
        return userResult;
    }
}
