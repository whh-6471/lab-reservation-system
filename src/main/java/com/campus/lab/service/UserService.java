package com.campus.lab.service;

import com.campus.lab.common.BusinessException;
import com.campus.lab.common.Role;
import com.campus.lab.entity.User;
import com.campus.lab.form.UserForm;
import com.campus.lab.repository.ReservationRepository;
import com.campus.lab.repository.UserRepository;
import com.campus.lab.util.PasswordUtil;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户管理业务（管理员）
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;

    public Page<User> page(String role, String keyword, Integer status, int pageNo) {
        Specification<User> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (role != null && !role.isBlank()) {
                predicates.add(cb.equal(root.get("role"), role));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (keyword != null && !keyword.isBlank()) {
                String like = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("realName")), like),
                        cb.like(cb.lower(root.get("username")), like),
                        cb.like(cb.lower(root.get("studentNo")), like)
                ));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return userRepository.findAll(spec, PageRequest.of(pageNo, 10, Sort.by(Sort.Direction.DESC, "id")));
    }

    public User get(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new BusinessException("用户不存在"));
    }

    public UserForm toForm(User user) {
        UserForm form = new UserForm();
        form.setId(user.getId());
        form.setUsername(user.getUsername());
        form.setRealName(user.getRealName());
        form.setRole(user.getRole());
        form.setStudentNo(user.getStudentNo());
        form.setDepartment(user.getDepartment());
        form.setPhone(user.getPhone());
        form.setEmail(user.getEmail());
        return form;
    }

    @Transactional
    public void create(UserForm form) {
        String username = form.getUsername().trim();
        if (userRepository.existsByUsername(username)) {
            throw new BusinessException("该账号已存在，请更换账号");
        }
        if (form.getPassword() == null || form.getPassword().isBlank()) {
            throw new BusinessException("新增用户必须设置登录密码");
        }
        if (!Role.isValid(form.getRole())) {
            throw new BusinessException("角色不合法");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(PasswordUtil.md5(form.getPassword()));
        user.setRealName(form.getRealName().trim());
        user.setRole(form.getRole());
        user.setStudentNo(form.getStudentNo());
        user.setDepartment(form.getDepartment());
        user.setPhone(form.getPhone());
        user.setEmail(form.getEmail());
        user.setStatus(1);
        userRepository.save(user);
    }

    @Transactional
    public void update(UserForm form) {
        User user = get(form.getId());
        String username = form.getUsername().trim();
        if (userRepository.existsByUsernameAndIdNot(username, user.getId())) {
            throw new BusinessException("该账号已被其他用户使用");
        }
        if (!Role.isValid(form.getRole())) {
            throw new BusinessException("角色不合法");
        }
        user.setUsername(username);
        user.setRealName(form.getRealName().trim());
        user.setRole(form.getRole());
        user.setStudentNo(form.getStudentNo());
        user.setDepartment(form.getDepartment());
        user.setPhone(form.getPhone());
        user.setEmail(form.getEmail());
        if (form.getPassword() != null && !form.getPassword().isBlank()) {
            user.setPassword(PasswordUtil.md5(form.getPassword()));
        }
        userRepository.save(user);
    }

    @Transactional
    public void toggleStatus(Long id, Long operatorId) {
        if (id.equals(operatorId)) {
            throw new BusinessException("不能禁用当前登录的管理员账号");
        }
        User user = get(id);
        user.setStatus(user.getStatus() != null && user.getStatus() == 1 ? 0 : 1);
        userRepository.save(user);
    }

    @Transactional
    public void delete(Long id, Long operatorId) {
        if (id.equals(operatorId)) {
            throw new BusinessException("不能删除当前登录账号");
        }
        User user = get(id);
        if (reservationRepository.existsByApplicantId(id)) {
            throw new BusinessException("该用户存在预约记录，无法删除，建议改为禁用");
        }
        userRepository.delete(user);
    }
}
