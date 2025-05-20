package ptumall.controller;

import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ptumall.model.Userorder;
import ptumall.service.OrderService;
import ptumall.utils.Result;
import ptumall.utils.ResultCodeEnum;

@RestController
@Api(tags = "订单接口")
@RequestMapping("/order")
public class OrderController {
    @Autowired
    OrderService orderService;

    @ApiOperation("分页获取订单列表")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(name = "uid",value = "用户id",dataTypeClass = Integer.class,required = true,defaultValue = "1"),
            @ApiImplicitParam(name = "pageNum",value = "当前页码",dataTypeClass = Integer.class,required = true,defaultValue = "1"),
            @ApiImplicitParam(name = "pageSize",value = "当前页数量",dataTypeClass = Integer.class,required = true,defaultValue = "4"),
    })
    @GetMapping("/all")
    public Result getAllOrder(@RequestParam(name = "uid") Integer uid,
                              @RequestParam(name = "pageNum") Integer pageNum,
                              @RequestParam(name = "pageSize") Integer pageSize)
    {
        PageInfo<Userorder> pageInfo = orderService.getAllOrder(uid,pageNum,pageSize);
        return Result.success(pageInfo);
    }

    @ApiOperation("删除单件订单")
    @ApiImplicitParam(name = "id",value = "订单id",dataTypeClass = Integer.class,required = true,defaultValue = "1")
    @DeleteMapping("/deleteone/{id}")
    public Result deldeteOne(@PathVariable Integer id)
    {
        int res = orderService.deleteOneOrder(id);
        if(res >= 1)
        {
            return Result.success();
        }
        else
        {
            return Result.failure(ResultCodeEnum.FAIL,"删除失败！");
        }
    }

    @ApiOperation("清空用户所有订单")
    @ApiImplicitParam(name = "uid",value = "用户id",dataTypeClass = Integer.class,required = true,defaultValue = "1")
    @DeleteMapping("/deleteall/{uid}")
    public Result deldeteAll(@PathVariable Integer uid)
    {
        int res = orderService.deleteAllOrder(uid);
        if(res >= 1)
        {
            return Result.success();
        }
        else
        {
            return Result.failure(ResultCodeEnum.FAIL,"清空失败！");
        }
    }
}

