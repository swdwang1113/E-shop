package ptumall.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ptumall.config.JWTInterceptors;
import ptumall.model.UserAddress;
import ptumall.service.UserAddressService;
import ptumall.vo.Result;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Api(tags = "用户地址接口")
@RestController
@RequestMapping("/api/addresses")
public class UserAddressController {

    @Autowired
    private UserAddressService userAddressService;

    @ApiOperation("添加收货地址")
    @PostMapping("")
    public Result<UserAddress> addAddress(HttpServletRequest request, @RequestBody UserAddress address) {
        Integer userId = (Integer) request.getAttribute(JWTInterceptors.USER_ID_KEY);
        if (userId == null) {
            return Result.unauthorized();
        }
        address.setUserId(userId);
        UserAddress savedAddress = userAddressService.addAddress(address);
        return Result.success(savedAddress);
    }

    @ApiOperation("获取收货地址列表")
    @GetMapping("")
    public Result<List<UserAddress>> getAddressList(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute(JWTInterceptors.USER_ID_KEY);
        if (userId == null) {
            return Result.unauthorized();
        }
        List<UserAddress> addressList = userAddressService.getAddressList(userId);
        return Result.success(addressList);
    }

    @ApiOperation("获取收货地址详情")
    @ApiImplicitParam(name = "id", value = "地址ID", required = true, paramType = "path")
    @GetMapping("/{id}")
    public Result<UserAddress> getAddress(HttpServletRequest request, @PathVariable("id") Integer id) {
        Integer userId = (Integer) request.getAttribute(JWTInterceptors.USER_ID_KEY);
        if (userId == null) {
            return Result.unauthorized();
        }
        UserAddress address = userAddressService.getAddress(userId, id);
        return Result.success(address);
    }

    @ApiOperation("更新收货地址")
    @ApiImplicitParam(name = "id", value = "地址ID", required = true, paramType = "path")
    @PutMapping("/{id}")
    public Result<UserAddress> updateAddress(HttpServletRequest request, @PathVariable("id") Integer id, @RequestBody UserAddress address) {
        Integer userId = (Integer) request.getAttribute(JWTInterceptors.USER_ID_KEY);
        if (userId == null) {
            return Result.unauthorized();
        }
        address.setId(id);
        address.setUserId(userId);
        UserAddress updatedAddress = userAddressService.updateAddress(address);
        return Result.success(updatedAddress);
    }

    @ApiOperation("删除收货地址")
    @ApiImplicitParam(name = "id", value = "地址ID", required = true, paramType = "path")
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteAddress(HttpServletRequest request, @PathVariable("id") Integer id) {
        Integer userId = (Integer) request.getAttribute(JWTInterceptors.USER_ID_KEY);
        if (userId == null) {
            return Result.unauthorized();
        }
        boolean success = userAddressService.deleteAddress(userId, id);
        return Result.success(success);
    }

    @ApiOperation("设为默认地址")
    @ApiImplicitParam(name = "id", value = "地址ID", required = true, paramType = "path")
    @PostMapping("/{id}/default")
    public Result<Boolean> setDefaultAddress(HttpServletRequest request, @PathVariable("id") Integer id) {
        Integer userId = (Integer) request.getAttribute(JWTInterceptors.USER_ID_KEY);
        if (userId == null) {
            return Result.unauthorized();
        }
        boolean success = userAddressService.setDefaultAddress(userId, id);
        return Result.success(success);
    }
} 