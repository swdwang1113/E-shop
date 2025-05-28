package ptumall.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ptumall.model.User;
import ptumall.service.UserService;
import ptumall.utils.AuthUtils;
import ptumall.vo.PageResult;
import ptumall.vo.Result;
import ptumall.vo.ResultCode;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Api(tags = "管理员用户接口")
@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private AuthUtils authUtils;
    
    @ApiOperation("获取所有用户列表")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "pageNum", value = "页码", required = true, paramType = "query"),
        @ApiImplicitParam(name = "pageSize", value = "每页数量", required = true, paramType = "query"),
        @ApiImplicitParam(name = "username", value = "用户名关键词(可选)", required = false, paramType = "query"),
        @ApiImplicitParam(name = "phone", value = "手机号关键词(可选)", required = false, paramType = "query")
    })
    @GetMapping("")
    public Result<PageResult<User>> getAllUsers(
        HttpServletRequest request,
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "10") Integer pageSize,
        @RequestParam(required = false) String username,
        @RequestParam(required = false) String phone
    ) {
        // 权限校验：只有管理员可以查看所有用户
        if (!authUtils.isAdmin(request)) {
            return Result.unauthorized();
        }
        
        PageResult<User> result;
        
        // 根据不同的参数组合调用不同的查询方法
        if (username != null && !username.trim().isEmpty()) {
            // 优先按用户名查询
            result = userService.searchUsersByUsername(username, pageNum, pageSize);
        } else if (phone != null && !phone.trim().isEmpty()) {
            // 按手机号查询
            result = userService.searchUsersByPhone(phone, pageNum, pageSize);
        } else {
            // 查询所有用户
            result = userService.getUserList(pageNum, pageSize);
        }
        
        return Result.success(result);
    }
    
    @ApiOperation("获取用户总数")
    @GetMapping("/count")
    public Result<Integer> getUserCount(HttpServletRequest request) {
        // 权限校验：只有管理员可以查看用户总数
        if (!authUtils.isAdmin(request)) {
            return Result.unauthorized();
        }
        
        int count = userService.getUserCount();
        return Result.success(count);
    }
    
    @ApiOperation("删除用户")
    @ApiImplicitParam(name = "id", value = "用户ID", required = true, paramType = "path")
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteUser(HttpServletRequest request, @PathVariable("id") Integer id) {
        // 权限校验：只有管理员可以删除用户
        if (!authUtils.isAdmin(request)) {
            return Result.unauthorized();
        }
        
        boolean success = userService.deleteUser(id);
        return Result.success(success);
    }
} 