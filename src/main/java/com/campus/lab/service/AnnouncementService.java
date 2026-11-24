package com.campus.lab.service;

import com.campus.lab.common.BusinessException;
import com.campus.lab.entity.Announcement;
import com.campus.lab.form.AnnouncementForm;
import com.campus.lab.repository.AnnouncementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 系统公告业务
 */
@Service
@RequiredArgsConstructor
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;

    public List<Announcement> latest() {
        return announcementRepository.findTop3ByOrderByCreateTimeDesc();
    }

    public List<Announcement> listAll() {
        return announcementRepository.findAllByOrderByCreateTimeDesc();
    }

    public Page<Announcement> page(int pageNo) {
        return announcementRepository.findAll(
                PageRequest.of(pageNo, 10, Sort.by(Sort.Direction.DESC, "createTime")));
    }

    public Announcement get(Long id) {
        return announcementRepository.findById(id).orElseThrow(() -> new BusinessException("公告不存在"));
    }

    @Transactional
    public void save(AnnouncementForm form, String publisher) {
        Announcement announcement;
        if (form.getId() == null) {
            announcement = new Announcement();
            announcement.setPublisher(publisher);
        } else {
            announcement = get(form.getId());
        }
        announcement.setTitle(form.getTitle().trim());
        announcement.setContent(form.getContent().trim());
        announcementRepository.save(announcement);
    }

    @Transactional
    public void delete(Long id) {
        announcementRepository.delete(get(id));
    }
}
