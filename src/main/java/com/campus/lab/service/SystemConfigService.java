package com.campus.lab.service;

import com.campus.lab.entity.SystemConfig;
import com.campus.lab.form.ConfigForm;
import com.campus.lab.repository.SystemConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 预约规则配置业务
 */
@Service
@RequiredArgsConstructor
public class SystemConfigService {

    private final SystemConfigRepository systemConfigRepository;

    public SystemConfig get() {
        return systemConfigRepository.findFirstByOrderByIdAsc().orElseGet(() -> {
            SystemConfig config = new SystemConfig();
            config.setMaxDurationHours(4);
            config.setAdvanceDays(7);
            config.setBookingRules("""
                    1. 实验室开放时间以各实验室公示的开放时段为准。
                    2. 每次预约最长不超过 4 小时，最多可提前 7 天预约。
                    3. 预约提交后需教师审核，审核通过后方可使用实验室。
                    4. 如需取消预约，请在预约开始前操作取消。
                    5. 使用期间请遵守实验室安全规范，爱护设备。
                    """);
            return systemConfigRepository.save(config);
        });
    }

    public ConfigForm toForm(SystemConfig config) {
        ConfigForm form = new ConfigForm();
        form.setMaxDurationHours(config.getMaxDurationHours());
        form.setAdvanceDays(config.getAdvanceDays());
        form.setBookingRules(config.getBookingRules());
        return form;
    }

    @Transactional
    public void save(ConfigForm form) {
        SystemConfig config = get();
        config.setMaxDurationHours(form.getMaxDurationHours());
        config.setAdvanceDays(form.getAdvanceDays());
        config.setBookingRules(form.getBookingRules().trim());
        systemConfigRepository.save(config);
    }
}
