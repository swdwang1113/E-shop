package ptumall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ptumall.dao.GoodsDao;
import ptumall.model.Goods;
import ptumall.service.GoodsService;

import java.util.List;

@Service
public class GoodsServiceImpl implements GoodsService {
    @Autowired(required = false)
    GoodsDao goodsDao;
    @Value("${image.prefix-url}")
    String imgUrl;

    @Override
    public PageInfo<Goods> getAllgoods(Integer pageNum, Integer pageSize) {
        //开启分页
        PageHelper.startPage(pageNum,pageSize);
        List<Goods> goodsList = goodsDao.getAllgoods();
        for(Goods goods : goodsList)
        {
            String picpath = goods.getGpicture();
            goods.setGpicture(imgUrl + picpath);
        }
        PageInfo<Goods> pageInfo = new PageInfo<>(goodsList);
        return  pageInfo;
    }

    @Override
    public Goods getGoodById(Integer gid) {
        Goods goods = goodsDao.getGoodById(gid);
        if(goods==null)
        {
            return null;
        }
        String picpath = goods.getGpicture();
        goods.setGpicture(imgUrl + picpath);
        return goods;
    }

    @Override
    public PageInfo<Goods> searchGoodsByName(String gname, Integer pageNum, Integer pageSize) {
        //开启分页
        PageHelper.startPage(pageNum,pageSize);
        List<Goods> goodsList = goodsDao.searchGoodsByName(gname);
        for(Goods goods : goodsList)
        {
            String picpath = goods.getGpicture();
            goods.setGpicture(imgUrl + picpath);
        }
        PageInfo<Goods> pageInfo = new PageInfo<>(goodsList);
        return  pageInfo;
    }
}
