package org.xian.boot.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;
import org.xian.boot.MyResponse;
import org.xian.boot.mapper.SysUserMapper;
import org.xian.boot.entity.SysUser;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author xian
 */
@Service
public class SysUserService {
    @Resource
    private SysUserMapper sysUserMapper;

    /**
     * 保存一条记录
     *
     * @param sysUser 用户信息
     * @return 保存结果
     */
    public MyResponse insert(SysUser sysUser) {
        try {
            sysUserMapper.insert(sysUser);
            return new MyResponse("success", "新增成功");
        } catch (Exception e) {
            return new MyResponse("error", e.getMessage());
        }
    }

    /**
     * 根据用户 ID 查询一条记录
     *
     * @param userId 用户 ID
     * @return 用户信息
     */
    public SysUser select(Long userId) {
        return sysUserMapper.selectByUserIdOnXml(userId);
    }

    /**
     * 根据用户名更新用户年龄、性别、昵称信息
     *
     * @param sysUser 用户信息
     * @return 结果
     */
    public MyResponse update(SysUser sysUser) {
        try {
            sysUserMapper.update(sysUser);
            return new MyResponse("success", "更新成功");
        } catch (Exception e) {
            return new MyResponse("error", e.getMessage());
        }
    }


    /**
     * 根据用户 ID 删除用户信息
     *
     * @param userId 用户 ID
     * @return 操作结果
     */
    public MyResponse delete(Long userId) {
        try {
            sysUserMapper.delete(userId);
            return new MyResponse("success", "删除成功");
        } catch (Exception e) {
            return new MyResponse("error", e.getMessage());
        }
    }

    /**
     * 分页浏览
     *
     * @return 所有用户信息
     */
    public List<SysUser> selectAll() {
        return sysUserMapper.selectAll();
    }

    public PageInfo<SysUser> selectPage(int page,int size) {
        // PageHelper 随后执行的查询会自动分页
        PageHelper.startPage(page, size);
        PageHelper.orderBy("user_id DESC");
        return PageInfo.of(sysUserMapper.selectAllOnXml());
    }
}
