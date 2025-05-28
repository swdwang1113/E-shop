package ptumall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ptumall.dao.CartDao;
import ptumall.dao.GoodsDao;
import ptumall.dao.OrderDao;
import ptumall.dao.UserAddressDao;
import ptumall.exception.BusinessException;
import ptumall.model.*;
import ptumall.service.OrderService;
import ptumall.vo.CreateOrderParam;
import ptumall.vo.PageResult;
import ptumall.vo.ResultCode;
import ptumall.vo.OrderStatisticsVO;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderDao orderDao;
    
    @Autowired
    private CartDao cartDao;
    
    @Autowired
    private GoodsDao goodsDao;
    
    @Autowired
    private UserAddressDao userAddressDao;

    /**
     * 创建订单
     * @param userId 用户ID
     * @param param 创建订单参数
     * @return 订单信息
     */
    @Override
    @Transactional
    public Orders createOrder(Integer userId, CreateOrderParam param) {
        // 验证地址
        UserAddress address = userAddressDao.selectById(param.getAddressId());
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "收货地址不存在");
        }
        
        // 创建订单
        Orders order = new Orders();
        order.setUserId(userId);
        order.setOrderNo(generateOrderNo());
        order.setStatus((byte) 0); // 待付款
        order.setAddressId(param.getAddressId());
        
        List<OrderItems> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        // 处理购物车模式
        if (param.getCartItemIds() != null && !param.getCartItemIds().isEmpty()) {
            List<Cart> cartList = cartDao.selectByIds(param.getCartItemIds());
            
            // 验证购物车属于当前用户
            for (Cart cart : cartList) {
                if (!cart.getUserId().equals(userId)) {
                    throw new BusinessException(ResultCode.PARAM_ERROR, "购物车数据异常");
                }
                
                // 获取商品信息
                Goods goods = goodsDao.selectById(cart.getGoodsId());
                if (goods == null) {
                    throw new BusinessException(ResultCode.PARAM_ERROR, "商品不存在");
                }
                
                // 验证商品是否上架
                if (goods.getStatus() == null || goods.getStatus() != 1) {
                    throw new BusinessException(ResultCode.PARAM_ERROR, "商品已下架");
                }
                
                // 验证库存是否足够
                if (goods.getStock() < cart.getQuantity()) {
                    throw new BusinessException(ResultCode.PARAM_ERROR, "商品[" + goods.getName() + "]库存不足");
                }
                
                // 创建订单项
                OrderItems orderItem = new OrderItems();
                orderItem.setGoodsId(goods.getId());
                orderItem.setGoodsName(goods.getName());
                orderItem.setGoodsImage(goods.getImage());
                orderItem.setGoodsPrice(goods.getPrice());
                orderItem.setQuantity(cart.getQuantity());
                orderItem.setTotalPrice(goods.getPrice().multiply(new BigDecimal(cart.getQuantity())));
                orderItems.add(orderItem);
                
                // 累加总金额
                totalAmount = totalAmount.add(orderItem.getTotalPrice());
            }
            
            // 设置订单总金额
            order.setTotalAmount(totalAmount);
            
            // 保存订单
            orderDao.insert(order);
            
            // 保存订单商品
            for (OrderItems item : orderItems) {
                item.setOrderId(order.getId());
            }
            orderDao.batchInsertOrderItems(orderItems);
            
            // 扣减库存
            for (Cart cart : cartList) {
                Goods goods = goodsDao.selectById(cart.getGoodsId());
                goodsDao.updateStock(goods.getId(), goods.getStock() - cart.getQuantity());
                
                // 更新销量
                goodsDao.updateSalesVolume(goods.getId(), cart.getQuantity());
            }
            
            // 清空购物车
            cartDao.deleteByIds(param.getCartItemIds());
        } 
        // 处理直接购买模式
        else if (param.getGoodsId() != null && param.getQuantity() != null && param.getQuantity() > 0) {
            // 获取商品信息
            Goods goods = goodsDao.selectById(param.getGoodsId());
            if (goods == null) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "商品不存在");
            }
            
            // 验证商品是否上架
            if (goods.getStatus() == null || goods.getStatus() != 1) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "商品已下架");
            }
            
            // 验证库存是否足够
            if (goods.getStock() < param.getQuantity()) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "商品[" + goods.getName() + "]库存不足");
            }
            
            // 创建订单项
            OrderItems orderItem = new OrderItems();
            orderItem.setGoodsId(goods.getId());
            orderItem.setGoodsName(goods.getName());
            orderItem.setGoodsImage(goods.getImage());
            orderItem.setGoodsPrice(goods.getPrice());
            orderItem.setQuantity(param.getQuantity());
            orderItem.setTotalPrice(goods.getPrice().multiply(new BigDecimal(param.getQuantity())));
            orderItems.add(orderItem);
            
            // 设置订单总金额
            totalAmount = orderItem.getTotalPrice();
            order.setTotalAmount(totalAmount);
            
            // 保存订单
            orderDao.insert(order);
            
            // 保存订单商品
            orderItem.setOrderId(order.getId());
            orderDao.insertOrderItem(orderItem);
            
            // 扣减库存
            goodsDao.updateStock(goods.getId(), goods.getStock() - param.getQuantity());
            
            // 更新销量
            goodsDao.updateSalesVolume(goods.getId(), param.getQuantity());
        } else {
            throw new BusinessException(ResultCode.PARAM_ERROR, "参数错误");
        }
        
        // 设置订单商品
        order.setOrderItems(orderItems);
        
        // 设置收货地址
        order.setAddress(address);
        
        return order;
    }

    /**
     * 生成订单编号
     * @return 订单编号
     */
    private String generateOrderNo() {
        // 生成订单号，格式：年月日+随机数
        String date = String.format("%1$tY%1$tm%1$td", new Date());
        String uuid = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 6);
        return date + uuid;
    }

    /**
     * 根据订单ID获取订单详情
     * @param userId 用户ID
     * @param orderId 订单ID
     * @return 订单信息
     */
    @Override
    public Orders getOrderDetail(Integer userId, Integer orderId) {
        // 获取订单
        Orders order = orderDao.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "订单不存在");
        }
        
        // 获取订单商品
        List<OrderItems> orderItems = orderDao.selectOrderItemsByOrderId(orderId);
        order.setOrderItems(orderItems);
        
        // 获取收货地址
        UserAddress address = userAddressDao.selectById(order.getAddressId());
        order.setAddress(address);
        
        return order;
    }

    /**
     * 根据订单编号获取订单详情
     * @param userId 用户ID
     * @param orderNo 订单编号
     * @return 订单信息
     */
    @Override
    public Orders getOrderByOrderNo(Integer userId, String orderNo) {
        // 获取订单
        Orders order = orderDao.selectByOrderNo(orderNo);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "订单不存在");
        }
        
        // 获取订单商品
        List<OrderItems> orderItems = orderDao.selectOrderItemsByOrderId(order.getId());
        order.setOrderItems(orderItems);
        
        // 获取收货地址
        UserAddress address = userAddressDao.selectById(order.getAddressId());
        order.setAddress(address);
        
        return order;
    }

    /**
     * 获取用户订单列表
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 订单列表分页结果
     */
    @Override
    public PageResult<Orders> getOrderList(Integer userId, Integer pageNum, Integer pageSize) {
        // 分页查询
        PageHelper.startPage(pageNum, pageSize);
        List<Orders> orderList = orderDao.selectByUserId(userId);
        PageInfo<Orders> pageInfo = new PageInfo<>(orderList);
        
        // 获取订单商品和收货地址
        for (Orders order : orderList) {
            List<OrderItems> orderItems = orderDao.selectOrderItemsByOrderId(order.getId());
            order.setOrderItems(orderItems);
            
            UserAddress address = userAddressDao.selectById(order.getAddressId());
            order.setAddress(address);
        }
        
        // 构建分页结果
        return new PageResult<>(
            pageInfo.getTotal(), 
            pageInfo.getPages(), 
            pageInfo.getPageNum(), 
            pageInfo.getPageSize(), 
            orderList
        );
    }

    /**
     * 根据状态获取用户订单列表
     * @param userId 用户ID
     * @param status 订单状态
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 订单列表分页结果
     */
    @Override
    public PageResult<Orders> getOrderListByStatus(Integer userId, Byte status, Integer pageNum, Integer pageSize) {
        // 分页查询
        PageHelper.startPage(pageNum, pageSize);
        List<Orders> orderList = orderDao.selectByUserIdAndStatus(userId, status);
        PageInfo<Orders> pageInfo = new PageInfo<>(orderList);
        
        // 获取订单商品和收货地址
        for (Orders order : orderList) {
            List<OrderItems> orderItems = orderDao.selectOrderItemsByOrderId(order.getId());
            order.setOrderItems(orderItems);
            
            UserAddress address = userAddressDao.selectById(order.getAddressId());
            order.setAddress(address);
        }
        
        // 构建分页结果
        return new PageResult<>(
            pageInfo.getTotal(), 
            pageInfo.getPages(), 
            pageInfo.getPageNum(), 
            pageInfo.getPageSize(), 
            orderList
        );
    }

    /**
     * 取消订单
     * @param userId 用户ID
     * @param orderId 订单ID
     * @return 是否成功
     */
    @Override
    @Transactional
    public boolean cancelOrder(Integer userId, Integer orderId) {
        // 获取订单
        Orders order = orderDao.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "订单不存在");
        }
        
        // 只有待付款的订单可以取消
        if (order.getStatus() != 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "当前订单状态不可取消");
        }
        
        // 获取订单商品
        List<OrderItems> orderItems = orderDao.selectOrderItemsByOrderId(orderId);
        
        // 恢复商品库存
        for (OrderItems item : orderItems) {
            // 获取商品当前库存
            Goods goods = goodsDao.selectById(item.getGoodsId());
            if (goods != null) {
                // 恢复库存
                goodsDao.updateStock(goods.getId(), goods.getStock() + item.getQuantity());
                
                // 恢复销量
                if (goods.getSalesVolume() != null && goods.getSalesVolume() >= item.getQuantity()) {
                    goodsDao.updateSalesVolume(goods.getId(), -item.getQuantity());
                }
            }
        }
        
        // 更新订单状态为已取消(4)
        return orderDao.updateStatus(orderId, (byte) 4) > 0;
    }

    /**
     * 支付订单
     * @param userId 用户ID
     * @param orderId 订单ID
     * @param paymentType 支付方式
     * @return 是否成功
     */
    @Override
    @Transactional
    public boolean payOrder(Integer userId, Integer orderId, Byte paymentType) {
        // 获取订单
        Orders order = orderDao.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "订单不存在");
        }
        
        // 只有未支付的订单可以支付
        if (order.getStatus() != 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "当前订单状态不可支付");
        }
        
        // 更新订单状态为已支付(1)
        return orderDao.pay(orderId, paymentType) > 0;
    }

    /**
     * 确认收货
     * @param userId 用户ID
     * @param orderId 订单ID
     * @return 是否成功
     */
    @Override
    @Transactional
    public boolean confirmReceipt(Integer userId, Integer orderId) {
        // 获取订单
        Orders order = orderDao.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "订单不存在");
        }
        
        // 只有已发货的订单可以确认收货
        if (order.getStatus() != 2) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "当前订单状态不可确认收货");
        }
        
        // 更新订单状态为已完成(3)
        return orderDao.updateStatus(orderId, (byte) 3) > 0;
    }

    /**
     * 获取所有订单列表（管理员接口）
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 订单列表分页结果
     */
    @Override
    public PageResult<Orders> getAllOrderList(Integer pageNum, Integer pageSize) {
        // 分页查询所有订单
        PageHelper.startPage(pageNum, pageSize);
        List<Orders> orderList = orderDao.selectAll();
        PageInfo<Orders> pageInfo = new PageInfo<>(orderList);
        
        // 获取订单商品和收货地址
        for (Orders order : orderList) {
            List<OrderItems> orderItems = orderDao.selectOrderItemsByOrderId(order.getId());
            order.setOrderItems(orderItems);
            
            UserAddress address = userAddressDao.selectById(order.getAddressId());
            order.setAddress(address);
        }
        
        // 构建分页结果
        return new PageResult<>(
            pageInfo.getTotal(), 
            pageInfo.getPages(), 
            pageInfo.getPageNum(), 
            pageInfo.getPageSize(), 
            orderList
        );
    }
    
    /**
     * 根据状态获取所有订单列表（管理员接口）
     * @param status 订单状态
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 订单列表分页结果
     */
    @Override
    public PageResult<Orders> getAllOrderListByStatus(Byte status, Integer pageNum, Integer pageSize) {
        // 分页查询所有订单
        PageHelper.startPage(pageNum, pageSize);
        List<Orders> orderList = orderDao.selectAllByStatus(status);
        PageInfo<Orders> pageInfo = new PageInfo<>(orderList);
        
        // 获取订单商品和收货地址
        for (Orders order : orderList) {
            List<OrderItems> orderItems = orderDao.selectOrderItemsByOrderId(order.getId());
            order.setOrderItems(orderItems);
            
            UserAddress address = userAddressDao.selectById(order.getAddressId());
            order.setAddress(address);
        }
        
        // 构建分页结果
        return new PageResult<>(
            pageInfo.getTotal(), 
            pageInfo.getPages(), 
            pageInfo.getPageNum(), 
            pageInfo.getPageSize(), 
            orderList
        );
    }
    
    /**
     * 根据订单号关键词搜索订单（管理员接口）
     * @param orderNo 订单号关键词
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 订单列表分页结果
     */
    @Override
    public PageResult<Orders> searchOrdersByOrderNo(String orderNo, Integer pageNum, Integer pageSize) {
        // 分页查询订单
        PageHelper.startPage(pageNum, pageSize);
        List<Orders> orderList = orderDao.selectByOrderNoKeyword(orderNo);
        PageInfo<Orders> pageInfo = new PageInfo<>(orderList);
        
        // 获取订单商品和收货地址
        for (Orders order : orderList) {
            List<OrderItems> orderItems = orderDao.selectOrderItemsByOrderId(order.getId());
            order.setOrderItems(orderItems);
            
            UserAddress address = userAddressDao.selectById(order.getAddressId());
            order.setAddress(address);
        }
        
        // 构建分页结果
        return new PageResult<>(
            pageInfo.getTotal(), 
            pageInfo.getPages(), 
            pageInfo.getPageNum(), 
            pageInfo.getPageSize(), 
            orderList
        );
    }
    
    /**
     * 根据用户ID搜索订单（管理员接口）
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 订单列表分页结果
     */
    @Override
    public PageResult<Orders> searchOrdersByUserId(Integer userId, Integer pageNum, Integer pageSize) {
        // 分页查询订单
        PageHelper.startPage(pageNum, pageSize);
        List<Orders> orderList = orderDao.selectByUserIdAdmin(userId);
        PageInfo<Orders> pageInfo = new PageInfo<>(orderList);
        
        // 获取订单商品和收货地址
        for (Orders order : orderList) {
            List<OrderItems> orderItems = orderDao.selectOrderItemsByOrderId(order.getId());
            order.setOrderItems(orderItems);
            
            UserAddress address = userAddressDao.selectById(order.getAddressId());
            order.setAddress(address);
        }
        
        // 构建分页结果
        return new PageResult<>(
            pageInfo.getTotal(), 
            pageInfo.getPages(), 
            pageInfo.getPageNum(), 
            pageInfo.getPageSize(), 
            orderList
        );
    }
    
    /**
     * 根据订单号关键词和状态搜索订单（管理员接口）
     * @param orderNo 订单号关键词
     * @param status 订单状态
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 订单列表分页结果
     */
    @Override
    public PageResult<Orders> searchOrdersByOrderNoAndStatus(String orderNo, Byte status, Integer pageNum, Integer pageSize) {
        // 分页查询订单
        PageHelper.startPage(pageNum, pageSize);
        List<Orders> orderList = orderDao.selectByOrderNoAndStatus(orderNo, status);
        PageInfo<Orders> pageInfo = new PageInfo<>(orderList);
        
        // 获取订单商品和收货地址
        for (Orders order : orderList) {
            List<OrderItems> orderItems = orderDao.selectOrderItemsByOrderId(order.getId());
            order.setOrderItems(orderItems);
            
            UserAddress address = userAddressDao.selectById(order.getAddressId());
            order.setAddress(address);
        }
        
        // 构建分页结果
        return new PageResult<>(
            pageInfo.getTotal(), 
            pageInfo.getPages(), 
            pageInfo.getPageNum(), 
            pageInfo.getPageSize(), 
            orderList
        );
    }
    
    /**
     * 根据用户ID和状态搜索订单（管理员接口）
     * @param userId 用户ID
     * @param status 订单状态
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 订单列表分页结果
     */
    @Override
    public PageResult<Orders> searchOrdersByUserIdAndStatus(Integer userId, Byte status, Integer pageNum, Integer pageSize) {
        // 分页查询订单
        PageHelper.startPage(pageNum, pageSize);
        List<Orders> orderList = orderDao.selectByUserIdAndStatusAdmin(userId, status);
        PageInfo<Orders> pageInfo = new PageInfo<>(orderList);
        
        // 获取订单商品和收货地址
        for (Orders order : orderList) {
            List<OrderItems> orderItems = orderDao.selectOrderItemsByOrderId(order.getId());
            order.setOrderItems(orderItems);
            
            UserAddress address = userAddressDao.selectById(order.getAddressId());
            order.setAddress(address);
        }
        
        // 构建分页结果
        return new PageResult<>(
            pageInfo.getTotal(), 
            pageInfo.getPages(), 
            pageInfo.getPageNum(), 
            pageInfo.getPageSize(), 
            orderList
        );
    }
    
    /**
     * 订单发货（管理员接口）
     * @param orderId 订单ID
     * @return 是否成功
     */
    @Override
    @Transactional
    public boolean shipOrder(Integer orderId) {
        // 获取订单
        Orders order = orderDao.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "订单不存在");
        }
        
        // 只有已付款的订单可以发货
        if (order.getStatus() != 1) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "当前订单状态不可发货");
        }
        
        // 更新订单状态为已发货(2)
        return orderDao.updateStatus(orderId, (byte) 2) > 0;
    }
    
    /**
     * 删除订单（管理员接口）
     * @param orderId 订单ID
     * @return 是否成功
     */
    @Override
    @Transactional
    public boolean deleteOrder(Integer orderId) {
        // 获取订单
        Orders order = orderDao.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "订单不存在");
        }
        
        // 先删除订单商品
        orderDao.deleteOrderItems(orderId);
        
        // 再删除订单
        return orderDao.deleteById(orderId) > 0;
    }
    
    /**
     * 删除用户自己的订单
     * @param userId 用户ID
     * @param orderId 订单ID
     * @return 是否成功
     */
    @Override
    @Transactional
    public boolean deleteUserOrder(Integer userId, Integer orderId) {
        // 获取订单
        Orders order = orderDao.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "订单不存在");
        }
        
        // 只有已完成或已取消的订单可以删除
        if (order.getStatus() != 3 && order.getStatus() != 4) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "当前订单状态不可删除");
        }
        
        return orderDao.deleteById(orderId) > 0;
    }
    
    /**
     * 管理员获取订单详情
     * @param orderId 订单ID
     * @return 订单信息
     */
    @Override
    public Orders getAdminOrderDetail(Integer orderId) {
        // 获取订单
        Orders order = orderDao.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "订单不存在");
        }
        
        // 获取订单商品
        List<OrderItems> orderItems = orderDao.selectOrderItemsByOrderId(orderId);
        order.setOrderItems(orderItems);
        
        // 获取收货地址
        UserAddress address = userAddressDao.selectById(order.getAddressId());
        order.setAddress(address);
        
        return order;
    }

    @Override
    public OrderStatisticsVO getOrderStatistics() {
        int totalOrders = orderDao.count();
        BigDecimal totalSales = orderDao.sumTotalAmount();
        return new OrderStatisticsVO(totalOrders, totalSales);
    }
}
