package org.xian.boot.service;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.xian.boot.MyResponse;
import org.xian.boot.entity.SysUser;
import org.xian.boot.repository.SysUserRepository;

import javax.annotation.Resource;
import java.util.List;

/**
 * SysUser 服务层
 *
 * @author xian
 */
@Service
public class SysUserService {
    @Resource
    private SysUserRepository sysUserRepository;


    /**
     * 保存一条记录
     *
     * @param sysUser 用户信息
     * @return 保存结果
     */
    public MyResponse save(SysUser sysUser) {
        try {
            sysUserRepository.save(sysUser);
            return new MyResponse("success", "新增成功");
        } catch (Exception e) {
            return new MyResponse("error", e.getMessage());
        }
    }

    /**
     * 根据用户民查询用户信息
     *
     * @param username 用户名
     * @return 用户信息
     */
    public SysUser find(String username) {
        return sysUserRepository.findByUsername(username);
    }

    /**
     * 根据用户名更新数据
     *
     * @param sysUser 需要更新的用户信息
     * @return 更新结果
     */
    public MyResponse update(SysUser sysUser) {
        try {
            SysUser oldSysUser = sysUserRepository.findByUsername(sysUser.getUsername());
            sysUser.setUserId(oldSysUser.getUserId());
            sysUserRepository.save(sysUser);
            return new MyResponse("success", "更新成功");
        } catch (Exception e) {
            return new MyResponse("error", e.getMessage());
        }
    }

    public MyResponse delete(String username) {
        try {
            SysUser oldSysUser = sysUserRepository.findByUsername(username);
            sysUserRepository.delete(oldSysUser);
            return new MyResponse("success", "删除成功");
        } catch (Exception e) {
            return new MyResponse("error", e.getMessage());
        }
    }

    public MyResponse saveAll(List<SysUser> sysUserList) {
        try {
            sysUserRepository.saveAll(sysUserList);
            return new MyResponse("success", "新增成功");
        } catch (Exception e) {
            return new MyResponse("error", e.getMessage());
        }
    }

    public List<SysUser> list() {
        return sysUserRepository.findAll();
    }

    public Page<SysUser> page(Integer page, Integer size) {
        // 根据 userId 排序
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.ASC, "userId");
        return sysUserRepository.findAll(pageable);
    }

    public Page<SysUser> searchByNickname(String nickname, Integer page, Integer size) {
        // 根据 userId 排序
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.ASC, "userId");
        return sysUserRepository.searchByNickname(nickname,pageable);
    }
}
