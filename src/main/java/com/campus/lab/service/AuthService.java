package com.campus.lab.service;

import com.campus.lab.common.BusinessException;
import com.campus.lab.common.LoginUser;
import com.campus.lab.common.Role;
import com.campus.lab.entity.User;
import com.campus.lab.form.LoginForm;
import com.campus.lab.form.PasswordForm;
import com.campus.lab.form.ProfileForm;
import com.campus.lab.form.RegisterForm;
import com.campus.lab.repository.UserRepository;
import com.campus.lab.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 登录、注册与个人中心业务
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    public LoginUser login(LoginForm form) {
        User user = userRepository.findByUsername(form.getUsername().trim())
                .orElseThrow(() -> new BusinessException("账号或密码错误"));
        if (!PasswordUtil.md5(form.getPassword()).equals(user.getPassword())) {
            throw new BusinessException("账号或密码错误");
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException("该账号已被禁用，请联系管理员");
        }
        LoginUser loginUser = new LoginUser();
        loginUser.setId(user.getId());
        loginUser.setUsername(user.getUsername());
        loginUser.setRealName(user.getRealName());
        loginUser.setRole(user.getRole());
        loginUser.setDepartment(user.getDepartment());
        return loginUser;
    }

    @Transactional
    public void register(RegisterForm form) {
        String username = form.getUsername().trim();
        if (!form.getPassword().equals(form.getConfirmPassword())) {
            throw new BusinessException("两次输入的密码不一致");
        }
        if (userRepository.existsByUsername(username)) {
            throw new BusinessException("该账号已存在，请更换账号");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(PasswordUtil.md5(form.getPassword()));
        user.setRealName(form.getRealName().trim());
        user.setRole(Role.STUDENT);
        user.setStudentNo(form.getStudentNo());
        user.setDepartment(form.getDepartment());
        user.setPhone(form.getPhone());
        user.setEmail(form.getEmail());
        user.setStatus(1);
        userRepository.save(user);
    }

    public User get(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new BusinessException("用户不存在"));
    }

    @Transactional
    public void updateProfile(Long id, ProfileForm form) {
        User user = get(id);
        user.setRealName(form.getRealName().trim());
        user.setStudentNo(form.getStudentNo());
        user.setDepartment(form.getDepartment());
        user.setPhone(form.getPhone());
        user.setEmail(form.getEmail());
        userRepository.save(user);
    }

    @Transactional
    public void changePassword(Long id, PasswordForm form) {
        User user = get(id);
        if (!PasswordUtil.md5(form.getOldPassword()).equals(user.getPassword())) {
            throw new BusinessException("原密码不正确");
        }
        if (!form.getNewPassword().equals(form.getConfirmPassword())) {
            throw new BusinessException("两次输入的新密码不一致");
        }
        user.setPassword(PasswordUtil.md5(form.getNewPassword()));
        userRepository.save(user);
    }
}
