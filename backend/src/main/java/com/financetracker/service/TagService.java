package com.financetracker.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.financetracker.dto.TagDto;
import com.financetracker.model.Tag;
import com.financetracker.model.User;
import com.financetracker.repository.TagRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    public List<TagDto> getAll(User user) {
        return tagRepository.findByUserOrderByNameAsc(user).stream()
            .map(TagDto::from).toList();
    }

    @Transactional
    public TagDto create(User user, TagDto dto) {
        if (tagRepository.existsByNameIgnoreCaseAndUser(dto.name(), user)) {
            throw new BadRequestException("Tag '" + dto.name() + "' already exists");
        }

        Tag tag = Tag.builder()
            .name(dto.name().trim())
            .colorHex(dto.colorHex() != null ? dto.colorHex() : "#6366f1")
            .user(user)
            .build();
        return TagDto.from(tagRepository.save(tag));
    }

    @Transactional
    public TagDto update(String id, User user, TagDto dto) {
        Tag tag = tagRepository.findByIdAndUser(id, user)
            .orElseThrow(() -> new ResourceNotFoundException("Tag not found"));
        tag.setName(dto.name().trim());

        if (dto.colorHex() != null) tag.setColorHex(dto.colorHex());

        return TagDto.from(tagRepository.save(tag));
    }

    @Transactional
    public void delete(String id, User user) {
        Tag tag = tagRepository.findByIdAndUser(id, user)
            .orElseThrow(() -> new ResourceNotFoundException("Tag not found"));
        tagRepository.delete(tag);
    }
}
