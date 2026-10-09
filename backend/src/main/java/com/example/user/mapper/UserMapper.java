package com.example.user.mapper;

import org.mapstruct.Mapper;

import com.example.config.MappingConfig;
import com.example.user.dto.UserResponse;
import com.example.user.model.UserEntity;

/**
 * 
 * <b>UserMapper</b>
 * <br>
 * Mapper interface responsible for mapping User objects to DTOs.
 */
@Mapper (
    config = MappingConfig.class
)
public interface UserMapper {
    UserResponse toResponse(UserEntity user);
}
