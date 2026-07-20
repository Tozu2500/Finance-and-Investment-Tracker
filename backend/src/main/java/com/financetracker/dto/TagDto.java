package com.financetracker.dto;

import com.financetracker.model.Tag;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TagDto(
    
    String id,
    @NotBlank @Size(max = 50) String name,
    String colorHex
) {

    public static TagDto from(Tag tag) {
        return new TagDto(tag.getId(), tag.getName(), tag.getColorHex());
    }

}
