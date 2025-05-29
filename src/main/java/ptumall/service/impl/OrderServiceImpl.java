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

/**
 * 订单服务实现类
 * 处理所有订单相关的业务逻辑
 * 包括：创建订单、查询订单、取消订单、支付订单等操作
 */
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
     * 支持两种创建方式：
     * 1. 从购物车创建订单
     * 2. 直接购买商品创建订单
     * 
     * @param userId 用户ID
     * @param param 创建订单参数
     * @return 创建成功的订单信息
     */
    @Override
    @Transactional
    public Orders createOrder(Integer userId, CreateOrderParam param) {
        // 验证收货地址
        UserAddress address = userAddressDao.selectById(param.getAddressId());
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "收货地址不存在");
        }
        
        // 创建订单基本信息
        Orders order = new Orders();
        order.setUserId(userId);
        order.setOrderNo(generateOrderNo());  // 生成订单编号
        order.setStatus((byte) 0);  // 设置订单状态为待付款
        order.setAddressId(param.getAddressId());
        
        // 初始化订单项列表和总金额
        List<OrderItems> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        // 处理购物车模式创建订单
        if (param.getCartItemIds() != null && !param.getCartItemIds().isEmpty()) {
            // 获取购物车商品列表
            List<Cart> cartList = cartDao.selectByIds(param.getCartItemIds());
            
            // 验证购物车商品
            for (Cart cart : cartList) {
                // 验证购物车是否属于当前用户
                if (!cart.getUserId().equals(userId)) {
                    throw new BusinessException(ResultCode.PARAM_ERROR, "购物车数据异常");
                }
                
                // 获取并验证商品信息
                Goods goods = goodsDao.selectById(cart.getGoodsId());
                if (goods == null) {
                    throw new BusinessException(ResultCode.PARAM_ERROR, "商品不存在");
                }
                
                // 验证商品状态
                if (goods.getStatus() == null || goods.getStatus() != 1) {
                    throw new BusinessException(ResultCode.PARAM_ERROR, "商品已下架");
                }
                
                // 验证商品库存
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
                
                // 累加订单总金额
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
            
            // 更新商品库存和销量
            for (Cart cart : cartList) {
                Goods goods = goodsDao.selectById(cart.getGoodsId());
                goodsDao.updateStock(goods.getId(), goods.getStock() - cart.getQuantity());
                goodsDao.updateSalesVolume(goods.getId(), cart.getQuantity());
            }
            
            // 清空已下单的购物车商品
            cartDao.deleteByIds(param.getCartItemIds());
        } 
        // 处理直接购买模式创建订单
        else if (param.getGoodsId() != null && param.getQuantity() != null && param.getQuantity() > 0) {
            // 获取并验证商品信息
            Goods goods = goodsDao.selectById(param.getGoodsId());
            if (goods == null) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "商品不存在");
            }
            
            // 验证商品状态
            if (goods.getStatus() == null || goods.getStatus() != 1) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "商品已下架");
            }
            
            // 验证商品库存
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
            
            // 更新商品库存和销量
            goodsDao.updateStock(goods.getId(), goods.getStock() - param.getQuantity());
            goodsDao.updateSalesVolume(goods.getId(), param.getQuantity());
        } else {
            throw new BusinessException(ResultCode.PARAM_ERROR, "参数错误");
        }
        
        // 设置订单商品和收货地址信息
        order.setOrderItems(orderItems);
        order.setAddress(address);
        
        return order;
    }

    /**
     * 生成订单编号
     * 格式：年月日+6位随机数
     * 例如：20240315123456
     * 
     * @return 订单编号
     */
    private String generateOrderNo() {
        String date = String.format("%1$tY%1$tm%1$td", new Date());
        String uuid = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 6);
        return date + uuid;
    }

    /**
     * 获取订单详情
     * 包括订单基本信息、订单商品、收货地址等
     * 
     * @param userId 用户ID
     * @param orderId 订单ID
     * @return 订单详细信息
     */
    @Override
    public Orders getOrderDetail(Integer userId, Integer orderId) {
        // 获取并验证订单
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
     * 
     * @param userId 用户ID
     * @param orderNo 订单编号
     * @return 订单详细信息
     */
    @Override
    public Orders getOrderByOrderNo(Integer userId, String orderNo) {
        // 获取并验证订单
        Orders order = orderDao.selectByOrderNo(orderNo);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "订单不存在");
        }
        
        // 获取订单商品和收货地址
        List<OrderItems> orderItems = orderDao.selectOrderItemsByOrderId(order.getId());
        order.setOrderItems(orderItems);
        
        UserAddress address = userAddressDao.selectById(order.getAddressId());
        order.setAddress(address);
        
        return order;
    }

    /**
     * 获取用户订单列表
     * 支持分页查询
     * 
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 分页的订单列表
     */
    @Override
    public PageResult<Orders> getOrderList(Integer userId, Integer pageNum, Integer pageSize) {
        // 分页查询订单
        PageHelper.startPage(pageNum, pageSize);
        List<Orders> orderList = orderDao.selectByUserId(userId);
        PageInfo<Orders> pageInfo = new PageInfo<>(orderList);
        
        // 获取每个订单的商品和地址信息
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
     * 
     * @param userId 用户ID
     * @param status 订单状态
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 分页的订单列表
     */
    @Override
    public PageResult<Orders> getOrderListByStatus(Integer userId, Byte status, Integer pageNum, Integer pageSize) {
        // 分页查询订单
        PageHelper.startPage(pageNum, pageSize);
        List<Orders> orderList = orderDao.selectByUserIdAndStatus(userId, status);
        PageInfo<Orders> pageInfo = new PageInfo<>(orderList);
        
        // 获取每个订单的商品和地址信息
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
     * 只能取消待付款的订单
     * 取消后会恢复商品库存和销量
     * 
     * @param userId 用户ID
     * @param orderId 订单ID
     * @return 是否取消成功
     */
    @Override
    @Transactional
    public boolean cancelOrder(Integer userId, Integer orderId) {
        // 获取并验证订单
        Orders order = orderDao.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "订单不存在");
        }
        
        // 验证订单状态
        if (order.getStatus() != 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "当前订单状态不可取消");
        }
        
        // 获取订单商品
        List<OrderItems> orderItems = orderDao.selectOrderItemsByOrderId(orderId);
        
        // 恢复商品库存和销量
        for (OrderItems item : orderItems) {
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
     * 只能支付待付款的订单
     * 
     * @param userId 用户ID
     * @param orderId 订单ID
     * @param paymentType 支付方式
     * @return 是否支付成功
     */
    @Override
    @Transactional
    public boolean payOrder(Integer userId, Integer orderId, Byte paymentType) {
        // 获取并验证订单
        Orders order = orderDao.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "订单不存在");
        }
        
        // 验证订单状态
        if (order.getStatus() != 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "当前订单状态不可支付");
        }
        
        // 更新订单状态为已支付(1)
        return orderDao.pay(orderId, paymentType) > 0;
    }

    /**
     * 确认收货
     * 只能确认已发货的订单
     * 
     * @param userId 用户ID
     * @param orderId 订单ID
     * @return 是否确认成功
     */
    @Override
    @Transactional
    public boolean confirmReceipt(Integer userId, Integer orderId) {
        // 获取并验证订单
        Orders order = orderDao.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "订单不存在");
        }
        
        // 验证订单状态
        if (order.getStatus() != 2) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "当前订单状态不可确认收货");
        }
        
        // 更新订单状态为已完成(3)
        return orderDao.updateStatus(orderId, (byte) 3) > 0;
    }

    /**
     * 获取所有订单列表（管理员接口）
     * 
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 分页的订单列表
     */
    @Override
    public PageResult<Orders> getAllOrderList(Integer pageNum, Integer pageSize) {
        // 分页查询所有订单
        PageHelper.startPage(pageNum, pageSize);
        List<Orders> orderList = orderDao.selectAll();
        PageInfo<Orders> pageInfo = new PageInfo<>(orderList);
        
        // 获取每个订单的商品和地址信息
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
     * 
     * @param status 订单状态
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 分页的订单列表
     */
    @Override
    public PageResult<Orders> getAllOrderListByStatus(Byte status, Integer pageNum, Integer pageSize) {
        // 分页查询订单
        PageHelper.startPage(pageNum, pageSize);
        List<Orders> orderList = orderDao.selectAllByStatus(status);
        PageInfo<Orders> pageInfo = new PageInfo<>(orderList);
        
        // 获取每个订单的商品和地址信息
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
     * 
     * @param orderNo 订单号关键词
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 分页的订单列表
     */
    @Override
    public PageResult<Orders> searchOrdersByOrderNo(String orderNo, Integer pageNum, Integer pageSize) {
        // 分页查询订单
        PageHelper.startPage(pageNum, pageSize);
        List<Orders> orderList = orderDao.selectByOrderNoKeyword(orderNo);
        PageInfo<Orders> pageInfo = new PageInfo<>(orderList);
        
        // 获取每个订单的商品和地址信息
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
     * 
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 分页的订单列表
     */
    @Override
    public PageResult<Orders> searchOrdersByUserId(Integer userId, Integer pageNum, Integer pageSize) {
        // 分页查询订单
        PageHelper.startPage(pageNum, pageSize);
        List<Orders> orderList = orderDao.selectByUserIdAdmin(userId);
        PageInfo<Orders> pageInfo = new PageInfo<>(orderList);
        
        // 获取每个订单的商品和地址信息
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
     * 
     * @param orderNo 订单号关键词
     * @param status 订单状态
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 分页的订单列表
     */
    @Override
    public PageResult<Orders> searchOrdersByOrderNoAndStatus(String orderNo, Byte status, Integer pageNum, Integer pageSize) {
        // 分页查询订单
        PageHelper.startPage(pageNum, pageSize);
        List<Orders> orderList = orderDao.selectByOrderNoAndStatus(orderNo, status);
        PageInfo<Orders> pageInfo = new PageInfo<>(orderList);
        
        // 获取每个订单的商品和地址信息
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
     * 
     * @param userId 用户ID
     * @param status 订单状态
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 分页的订单列表
     */
    @Override
    public PageResult<Orders> searchOrdersByUserIdAndStatus(Integer userId, Byte status, Integer pageNum, Integer pageSize) {
        // 分页查询订单
        PageHelper.startPage(pageNum, pageSize);
        List<Orders> orderList = orderDao.selectByUserIdAndStatusAdmin(userId, status);
        PageInfo<Orders> pageInfo = new PageInfo<>(orderList);
        
        // 获取每个订单的商品和地址信息
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
     * 只能对已支付的订单进行发货
     * 
     * @param orderId 订单ID
     * @return 是否发货成功
     */
    @Override
    @Transactional
    public boolean shipOrder(Integer orderId) {
        // 获取并验证订单
        Orders order = orderDao.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "订单不存在");
        }
        
        // 验证订单状态
        if (order.getStatus() != 1) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "当前订单状态不可发货");
        }
        
        // 更新订单状态为已发货(2)
        return orderDao.updateStatus(orderId, (byte) 2) > 0;
    }
    
    /**
     * 删除订单（管理员接口）
     * 会同时删除订单和订单商品
     * 
     * @param orderId 订单ID
     * @return 是否删除成功
     */
    @Override
    @Transactional
    public boolean deleteOrder(Integer orderId) {
        // 获取并验证订单
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
     * 只能删除已完成或已取消的订单
     * 
     * @param userId 用户ID
     * @param orderId 订单ID
     * @return 是否删除成功
     */
    @Override
    @Transactional
    public boolean deleteUserOrder(Integer userId, Integer orderId) {
        // 获取并验证订单
        Orders order = orderDao.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "订单不存在");
        }
        
        // 验证订单状态
        if (order.getStatus() != 3 && order.getStatus() != 4) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "当前订单状态不可删除");
        }
        
        return orderDao.deleteById(orderId) > 0;
    }
    
    /**
     * 管理员获取订单详情
     * 
     * @param orderId 订单ID
     * @return 订单详细信息
     */
    @Override
    public Orders getAdminOrderDetail(Integer orderId) {
        // 获取并验证订单
        Orders order = orderDao.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "订单不存在");
        }
        
        // 获取订单商品和收货地址
        List<OrderItems> orderItems = orderDao.selectOrderItemsByOrderId(orderId);
        order.setOrderItems(orderItems);
        
        UserAddress address = userAddressDao.selectById(order.getAddressId());
        order.setAddress(address);
        
        return order;
    }

    /**
     * 获取订单统计信息
     * 包括总订单数和总销售额
     * 
     * @return 订单统计信息
     */
    @Override
    public OrderStatisticsVO getOrderStatistics() {
        int totalOrders = orderDao.count();
        BigDecimal totalSales = orderDao.sumTotalAmount();
        return new OrderStatisticsVO(totalOrders, totalSales);
    }
}
