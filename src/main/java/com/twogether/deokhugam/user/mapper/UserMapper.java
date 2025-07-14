package com.twogether.deokhugam.user.mapper;

import com.twogether.deokhugam.user.dto.UserDto;
import com.twogether.deokhugam.user.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDto toDto(User user);
}
